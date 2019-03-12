package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Error and its DTO ErrorWithExecutedScenarioAndRunAndExecutionDTO.
 */
@Mapper(uses = { ExecutionMapper.class })
public interface ErrorWithExecutedScenarioAndRunAndExecutionMapper extends EntityMapper<ErrorWithExecutedScenarioAndRunAndExecutionDTO, Error> {

    // All methods are parameterized for EntityMapper

}
