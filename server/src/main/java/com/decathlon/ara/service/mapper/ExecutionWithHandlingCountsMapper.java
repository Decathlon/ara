package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.service.dto.execution.ExecutionWithHandlingCountsDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Execution and its DTO ExecutionWithHandlingCountsDTO.
 */
@Mapper(uses = { QualityThresholdMapper.class, QualitySeverityMapper.class })
public interface ExecutionWithHandlingCountsMapper extends EntityMapper<ExecutionWithHandlingCountsDTO, Execution> {

    // All methods are parameterized for EntityMapper

}
