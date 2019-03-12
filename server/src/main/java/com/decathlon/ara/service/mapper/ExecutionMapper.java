package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Execution and its DTO ExecutionDTO.
 */
@Mapper(uses = { QualityThresholdMapper.class, QualitySeverityMapper.class })
public interface ExecutionMapper extends EntityMapper<ExecutionDTO, Execution> {

    // All methods are parameterized for EntityMapper

}
