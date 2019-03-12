package com.decathlon.ara.service;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.decathlon.ara.service.dto.run.RunDTO;
import com.decathlon.ara.service.dto.run.RunWithQualitiesDTO;
import com.decathlon.ara.service.dto.run.ExecutedScenarioHandlingCountsDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.ExecutionHistoryPointMapper;
import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import com.decathlon.ara.report.util.ScenarioExtractorUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionHistoryService {

    @NonNull
    private final ExecutionRepository executionRepository;

    @NonNull
    private final ExecutionHistoryPointMapper executionHistoryPointMapper;

    @NonNull
    private final ExecutedScenarioRepository executedScenarioRepository;

    @NonNull
    private final FunctionalityRepository functionalityRepository;

    @NonNull
    private final SeverityService severityService;

    /**
     * Given a project, find latest DONE and not DISCARDED execution for all cycles, with previous and next IDs to be
     * able to browse the history of each cycle. Each execution has executed-scenario counts for each run, severity and
     * team, where the number of passed, handled, unhandled and total executed-scenarios are provided.
     *
     * @param projectId the ID of the project in which to work
     * @return executions ordered by projectId, branchPosition, branchName and cycleName, with NEXT/PREVIOUS ids and
     * scenario counts
     */
    public List<ExecutionHistoryPointDTO> getLatestExecutionHistories(long projectId) {
        return computeExecutionHistoryPointDTOS(projectId, executionRepository.findLatestOfEachCycleByProjectId(projectId));
    }

    /**
     * Get one execution, with previous and next IDs to be able to browse the history of each cycle.
     * The execution has executed-scenario counts for each run, severity and team, where the number of passed, handled,
     * unhandled and total executed-scenarios are provided.
     *
     * @param projectId the ID of the project in which to work
     * @param id the ID of the execution to retrieve
     * @return the execution, with scenario counts
     * @throws NotFoundException when the execution cannot be found
     */
    public ExecutionHistoryPointDTO getExecution(long projectId, long id) throws NotFoundException {
        final Execution execution = executionRepository.findByProjectIdAndId(projectId, id);
        if (execution == null) {
            throw new NotFoundException(Messages.NOT_FOUND_EXECUTION, Entities.EXECUTION);
        }
        return computeExecutionHistoryPointDTOS(projectId, Collections.singletonList(execution)).get(0);
    }

    private List<ExecutionHistoryPointDTO> computeExecutionHistoryPointDTOS(long projectId, List<Execution> executions) {
        final List<ExecutionHistoryPointDTO> dtoList = executionHistoryPointMapper.toDto(executions);

        Map<Long, Long> functionalityTeamIds = functionalityRepository.getFunctionalityTeamIds(projectId);

        final Set<Long> runIds = dtoList.stream()
                .flatMap(executionHistoryPointDTO -> executionHistoryPointDTO.getRuns().stream())
                .map(RunDTO::getId)
                .collect(Collectors.toSet());
        final List<ExecutedScenarioWithErrorAndProblemJoin> allErrorCounts = executedScenarioRepository.findAllErrorAndProblemCounts(runIds);

        List<Execution> nextExecutions = executionRepository.findNextOf(executions);
        List<Execution> previousExecutions = executionRepository.findPreviousOf(executions);

        for (ExecutionHistoryPointDTO dto : dtoList) {
            fillExecutionHistoryPoint(dto, allErrorCounts, functionalityTeamIds);
            dto.setNextId(findExecutionByBranchAndName(nextExecutions, dto.getBranch(), dto.getName()));
            dto.setPreviousId(findExecutionByBranchAndName(previousExecutions, dto.getBranch(), dto.getName()));
        }
        return dtoList;
    }

    private Long findExecutionByBranchAndName(List<Execution> executions, String branch, String name) {
        return executions.stream()
                .filter(c -> c.getBranch().equals(branch) && c.getName().equals(name))
                .map(Execution::getId)
                .findFirst()
                .orElse(null);
    }

    public ExecutedScenarioHandlingCountsDTO getExecutedScenarioHandlingCountsFor(Execution execution) {
        final Set<Long> runIds = execution.getRuns().stream()
                .map(Run::getId)
                .collect(Collectors.toSet());

        ExecutedScenarioHandlingCountsDTO executedScenarioHandlingCounts = new ExecutedScenarioHandlingCountsDTO();
        for (ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin : executedScenarioRepository.findAllErrorAndProblemCounts(runIds)) {
            incrementCountsByHandling(executedScenarioJoin, executedScenarioHandlingCounts);
        }
        return executedScenarioHandlingCounts;
    }

    private void fillExecutionHistoryPoint(ExecutionHistoryPointDTO execution, List<ExecutedScenarioWithErrorAndProblemJoin> allErrorCounts, Map<Long, Long> functionalityTeamIds) {
        List<SeverityDTO> activeSeverities = execution.getQualitySeverities().stream()
                .map(QualitySeverityDTO::getSeverity)
                .collect(Collectors.toList());
        String defaultSeverityCode = severityService.getDefaultSeverityCode(activeSeverities);

        for (RunWithQualitiesDTO run : execution.getRuns()) {
            fillQualities(allErrorCounts, run, functionalityTeamIds, defaultSeverityCode);
        }
    }

    /**
     * Fill quality aggregates of a RunWithQualitiesDTO (counts of scenarios per severity and per team+severity).
     *
     * @param allExecutedScenarioJoin all executed-scenarios of the the given run, joined with their errors and problems
     * @param run this method will fill {@code qualitiesPerSeverity} and {@code qualitiesPerTeamAndSeverity} in it
     * @param functionalityTeamIds a map of key functionality.id and value functionality.teamId
     * @param defaultSeverityCode the default severity code to use if the scenario has none
     */
    void fillQualities(List<ExecutedScenarioWithErrorAndProblemJoin> allExecutedScenarioJoin,
                       RunWithQualitiesDTO run,
                       Map<Long, Long> functionalityTeamIds,
                       String defaultSeverityCode) {
        run.setQualitiesPerSeverity(new HashMap<>());
        run.setQualitiesPerTeamAndSeverity(new HashMap<>());

        final List<ExecutedScenarioWithErrorAndProblemJoin> allExecutedScenarioJoinOfRun = allExecutedScenarioJoin
                .stream()
                .filter(e -> run.getId().longValue() == e.getRunId())
                .collect(Collectors.toList());
        for (ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin : allExecutedScenarioJoinOfRun) {
            // Count the scenario for its severity and for global
            addScenario(executedScenarioJoin, run.getQualitiesPerSeverity(), defaultSeverityCode);

            // Do the same for the teams of the scenario
            final List<Long> functionalityIds = ScenarioExtractorUtil
                    .extractFunctionalityIds(executedScenarioJoin.getName());
            final Set<Long> teamIds = functionalityIds.stream()
                    .map(functionalityTeamIds::get)
                    .filter(Objects::nonNull) // Unknown functionality IDs have null team IDs
                    .collect(Collectors.toSet());
            if (teamIds.isEmpty()) {
                addScenarioForTeamAndSeverity(run, defaultSeverityCode, executedScenarioJoin, Team.NOT_ASSIGNED.getId());
            }
            for (Long teamId : teamIds) {
                addScenarioForTeamAndSeverity(run, defaultSeverityCode, executedScenarioJoin, teamId);
            }
        }
    }

    private void addScenarioForTeamAndSeverity(RunWithQualitiesDTO run,
                                               String defaultSeverityCode,
                                               ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin,
                                               Long teamId) {
        Map<String, ExecutedScenarioHandlingCountsDTO> countsWithErrors = run.getQualitiesPerTeamAndSeverity()
                .computeIfAbsent(teamId.toString(), k -> new HashMap<>());
        addScenario(executedScenarioJoin, countsWithErrors, defaultSeverityCode);
    }

    /**
     * Given an {@link ExecutedScenario}+{@link Error}+{@link Problem} join, increment the given handling-count for the
     * severity of the scenario (using the default severity if needed) AND the global "ALL" virtual-severity.
     *
     * @param executedScenarioJoin information to be used to deduce the handling state of the executed-scenario
     * @param qualitiesPerSeverity the counts in which to increment the handling of the executed-scenario, by severity
     *                             code (the key of the map)
     * @param defaultSeverityCode  the default severity code to use if the scenario has none (can be null, in which
     *                             case, only "ALL" counts will be incremented)
     */
    void addScenario(ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin,
                     Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity,
                     String defaultSeverityCode) {
        final String effectiveSeverityCode = (StringUtils.isEmpty(executedScenarioJoin.getSeverity()) ? defaultSeverityCode : executedScenarioJoin.getSeverity());
        if (effectiveSeverityCode != null) { // when no mandatory severity was asked to run => no default severity to find => null
            addScenarioForSeverity(executedScenarioJoin, qualitiesPerSeverity, effectiveSeverityCode);
        }
        addScenarioForSeverity(executedScenarioJoin, qualitiesPerSeverity, Severity.ALL.getCode());
    }

    /**
     * Given an {@link ExecutedScenario}+{@link Error}+{@link Problem} join, increment the given handling-count for the
     * severity.
     *
     * @param executedScenarioJoin information to be used to deduce the handling state of the executed-scenario
     * @param qualitiesPerSeverity the counts in which to increment the handling of the executed-scenario, by severity
     *                             code (the key of the map)
     * @param severityCode         the key of the map entry to increment (entry will be created if nonexistent)
     */
    void addScenarioForSeverity(ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin,
                                Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity,
                                String severityCode) {
        ExecutedScenarioHandlingCountsDTO executedScenarioHandlingCounts = qualitiesPerSeverity
                .computeIfAbsent(severityCode, k -> new ExecutedScenarioHandlingCountsDTO());

        incrementCountsByHandling(executedScenarioJoin, executedScenarioHandlingCounts);
    }

    /**
     * Given an {@link ExecutedScenario}+{@link Error}+{@link Problem} join, increment the given handling-count.
     *
     * @param executedScenarioJoin information to be used to deduce the handling state of the executed-scenario
     * @param executedScenarioHandlingCounts the counts in which to increment the handling of the executed-scenario
     */
    private void incrementCountsByHandling(ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin,
                                           ExecutedScenarioHandlingCountsDTO executedScenarioHandlingCounts) {
        if (executedScenarioJoin.getHandledCount() > 0) {
            executedScenarioHandlingCounts.setHandled(executedScenarioHandlingCounts.getHandled() + 1);
        } else if (executedScenarioJoin.getUnhandledCount() > 0) {
            executedScenarioHandlingCounts.setUnhandled(executedScenarioHandlingCounts.getUnhandled() + 1);
        } else {
            executedScenarioHandlingCounts.setPassed(executedScenarioHandlingCounts.getPassed() + 1);
        }
    }

}
