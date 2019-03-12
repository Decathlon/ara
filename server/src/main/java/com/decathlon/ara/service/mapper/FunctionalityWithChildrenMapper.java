package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.service.dto.functionality.FunctionalityWithChildrenDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Functionality and its DTO FunctionalityWithChildrenDTO.
 */
@Mapper
public interface FunctionalityWithChildrenMapper extends EntityMapper<FunctionalityWithChildrenDTO, Functionality> {

    // All methods are parameterized for EntityMapper

}
