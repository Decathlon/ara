package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Execution and its DTO ExecutionHistoryPointDTO.
 */
@Mapper(uses = { QualityThresholdMapper.class, QualitySeverityMapper.class })
public interface ExecutionHistoryPointMapper extends EntityMapper<ExecutionHistoryPointDTO, Execution> {

    // All methods are parameterized for EntityMapper

}
