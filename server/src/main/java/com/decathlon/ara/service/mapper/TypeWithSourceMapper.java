package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Type;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Type and its DTO TypeWithSourceDTO.
 */
@Mapper
public interface TypeWithSourceMapper extends EntityMapper<TypeWithSourceDTO, Type> {

    // All methods are parameterized for EntityMapper

}
