package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Run;
import com.decathlon.ara.service.dto.run.RunDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Run and its DTO RunDTO.
 */
@Mapper
public interface RunMapper extends EntityMapper<RunDTO, Run> {

    // All methods are parameterized for EntityMapper

}
