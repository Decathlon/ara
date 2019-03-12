package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Error and its DTO ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO.
 */
@Mapper(uses = { ExecutionMapper.class, ErrorWithProblemsMapper.class })
public interface ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsMapper extends EntityMapper<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO, Error> {

    // All methods are parameterized for EntityMapper

}
