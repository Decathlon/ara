package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.service.dto.problem.ProblemWithAggregateDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Problem and its DTO ProblemWithAggregateDTO.
 */
@Mapper
public interface ProblemWithAggregateMapper extends EntityMapper<ProblemWithAggregateDTO, Problem> {

    // All methods are parameterized for EntityMapper

}
