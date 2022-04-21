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

package com.decathlon.ara.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.ci.bean.PlannedIndexation;
import com.decathlon.ara.ci.service.ExecutionIndexerService;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.ExecutionCompletionRequest;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemOccurrence;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.repository.ExecutionCompletionRequestRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.scenario.cucumber.util.ScenarioExtractorUtil;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.execution.ExecutionCriteriaDTO;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.execution.ExecutionWithHandlingCountsDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.GenericMapper;
import com.decathlon.ara.service.support.Settings;

/**
 * Service for managing Execution.
 */
@Service
@Transactional
public class ExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionService.class);

    private static final String STILL_COMPUTING = "STILL_COMPUTING";

    private final ExecutionRepository executionRepository;

    private final ExecutionCompletionRequestRepository executionCompletionRequestRepository;

    private final FunctionalityRepository functionalityRepository;

    private final GenericMapper mapper;

    private final ExecutionHistoryService executionHistoryService;

    private final ArchiveService archiveService;

    private final SettingService settingService;

    private final ExecutionIndexerService executionIndexerService;

    private final CycleDefinitionRepository cycleDefinitionRepository;

    private final ProblemService problemService;

    @Autowired
    public ExecutionService(ExecutionRepository executionRepository,
            ExecutionCompletionRequestRepository executionCompletionRequestRepository,
            FunctionalityRepository functionalityRepository, GenericMapper mapper, ExecutionHistoryService executionHistoryService,
            ArchiveService archiveService, SettingService settingService,
            ExecutionIndexerService executionIndexerService, CycleDefinitionRepository cycleDefinitionRepository, ProblemService problemService) {
        this.executionRepository = executionRepository;
        this.executionCompletionRequestRepository = executionCompletionRequestRepository;
        this.functionalityRepository = functionalityRepository;
        this.mapper = mapper;
        this.executionHistoryService = executionHistoryService;
        this.archiveService = archiveService;
        this.settingService = settingService;
        this.executionIndexerService = executionIndexerService;
        this.cycleDefinitionRepository = cycleDefinitionRepository;
        this.problemService = problemService;
    }

    /**
     * Get all executions.
     *
     * @param projectId the ID of the project in which to work
     * @param pageable  the meta-data of the requested page
     * @return the executions, ordered by decreasing test date
     */
    @Transactional(readOnly = true)
    public Page<ExecutionWithHandlingCountsDTO> findAll(long projectId, Pageable pageable) {
        return executionRepository.findAllByProjectIdOrderByTestDateTimeDesc(projectId, pageable).map(this::toDtoWithAggregate);
    }

    /**
     * Get one execution by id.
     *
     * @param projectId     the ID of the project in which to work
     * @param id            the id of the entity
     * @param criteria      the search criteria to use while filtering the executed scenarios.
     * @return the entity
     * @throws NotFoundException when the execution cannot be found
     */
    @Transactional(readOnly = true)
    public ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO findOneWithRuns(long projectId, long id, ExecutionCriteriaDTO criteria) throws NotFoundException {
        Execution execution = executionRepository.findByProjectIdAndId(projectId, id);
        if (execution == null) {
            throw new NotFoundException(Messages.NOT_FOUND_EXECUTION, Entities.EXECUTION);
        }

        if (!criteria.isWithSucceed()) {
            this.removeScenariosWithoutErrors(execution);
        }

        return mapper.map(execution, ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO.class,
                (entity, dto) -> mapExecutionProblem(projectId, entity, dto));
    }

    private void mapExecutionProblem(Long projectId, Execution execution, ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO dto) {
        int[] positions = { 0, 0, 0 };
        final Map<Long, Long> functionalityTeamIds = functionalityRepository.getFunctionalityTeamIds(projectId);
        for (Run run : safeToIterate(execution.getRuns())) {
            for (ExecutedScenario executedScenario : safeToIterate(run.getExecutedScenarios())) {
                ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO executedScenarioDto = dto.getRuns().get(positions[0]).getExecutedScenarios().get(positions[1]);
                executedScenarioDto.setTeamIds(ScenarioExtractorUtil.extractFunctionalityIds(executedScenario.getName()).stream()
                        .map(functionalityTeamIds::get)
                        .filter(Objects::nonNull) // Unknown functionality IDs have null team IDs
                        .collect(Collectors.toSet()));
                for (Error error : safeToIterate(executedScenario.getErrors())) {
                    List<Problem> problems = error.getProblemOccurrences().stream()
                            .map(ProblemOccurrence::getProblemPattern)
                            .map(ProblemPattern::getProblem)
                            .sorted(Comparator.nullsLast(Problem::compareTo))
                            .distinct()
                            .toList();
                    ErrorWithProblemsDTO errorDto = executedScenarioDto.getErrors().get(positions[2]);
                    errorDto.setProblems(mapper.mapCollection(problems, ProblemDTO.class));
                    for (ProblemDTO problem : errorDto.getProblems()) {
                        problem.setDefectUrl(problemService.retrieveDefectUrl(projectId, problem));
                    }
                    positions[2] = positions[2] + 1;
                }
                positions[1] = positions[1] + 1;
                positions[2] = 0;
            }
            positions[0] = positions[0] + 1;
            positions[1] = 0;
            positions[2] = 0;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Collection<?>> T safeToIterate(T collection) {
        if (collection == null) {
            return (T) Collections.emptyList();
        }
        return collection;
    }

    private void removeScenariosWithoutErrors(Execution execution) {
        for (Run run : execution.getRuns()) {
            run.getExecutedScenarios().removeIf(s -> s.getErrors().isEmpty());
        }
    }

    /**
     * Discard an execution while assigning it a discard reason, or change discard reason if already discarded.
     *
     * @param projectId     the ID of the project in which to work
     * @param id            the ID of the execution to discard
     * @param discardReason the reason explaining to discarding of the execution
     * @return the modified and saved Execution
     * @throws BadRequestException if the execution cannot be found or the discarded reason is null or empty
     */
    public ExecutionDTO discard(long projectId, long id, String discardReason) throws BadRequestException {
        if (StringUtils.isEmpty(discardReason)) {
            throw new BadRequestException(Messages.RULE_DISCARDED_EXECUTIONS_MUST_HAVE_REASON, Entities.EXECUTION, "reason_mandatory_for_discarded_executions");
        }

        Execution execution = executionRepository.findByProjectIdAndId(projectId, id);
        if (execution == null) {
            throw new NotFoundException(Messages.NOT_FOUND_EXECUTION, Entities.EXECUTION);
        }

        execution.setAcceptance(ExecutionAcceptance.DISCARDED);
        execution.setDiscardReason(discardReason);
        return mapper.map(executionRepository.save(execution), ExecutionDTO.class);
    }

    /**
     * Un-discard an execution and reset its discard reason. Does nothing if the execution is not discarded.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the ID of the execution to un-discard
     * @return the modified and saved Execution
     * @throws NotFoundException if the execution cannot be found
     */
    public ExecutionDTO unDiscard(long projectId, long id) throws NotFoundException {
        Execution execution = executionRepository.findByProjectIdAndId(projectId, id);
        if (execution == null) {
            throw new NotFoundException(Messages.NOT_FOUND_EXECUTION, Entities.EXECUTION);
        }

        execution.setAcceptance(ExecutionAcceptance.NEW);
        execution.setDiscardReason(null);
        return mapper.map(executionRepository.save(execution), ExecutionDTO.class);
    }

    /**
     * The execution job is about to complete: using this request, the job signals this to ARA, and ARA sets a flag on the execution.<br>
     * When the crawler will next run, it will unset the flag at the same time as indexing the very latest data about the job.<br>
     * Between the time the flag is set and the flag is unset, the request to get quality status will reply STILL_COMPUTING
     * as it cannot guarantee it has all necessary information yet in order to return the definitive quality status.<br>
     * Only a crawling started AFTER the completion-request is guaranteed to have the latest data, so only such crawling will unset the flag.
     *
     * @param projectId the ID of the project in which to work
     * @param jobUrl    the job URL of the execution to mark for completion
     */
    public void requestCompletion(long projectId, String jobUrl) {
        final Execution execution = executionRepository.findByProjectIdAndJobUrl(projectId, jobUrl);

        // Cycle-run not indexed yet OR not done yet (will never be crawled again if done: we have the definitive data)
        if (execution == null || execution.getStatus() != JobStatus.DONE) {
            // The flag is in a separate table (and not an execution column) for two reasons:
            // * The execution may not be crawled yet, so no row in database (severe enough if it was a column ;-) )
            // * A crawling might be in progress (with NOT up to date data) while setting the flag, and the crawling save would update the whole execution row, removing the flag before the next crawling would have a chance to run with the complete data (resulting in a non-definitive quality-status report meanwhile)
            executionCompletionRequestRepository.save(new ExecutionCompletionRequest(jobUrl));
        }
    }

    /**
     * Get the quality status of the execution for the given job.<br>
     * WARNING: to be sure to get reliable results, /request-completion must be called prior to this call.<br>
     * "STILL_COMPUTING" is returned if it too soon to get the definitive quality status: in this case, you must
     * query again every few tens of seconds until something else is returned: it will be the definitive quality status.
     *
     * @param projectId the ID of the project in which to work
     * @param jobUrl    the job URL of the execution
     * @return "STILL_COMPUTING" if the flag set by /request-completion is still there (indexation is not done yet),
     * or one of the {@link QualityStatus} enumeration names when the
     * definitive quality status of the execution is known
     */
    public String getQualityStatus(long projectId, String jobUrl) {
        final Execution execution = executionRepository.findByProjectIdAndJobUrl(projectId, jobUrl);

        // Not indexed yet (plus, if the completionRequested flag is set and the execution crashed with no usable data,
        // it will get indexed (as CRASHED) next time the crawler run)
        if (execution == null) {
            return STILL_COMPUTING; // Please call getQualityStatus later: status will be set soon
        }

        // When DONE, an execution will not be re-indexed anymore (we have the definitive data), no matter if the flag is set or not
        if (execution.getStatus() != JobStatus.DONE) {
            final Optional<ExecutionCompletionRequest> request = executionCompletionRequestRepository.findById(jobUrl);
            if (request.isPresent()) {
                return STILL_COMPUTING; // Please call getQualityStatus later: status will be set soon
            }
        }

        if (crashed(execution)) {
            return "CRASHED";
        } else if (execution.getBlockingValidation() != Boolean.TRUE) {
            return "NOT_BLOCKING";
        } else if (StringUtils.isNotEmpty(execution.getDiscardReason())) {
            return "DISCARDED";
        }

        // We have the definitive data (or completionRequested was never called beforehand: not our problem)
        return (execution.getQualityStatus() == null ? QualityStatus.INCOMPLETE : execution.getQualityStatus()).name();
    }

    private boolean crashed(Execution execution) {
        return execution.getQualitySeverities() == null || execution.getQualitySeverities().isEmpty() || execution.getQualityThresholds() == null || execution.getQualityThresholds().isEmpty();
    }

    private ExecutionWithHandlingCountsDTO toDtoWithAggregate(Execution execution) {
        return mapper.map(execution, ExecutionWithHandlingCountsDTO.class, (entity, dto) -> dto.setScenarioCounts(executionHistoryService.getExecutedScenarioHandlingCountsFor(entity)));
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return latest blocking and eligible executions for each branch
     */
    public List<ExecutionDTO> getLatestEligibleVersions(long projectId) {
        return mapper.mapCollection(executionRepository.getLatestEligibleVersionsByProjectId(projectId), ExecutionDTO.class);
    }

    /**
     * Unzip the given multipart file and launch an indexation of this execution for the given project's cycle.
     *
     * @param projectId the id of project which the execution belongs to
     * @param projectCode      the code of the project
     * @param branch    the branch of the current cycle for this execution
     * @param cycle     the cycle for this execution
     * @param zipFile   the execution to index
     * @throws IllegalArgumentException if the project doesn't use the file system indexer or the cycle doesn't exists.
     * @throws IOException              if the zip file can't be unzipped.
     */
    public void uploadExecutionReport(long projectId, String projectCode, String branch, String cycle, MultipartFile zipFile) throws IOException {
        CycleDefinition cycleDefinition = cycleDefinitionRepository.findByProjectIdAndBranchAndName(projectId, branch, cycle)
                .orElseThrow(() -> new IllegalArgumentException("The branch or cycle for this project doesn't exists."));

        String path = settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH)
                .replace(Settings.PROJECT_VARIABLE, projectCode)
                .replace(Settings.BRANCH_VARIABLE, branch)
                .replace(Settings.CYCLE_VARIABLE, cycle);
        File destinationDirectory = new File(path, "incoming");
        String buildInformationFilePath = settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH);
        List<File> executionDirectories = unzipExecutions(destinationDirectory, zipFile, buildInformationFilePath);
        launchExecutionDirectoriesProcessingThread(projectId, executionDirectories, cycleDefinition);
    }

    /**
     * Process the execution directories asynchronously
     * @param projectId the project id
     * @param executionDirectories the execution directories
     * @param cycleDefinition the cycle definition
     */
    public synchronized void launchExecutionDirectoriesProcessingThread(
                                                                        Long projectId,
                                                                        List<File> executionDirectories,
                                                                        CycleDefinition cycleDefinition) {
        Thread processExecutionDirectoriesThread = new Thread(() -> {
            LOG.info("EXECUTION|Processing execution files in a new thread");
            for (final File executionDirectory : executionDirectories) {
                try {
                    processSpecificDirectory(cycleDefinition, executionDirectory);
                } catch (Exception e) {
                    LOG.warn("EXECUTION|A problem occurred while indexing this execution [{}]", executionDirectory.getPath(), e);
                } finally {
                    LOG.info("EXECUTION|Cleaning the incoming folder: {}", executionDirectory.getAbsolutePath());
                    cleanExecutionFiles(projectId, executionDirectory);
                }
            }
        });
        processExecutionDirectoriesThread.start();
    }

    public void processSpecificDirectory(CycleDefinition cycleDefinition, File executionDirectory) {
        LOG.info("EXECUTION|Received new execution report in {}", executionDirectory.getAbsolutePath());

        PlannedIndexation plannedIndexation = new PlannedIndexation(cycleDefinition, executionDirectory);
        executionIndexerService.indexExecution(plannedIndexation);
    }

    List<File> unzipExecutions(File destinationDirectory, MultipartFile zipFile, String buildInformationFilePath) throws IOException {
        Files.createDirectories(destinationDirectory.toPath());
        this.archiveService.unzip(zipFile, destinationDirectory);
        return retrieveAllExecutionDirectories(destinationDirectory, buildInformationFilePath);
    }

    List<File> retrieveAllExecutionDirectories(File file, String buildInformationFilePath) {
        if (ArrayUtils.isEmpty(file.list())) {
            LOG.warn("EXECUTION|No entries found in the zip file {}", file.getAbsolutePath());
            return new ArrayList<>();
        }

        if (isExecutionDirectory(file, buildInformationFilePath)) {
            return Collections.singletonList(file);
        }

        List<File> executionDirectories = new ArrayList<>();
        File[] entries = file.listFiles();
        if (null != entries) {
            executionDirectories = Arrays.asList(entries).stream()
                    .filter(f -> isExecutionDirectory(f, buildInformationFilePath))
                    .toList();
        }
        return executionDirectories;
    }

    boolean isExecutionDirectory(File file, String buildInformationFilePath) {
        return file.isDirectory() && file.getName().matches("[0-9]+")
                && new File(file, buildInformationFilePath).exists();
    }

    /**
     * If enabled in settings, delete the directory containing the files related to the indexed execution
     *
     * @param projectId the project id
     * @param executionDirectory the directory containing the files related to the indexed execution
     */
    private void cleanExecutionFiles(Long projectId, File executionDirectory) {
        if (settingService.getBoolean(projectId, Settings.EXECUTION_INDEXER_FILE_DELETE_AFTER_INDEXING_AS_DONE)) {
            try {
                FileUtils.deleteDirectory(executionDirectory);
            } catch (IOException e) {
                LOG.warn("EXECUTION|The directory [{}] wasn't deleted due to an error", executionDirectory.getAbsolutePath(), e);
            }
        }
    }
}
