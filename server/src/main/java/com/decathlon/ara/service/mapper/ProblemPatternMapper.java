package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity ProblemPattern and its DTO ProblemPatternDTO.
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ProblemPatternMapper extends EntityMapper<ProblemPatternDTO, ProblemPattern> {

    // All methods are parameterized for EntityMapper

}
