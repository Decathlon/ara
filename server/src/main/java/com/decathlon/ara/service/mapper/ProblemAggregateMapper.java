package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.projection.ProblemAggregate;
import com.decathlon.ara.service.dto.problem.ProblemAggregateDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Team and its DTO TeamDTO.
 */
@Mapper
public interface ProblemAggregateMapper extends EntityMapper<ProblemAggregateDTO, ProblemAggregate> {

    // All methods are parameterized for EntityMapper

}
