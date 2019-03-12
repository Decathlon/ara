package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsAndAggregateTDO;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

@Mapper(uses = { ProblemPatternMapper.class }, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ProblemWithPatternsAndAggregateMapper extends EntityMapper<ProblemWithPatternsAndAggregateTDO, Problem> {

}
