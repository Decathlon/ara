package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity CycleDefinition and its DTO CycleDefinitionDTO.
 */
@Mapper
public interface CycleDefinitionMapper extends EntityMapper<CycleDefinitionDTO, CycleDefinition> {

    // All methods are parameterized for EntityMapper

}
