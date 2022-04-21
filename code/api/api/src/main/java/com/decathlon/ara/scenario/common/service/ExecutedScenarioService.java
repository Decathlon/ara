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

package com.decathlon.ara.scenario.common.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.scenario.cucumber.util.ScenarioExtractorUtil;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.request.ExecutedScenarioHistoryInputDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing ExecutedScenario.
 */
@Service
@Transactional
public class ExecutedScenarioService {

    private final ExecutedScenarioRepository executedScenarioRepository;

    private final ErrorRepository errorRepository;

    private final FunctionalityRepository functionalityRepository;

    private final GenericMapper mapper;

    public ExecutedScenarioService(ExecutedScenarioRepository executedScenarioRepository,
            ErrorRepository errorRepository, FunctionalityRepository functionalityRepository,
            GenericMapper mapper) {
        this.executedScenarioRepository = executedScenarioRepository;
        this.errorRepository = errorRepository;
        this.functionalityRepository = functionalityRepository;
        this.mapper = mapper;
    }

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
                input.getRunTypeCode(),
                input.getDuration());

        final List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO> dtoList =
                mapper.mapCollection(executedScenarios, ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO.class);

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
        return mapper.map(executedScenario, ExecutedScenarioDTO.class);
    }

    private void assignProblemsToErrors(List<ExecutedScenario> executedScenarios, List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO> dtoList) {
        // Error has problemPatterns, leading to problems, but ErrorWithProblemsDTO directly has a list of problems: fill list manually
        Map<Error, List<Problem>> errorsProblems = errorRepository.getErrorsProblems(flattenErrors(executedScenarios));
        for (ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO executedScenarioDto : dtoList) {
            for (ErrorWithProblemsDTO errorDto : executedScenarioDto.getErrors()) {
                errorDto.setProblems(mapper.mapCollection(getErrorProblems(errorsProblems, errorDto.getId()), ProblemDTO.class));
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
                .toList();
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
