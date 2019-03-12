package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Run;
import com.decathlon.ara.service.dto.run.RunWithErrorsDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Run and its DTO RunWithErrorsDTO.
 */
@Mapper
public interface RunWithErrorsMapper extends EntityMapper<RunWithErrorsDTO, Run> {

    // All methods are parameterized for EntityMapper

}
