package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Run;
import com.decathlon.ara.service.dto.run.RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Run and its DTO RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO.
 */
@Mapper(uses = ExecutedScenarioWithTeamIdsAndErrorsAndProblemsMapper.class)
public interface RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsMapper extends EntityMapper<RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO, Run> {

    // All methods are parameterized for EntityMapper

}
