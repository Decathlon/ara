package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Type;
import com.decathlon.ara.service.dto.type.TypeWithSourceCodeDTO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper for the entity Type and its DTO TypeWithSourceCodeDTO.
 */
@Mapper
public interface TypeWithSourceCodeMapper extends EntityMapper<TypeWithSourceCodeDTO, Type> {

    @Override
    @Mapping(source = "sourceCode", target = "source.code")
    Type toEntity(TypeWithSourceCodeDTO dto);

    @Override
    @Mapping(source = "source.code", target = "sourceCode")
    TypeWithSourceCodeDTO toDto(Type entity);

    @AfterMapping
    default void removeEmptySource(@MappingTarget Type entity) {
        if (StringUtils.isEmpty(entity.getSource().getCode())) {
            entity.setSource(null);
        }
    }

}
