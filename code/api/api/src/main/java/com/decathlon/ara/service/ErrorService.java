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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemOccurrence;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ProblemOccurrenceRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.RunRepository;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.repository.custom.util.TransactionAppenderUtil;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.DistinctStatisticsDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing Error.
 */
@Service
@Transactional
public class ErrorService {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorService.class);

    private static final Sort ERROR_SORTING = Sort.by(Sort.Direction.ASC,
            "executedScenario.id",
            "stepLine");

    private final ErrorRepository errorRepository;

    private final ExecutedScenarioRepository executedScenarioRepository;

    private final ExecutionRepository executionRepository;

    private final RunRepository runRepository;

    private final ProblemService problemService;

    private final ProblemOccurrenceRepository problemOccurrenceRepository;

    private final ProblemPatternRepository problemPatternRepository;

    private final GenericMapper mapper;

    private final JpaCacheManager jpaCacheManager;

    private final TransactionAppenderUtil transactionAppenderUtil;

    @Autowired
    public ErrorService(ErrorRepository errorRepository, ExecutedScenarioRepository executedScenarioRepository,
            ExecutionRepository executionRepository, RunRepository runRepository,
            ProblemService problemService, ProblemOccurrenceRepository problemOccurrenceRepository,
            ProblemPatternRepository problemPatternRepository,
            GenericMapper mapper, JpaCacheManager jpaCacheManager, TransactionAppenderUtil transactionService) {
        this.errorRepository = errorRepository;
        this.executedScenarioRepository = executedScenarioRepository;
        this.executionRepository = executionRepository;
        this.runRepository = runRepository;
        this.problemService = problemService;
        this.problemOccurrenceRepository = problemOccurrenceRepository;
        this.problemPatternRepository = problemPatternRepository;
        this.mapper = mapper;
        this.jpaCacheManager = jpaCacheManager;
        this.transactionAppenderUtil = transactionService;
    }

    /**
     * Get one error by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @return the entity
     * @throws NotFoundException when the error cannot be found in the project
     */
    @Transactional(readOnly = true)
    public ErrorWithExecutedScenarioAndRunAndExecutionDTO findOne(long projectId, long id) throws NotFoundException {
        final Error error = errorRepository.findByProjectIdAndId(projectId, id);
        if (error == null) {
            throw new NotFoundException(Messages.NOT_FOUND_ERROR, Entities.ERROR);
        }
        return mapper.map(error, ErrorWithExecutedScenarioAndRunAndExecutionDTO.class);
    }

    @Transactional(readOnly = true)
    public Page<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO> findMatchingErrors(long projectId, ProblemPatternDTO pattern, Pageable pageable) {
        Pageable effectivePageable;
        if (pageable == null) {
            effectivePageable = PageRequest.of(0, 10, ERROR_SORTING);
        } else if (pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ERROR_SORTING);
        } else {
            effectivePageable = pageable;
        }

        Page<Error> errors = errorRepository.findByProjectIdAndProblemPattern(projectId, mapper.map(pattern, ProblemPattern.class), effectivePageable);

        Map<Error, List<Problem>> errorsProblems = errorRepository.getErrorsProblems(errors.getContent());

        return errors.map(error -> mapper.map(error, ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO.class, (errorEntity, errorDto) -> {
            List<Problem> problems = errorsProblems.get(errorEntity);
            if (problems != null) {
                errorDto.setProblems(mapper.mapCollection(problems, ProblemDTO.class, (problemEntity, problemDto) -> problemDto.setDefectUrl(problemService.retrieveDefectUrl(problemEntity))));
            }
        }));
    }

    @Transactional(readOnly = true)
    public DistinctStatisticsDTO findDistinctProperties(long projectId, String property) {
        DistinctStatisticsDTO distinctValues = new DistinctStatisticsDTO();

        if ("releases".equals(property)) {
            distinctValues.setReleases(executionRepository.findDistinctReleaseByProjectId(projectId));
        }

        if ("countries".equals(property)) {
            distinctValues.setCountries(mapper.mapCollection(runRepository.findDistinctCountryByProjectId(projectId), CountryDTO.class));
        }

        if ("types".equals(property)) {
            distinctValues.setTypes(mapper.mapCollection(runRepository.findDistinctTypeByProjectId(projectId), TypeWithSourceDTO.class));
        }

        if ("platforms".equals(property)) {
            distinctValues.setPlatforms(runRepository.findDistinctPlatformByProjectId(projectId));
        }

        if ("featureNames".equals(property)) {
            distinctValues.setFeatureNames(executedScenarioRepository.findDistinctFeatureNameByProjectId(projectId));
        }

        if ("featureFiles".equals(property)) {
            distinctValues.setFeatureFiles(executedScenarioRepository.findDistinctFeatureFileByProjectId(projectId));
        }

        if ("scenarioNames".equals(property)) {
            distinctValues.setScenarioNames(executedScenarioRepository.findDistinctNameByProjectId(projectId));
        }

        if ("steps".equals(property)) {
            distinctValues.setSteps(errorRepository.findDistinctStepByProjectId(projectId));
        }

        if ("stepDefinitions".equals(property)) {
            distinctValues.setStepDefinitions(errorRepository.findDistinctStepDefinitionByProjectId(projectId));
        }

        return distinctValues;
    }

    public Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> getErrors(List<ProblemPattern> problemPatterns, Pageable pageable) {
        Page<Error> errors = errorRepository.findDistinctByProblemOccurrencesProblemPatternIn(problemPatterns, pageable);
        if (errors == null) {
            return null;
        }
        return errors.map(error -> mapper.map(error, ErrorWithExecutedScenarioAndRunAndExecutionDTO.class));
    }

    /**
     * When new errors get indexed into ARA, this method will assign them existing problems if at least one of the
    * problems's patterns match the errors.
    *
    * @param projectId the ID of the project in which to work
    * @param errorIds  the IDs of the new errors that were just created
    * @return all problems that were assigned one of the given new errors by this method
    */

    public void assignPatternToErrors(long projectId, ProblemPattern pattern) {
        Page<Error> matchingErrors = errorRepository.findByProjectIdAndProblemPattern(projectId, pattern, Pageable.unpaged());

        List<ProblemOccurrence> problemOccurrences = new ArrayList<>();
        List<Long> matchingErrorIds = new ArrayList<>();
        for (Error error : matchingErrors) {
            problemOccurrences.add(new ProblemOccurrence(error, pattern));
            matchingErrorIds.add(error.getId());
        }

        transactionAppenderUtil.doAfterCommit(() -> jpaCacheManager.evictCollections(Error.PROBLEM_OCCURRENCES_COLLECTION_CACHE, matchingErrorIds));

        problemOccurrenceRepository.saveAll(problemOccurrences);

        LOG.info("PROBLEM|error|Inserted {} problemOccurrences", problemOccurrences.size());
    }

    /**
     * When new errors get indexed into ARA, this method will assign them existing problems if at least one of the
     * problems's patterns match the errors.
     *
     * @param projectId the ID of the project in which to work
     * @param errorIds  the IDs of the new errors that were just created
     * @return all problems that were assigned one of the given new errors by this method
     */
    public Set<Problem> autoAssignProblemsToNewErrors(long projectId, List<Long> errorIds) {
        Set<Problem> updatedProblems = new HashSet<>();
        List<ProblemOccurrence> problemOccurrences = new ArrayList<>();
        for (ProblemPattern pattern : problemPatternRepository.findAllByProjectId(projectId)) {
            List<Error> matchingError = errorRepository.findByProjectIdAndProblemPatternAndErrorIds(projectId, pattern, errorIds);
            if (!matchingError.isEmpty()) {
                updatedProblems.add(pattern.getProblem());
                for (Error error : matchingError) {
                    problemOccurrences.add(new ProblemOccurrence(error, pattern));
                }
            }
        }

        problemOccurrenceRepository.saveAll(problemOccurrences);

        LOG.info("PROBLEM|error|Inserted {} problemOccurrences", problemOccurrences.size());

        return updatedProblems;
    }

}
