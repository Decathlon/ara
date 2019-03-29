package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper for the entity Error and its DTO ErrorWithProblemsDTO.
 */
@Mapper
public abstract class ErrorWithProblemsMapper implements EntityMapper<ErrorWithProblemsDTO, Error> {

    @Autowired
    private ProblemMapper problemMapper;

    /**
     *
     * @param error
     * @param errorDto
     * @deprecated
     * @see com.decathlon.ara.service.transformer.ProblemTransformer#toDtos(Collection)
     */
    @Deprecated
    @AfterMapping
    protected void populateProblems(Error error, @MappingTarget ErrorWithProblemsDTO errorDto) {
        final List<ProblemDTO> problems = problemMapper.toDto(
                error.getProblemPatterns()
                        .stream()
                        .map(ProblemPattern::getProblem)
                        .sorted() // By name, to display them in a consistent fashion
                        .distinct() // A problem can have two patterns matching the same error
                        .collect(Collectors.toList()));
        errorDto.setProblems(problems.isEmpty() ? null : problems); // null will not be serialized to JSON
    }

}
