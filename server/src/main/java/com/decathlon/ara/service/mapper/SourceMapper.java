package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.service.dto.source.SourceDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Source and its DTO SourceDTO.
 */
@Mapper
public interface SourceMapper extends EntityMapper<SourceDTO, Source> {

    // All methods are parameterized for EntityMapper

}
