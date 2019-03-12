package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.service.dto.error.ErrorDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Error and its DTO ErrorDTO.
 */
@Mapper
public interface ErrorMapper extends EntityMapper<ErrorDTO, Error> {

    // All methods are parameterized for EntityMapper

}
