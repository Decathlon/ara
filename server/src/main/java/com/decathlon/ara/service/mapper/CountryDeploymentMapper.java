package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.service.dto.countrydeployment.CountryDeploymentDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity CountryDeployment and its DTO CountryDeploymentDTO.
 */
@Mapper
public interface CountryDeploymentMapper extends EntityMapper<CountryDeploymentDTO, CountryDeployment> {

    // All methods are parameterized for EntityMapper

}
