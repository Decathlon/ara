package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.service.dto.country.CountryDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Country and its DTO CountryDTO.
 */
@Mapper
public interface CountryMapper extends EntityMapper<CountryDTO, Country> {

    // All methods are parameterized for EntityMapper

}
