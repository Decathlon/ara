package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Run;
import com.decathlon.ara.service.dto.run.RunWithExecutionDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Run and its DTO RunWithExecutionDTO.
 */
@Mapper(uses = { ExecutionMapper.class })
public interface RunWithExecutionMapper extends EntityMapper<RunWithExecutionDTO, Run> {

    // All methods are parameterized for EntityMapper

}
