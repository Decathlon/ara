package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.report.util.ScenarioExtractorUtil;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.request.ExecutedScenarioHistoryInputDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.mapper.ExecutedScenarioMapper;
import com.decathlon.ara.service.mapper.ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsMapper;
import com.decathlon.ara.service.mapper.ProblemMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing ExecutedScenario.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutedScenarioService {

    @NonNull
    private final ExecutedScenarioRepository executedScenarioRepository;

    @NonNull
    private final ErrorRepository errorRepository;

    @NonNull
    private final FunctionalityRepository functionalityRepository;

    @NonNull
    private final ExecutedScenarioMapper executedScenarioMapper;

    @NonNull
    private final ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsMapper executedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsMapper;

    @NonNull
    private final ProblemMapper problemMapper;

    /**
     * @param projectId the ID of the project in which to work
     * @param input     containing the mandatory cucumberId of the scenario to get history, and optional filter parameters
     * @return history of the execution of a scenario by its cucumberId
     * @throws BadRequestException if the mandatory cucumberId is null or empty
     */
    public List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO> findHistory(long projectId, ExecutedScenarioHistoryInputDTO input) throws BadRequestException {
        if (StringUtils.isEmpty(input.getCucumberId())) {
            throw new BadRequestException(Messages.RULE_EXECUTED_SCENARIO_HISTORY_MANDATORY_CUCUMBER_ID, Entities.EXECUTED_SCENARIO, "mandatory_cucumber_id");
        }

        final List<ExecutedScenario> executedScenarios = executedScenarioRepository.findHistory(
                projectId,
                input.getCucumberId(),
                input.getBranch(),
                input.getCycleName(),
                input.getCountryCode(),
                input.getRunTypeCode());

        final List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO> dtoList =
                executedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsMapper.toDto(executedScenarios);

        assignProblemsToErrors(executedScenarios, dtoList);
        assignTeamsToExecutedScenarios(projectId, dtoList);

        return dtoList;
    }

    /**
     * Return the basic informations of an executed scenario from its id.
     *
     * @param projectId          the project id of the executed scenario
     * @param executedScenarioId the id of the scenario
     * @return the basic information of the scenario
     */
    public ExecutedScenarioDTO findOne(long projectId, long executedScenarioId) {
        ExecutedScenario executedScenario = executedScenarioRepository.findOne(projectId, executedScenarioId);
        return executedScenarioMapper.toDto(executedScenario);
    }

    private void assignProblemsToErrors(List<ExecutedScenario> executedScenarios, List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO> dtoList) {
        // Error has problemPatterns, leading to problems, but ErrorWithProblemsDTO directly has a list of problems: fill list manually
        Map<Error, List<Problem>> errorsProblems = errorRepository.getErrorsProblems(flattenErrors(executedScenarios));
        for (ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO executedScenarioDto : dtoList) {
            for (ErrorWithProblemsDTO errorDto : executedScenarioDto.getErrors()) {
                errorDto.setProblems(problemMapper.toDto(getErrorProblems(errorsProblems, errorDto.getId())));
            }
        }
    }

    private void assignTeamsToExecutedScenarios(long projectId, List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO> executedScenarios) {
        final Map<Long, Long> functionalityTeamIds = functionalityRepository.getFunctionalityTeamIds(projectId);
        for (ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO executedScenario : executedScenarios) {
            executedScenario.setTeamIds(ScenarioExtractorUtil.extractFunctionalityIds(executedScenario.getName()).stream()
                    .map(functionalityTeamIds::get)
                    .filter(Objects::nonNull) // Unknown functionality IDs have null team IDs
                    .collect(Collectors.toSet()));
        }
    }

    private List<Error> flattenErrors(Collection<ExecutedScenario> executedScenarios) {
        return executedScenarios.stream()
                .flatMap(s -> s.getErrors().stream())
                .collect(Collectors.toList());
    }

    private List<Problem> getErrorProblems(Map<Error, List<Problem>> errorsProblems, Long errorId) {
        for (Map.Entry<Error, List<Problem>> entry : errorsProblems.entrySet()) {
            if (entry.getKey().getId().equals(errorId)) {
                return entry.getValue();
            }
        }
        return new ArrayList<>();
    }

}
