package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Problem and its DTO ProblemDTO.
 */
@Mapper
public interface ProblemMapper extends EntityMapper<ProblemDTO, Problem> {

    // All methods are parameterized for EntityMapper

}
