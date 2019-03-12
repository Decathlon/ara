package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsDTO;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Problem and its DTO ProblemWithPatternsDTO.
 */
@Mapper(uses = { ProblemPatternMapper.class }, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ProblemWithPatternsMapper extends EntityMapper<ProblemWithPatternsDTO, Problem> {

    // All methods are parameterized for EntityMapper

}
