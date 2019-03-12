package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.service.dto.execution.ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Execution and its DTO ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO.
 */
@Mapper(uses = { QualityThresholdMapper.class, QualitySeverityMapper.class,
        RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsMapper.class })
public interface ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsMapper
        extends EntityMapper<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO, Execution> {

    // All methods are parameterized for EntityMapper

}
