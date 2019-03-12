package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Functionality and its DTO FunctionalityDTO.
 */
@Mapper
public interface FunctionalityMapper extends EntityMapper<FunctionalityDTO, Functionality> {

    // All methods are parameterized for EntityMapper

}
