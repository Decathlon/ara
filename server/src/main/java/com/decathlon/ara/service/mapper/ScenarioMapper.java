package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.service.dto.scenario.ScenarioDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Scenario and its DTO ScenarioDTO.
 */
@Mapper
public interface ScenarioMapper extends EntityMapper<ScenarioDTO, Scenario> {

    // All methods are parameterized for EntityMapper

}
