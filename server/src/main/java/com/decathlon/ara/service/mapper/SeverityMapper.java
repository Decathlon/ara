package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Severity and its DTO SeverityDTO.
 */
@Mapper
public interface SeverityMapper extends EntityMapper<SeverityDTO, Severity> {

    // All methods are parameterized for EntityMapper

}
