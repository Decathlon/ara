package com.decathlon.ara.service;

import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.RunRepository;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.DistinctStatisticsDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.CountryMapper;
import com.decathlon.ara.service.mapper.ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsMapper;
import com.decathlon.ara.service.mapper.ErrorWithExecutedScenarioAndRunAndExecutionMapper;
import com.decathlon.ara.service.mapper.ProblemMapper;
import com.decathlon.ara.service.mapper.ProblemPatternMapper;
import com.decathlon.ara.service.mapper.TypeWithSourceMapper;
import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Error.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErrorService {

    @NonNull
    private final ErrorRepository errorRepository;

    @NonNull
    private final ExecutedScenarioRepository executedScenarioRepository;

    @NonNull
    private final ExecutionRepository executionRepository;

    @NonNull
    private final RunRepository runRepository;

    @NonNull
    private final ErrorWithExecutedScenarioAndRunAndExecutionMapper errorWithExecutedScenarioAndRunAndExecutionMapper;

    @NonNull
    private final ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsMapper errorWithExecutedScenarioAndRunAndExecutionAndProblemsMapper;

    @NonNull
    private final ProblemMapper problemMapper;

    @NonNull
    private final ProblemService problemService;

    @NonNull
    private final ProblemPatternMapper problemPatternMapper;

    @NonNull
    private final CountryMapper countryMapper;

    @NonNull
    private final TypeWithSourceMapper typeWithSourceMapper;

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
        return errorWithExecutedScenarioAndRunAndExecutionMapper.toDto(error);
    }

    @Transactional(readOnly = true)
    public Page<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO> findMatchingErrors(long projectId, ProblemPatternDTO pattern, Pageable pageable) {
        Page<Error> errors = errorRepository.findMatchingErrors(projectId, problemPatternMapper.toEntity(pattern), pageable);

        Map<Error, List<Problem>> errorsProblems = errorRepository.getErrorsProblems(errors.getContent());

        Page<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO> errorDtoPage = errors.map(errorWithExecutedScenarioAndRunAndExecutionAndProblemsMapper::toDto);
        errorDtoPage.getContent().forEach(errorDto -> {
            Optional<List<Problem>> errorProblems = errorsProblems.entrySet().stream()
                    .filter(entry -> entry.getKey().getId().equals(errorDto.getId()))
                    .map(Map.Entry::getValue)
                    .findFirst();

            if (errorProblems.isPresent()) {
                List<ProblemDTO> problemDtos = new ArrayList<>();
                for (Problem problem : errorProblems.get()) {
                    ProblemDTO dto = problemMapper.toDto(problem);
                    dto.setDefectUrl(problemService.retrieveDefectUrl(problem));
                    problemDtos.add(dto);
                }
                errorDto.setProblems(problemDtos);
            } else {
                errorDto.setProblems(null);
            }
        });

        return errorDtoPage;
    }

    @Transactional(readOnly = true)
    public DistinctStatisticsDTO findDistinctProperties(long projectId, String property) {
        DistinctStatisticsDTO distinctValues = new DistinctStatisticsDTO();

        if ("releases".equals(property)) {
            distinctValues.setReleases(executionRepository.findDistinctReleaseByProjectId(projectId));
        }

        if ("countries".equals(property)) {
            distinctValues.setCountries(countryMapper.toDto(runRepository.findDistinctCountryByProjectId(projectId)));
        }

        if ("types".equals(property)) {
            distinctValues.setTypes(typeWithSourceMapper.toDto(runRepository.findDistinctTypeByProjectId(projectId)));
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

}
