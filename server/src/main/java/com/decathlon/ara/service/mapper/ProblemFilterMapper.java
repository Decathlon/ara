package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.filter.ProblemFilter;
import com.decathlon.ara.service.dto.problem.ProblemFilterDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity ProblemFilter and its DTO ProblemFilterDTO.
 */
@Mapper
public interface ProblemFilterMapper extends EntityMapper<ProblemFilterDTO, ProblemFilter> {

    // All methods are parameterized for EntityMapper

}
