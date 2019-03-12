package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity RootCause and its DTO RootCauseDTO.
 */
@Mapper
public interface RootCauseMapper extends EntityMapper<RootCauseDTO, RootCause> {

    // All methods are parameterized for EntityMapper

}
