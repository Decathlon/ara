package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity ExecutedScenario and its DTO ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO.
 */
@Mapper(uses = { ExecutionMapper.class, ErrorWithProblemsMapper.class })
public interface ExecutedScenarioWithTeamIdsAndErrorsAndProblemsMapper extends EntityMapper<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO, ExecutedScenario> {

    // All methods are parameterized for EntityMapper

}
