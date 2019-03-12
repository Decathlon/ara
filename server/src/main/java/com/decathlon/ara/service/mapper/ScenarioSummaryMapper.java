package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.projection.ScenarioSummary;
import com.decathlon.ara.service.dto.scenario.ScenarioSummaryDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity ScenarioSummary and its DTO ScenarioSummaryDTO.
 */
@Mapper
public interface ScenarioSummaryMapper extends EntityMapper<ScenarioSummaryDTO, ScenarioSummary> {

    // All methods are parameterized for EntityMapper

}
