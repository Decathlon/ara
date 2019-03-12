package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Error and its DTO ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO.
 */
@Mapper(uses = { ExecutionMapper.class })
public interface ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsMapper extends EntityMapper<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO, ExecutedScenario> {

    // All methods are parameterized for EntityMapper

}
