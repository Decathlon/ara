/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.PlannedIndexation;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.custom.util.TransactionAppenderUtil;
import com.decathlon.ara.service.ErrorService;
import com.decathlon.ara.service.ExecutionFilesProcessorService;
import com.decathlon.ara.service.ProblemDenormalizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExecutionIndexerService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionIndexerService.class);

    private final ExecutionRepository executionRepository;

    private final ExecutionFilesProcessorService executionFilesProcessorService;

    private final ErrorService errorService;

    private final QualityEmailService qualityEmailService;

    private final ProblemDenormalizationService problemDenormalizationService;

    private final TransactionAppenderUtil transactionAppenderUtil;

    public ExecutionIndexerService(ExecutionRepository executionRepository,
            ExecutionFilesProcessorService executionFilesProcessorService, ErrorService errorService,
            QualityEmailService qualityEmailService, ProblemDenormalizationService problemDenormalizationService,
            TransactionAppenderUtil transactionAppenderUtil) {
        this.executionRepository = executionRepository;
        this.executionFilesProcessorService = executionFilesProcessorService;
        this.errorService = errorService;
        this.qualityEmailService = qualityEmailService;
        this.problemDenormalizationService = problemDenormalizationService;
        this.transactionAppenderUtil = transactionAppenderUtil;
    }

    /**
     * Index the execution of a test cycle.<br>
     * Can come from a continuous integration build (with possibly sub-builds).
     *
     * @param plannedIndexation the execution indexation being planned. It contains both:
     *                          - the folder containing the descriptions of the build to be indexed as an execution
     *                          - the cycle definition (branch, cycle)
     */
    @Transactional
    public void indexExecution(PlannedIndexation plannedIndexation) {
        if (plannedIndexation == null) {
            LOG.warn("EXECUTION|No execution indexation found");
            return;
        }

        File rawExecutionFolder = plannedIndexation.getExecutionFolder();
        if (rawExecutionFolder == null) {
            LOG.warn("EXECUTION|No execution folder found");
            return;
        }

        CycleDefinition cycleDefinition = plannedIndexation.getCycleDefinition();
        if (cycleDefinition == null) {
            LOG.warn("EXECUTION|No cycle definition found");
            return;
        }
        String branch = cycleDefinition.getBranch();
        String cycle = cycleDefinition.getName();
        final Long projectId = cycleDefinition.getProjectId();

        String link = rawExecutionFolder.getAbsolutePath() + File.separator;
        LOG.info("EXECUTION|Began execution indexing {}/{} for link {}", branch, cycle, link);

        Optional<Execution> previousExecution = executionRepository.findByCycleDefinitionProjectIdAndJobLinkAndJobLinkNotNull(projectId, link);
        List<Long> existingErrorIds = getErrorIds(previousExecution);

        Optional<Execution> processedExecution = executionFilesProcessorService.getExecution(plannedIndexation);

        if (!processedExecution.isPresent()) {
            LOG.warn("EXECUTION|Could not extract any execution from the directory {}", link);
            LOG.warn("EXECUTION|Some of the files may be incorrect, please check again");
            return;
        }

        final Execution savedExecution = executionRepository.save(processedExecution.get());

        List<Long> newErrorIds = new ArrayList<>(getErrorIds(Optional.of(savedExecution)));

        newErrorIds.removeAll(existingErrorIds);
        if (!newErrorIds.isEmpty()) {
            final Set<Problem> updatedProblems = errorService.autoAssignProblemsToNewErrors(projectId, newErrorIds);
            problemDenormalizationService.updateFirstAndLastSeenDateTimes(updatedProblems);
        }

        if (JobStatus.DONE.equals(savedExecution.getStatus())) {
            transactionAppenderUtil.doAfterCommit(() -> safelySendQualityEmail(savedExecution));
        }

        String url = processedExecution.get().getJobUrl();
        LOG.info("EXECUTION|Ended indexing execution {}/{} job URL {} and link {}", branch, cycle, url, link);
    }

    /**
     * @param execution send the quality email for this execution without throwing any exception (errors are logged):
     *                  an email failure is not a problem for the remaining of business logic
     */
    private void safelySendQualityEmail(Execution execution) {
        try {
            qualityEmailService.sendQualityEmail(execution.getCycleDefinition().getProjectId(), execution.getId());
        } catch (Exception e) {
            LOG.warn("EXECUTION|Uncaught exception while sending quality email (continuing normally)", e);
        }
    }

    List<Long> getErrorIds(Optional<Execution> execution) {
        if (!execution.isPresent()) {
            return Collections.emptyList();
        }
        return execution.get().getRuns().stream()
                .flatMap(run -> run.getExecutedScenarios().stream())
                .flatMap(executedScenario -> executedScenario.getErrors().stream())
                .map(Error::getId)
                .collect(Collectors.toList());
    }

}
