package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity ExecutedScenario and its DTO ExecutedScenarioDTO.
 */
@Mapper
public interface ExecutedScenarioMapper extends EntityMapper<ExecutedScenarioDTO, ExecutedScenario> {

    // All methods are parameterized for EntityMapper

}
