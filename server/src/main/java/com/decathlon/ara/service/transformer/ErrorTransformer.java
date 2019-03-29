package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Error.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
public class ErrorTransformer {

    @Autowired
    private ProblemTransformer problemTransformer;

    /**
     * Transform the given Error DO to a ErrorWithProblemsDTO object.
     * <p>
     * Returns an empty ErrorWithProblemsDTO if the parameter is null.
     *
     * @param error the DO to transform
     * @return the result DTO.
     */
    ErrorWithProblemsDTO toDto(Error error) {
        ErrorWithProblemsDTO result = new ErrorWithProblemsDTO();
        result.setId(0L);
        if (null != error) {
            result.setId(error.getId());
            result.setStep(error.getStep());
            result.setStepDefinition(error.getStepDefinition());
            result.setStepLine(error.getStepLine());
            result.setException(error.getException());

            List<Problem> problems = error.getProblemPatterns().stream()
                    .map(ProblemPattern::getProblem)
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());
            result.setProblems(problemTransformer.toDtos(problems));
        }
        return result;
    }

    /**
     * Transform the given list of Error DO to a list of ErrorWithProblemsDTO.
     * <p>
     * Returns an empty list if the parameter is null or empty.
     *
     * @param errors the list of DO to transform
     * @return the list of resulting DTO.
     */
    List<ErrorWithProblemsDTO> toDtos(Collection<Error> errors) {
        if (null == errors) {
            return new ArrayList<>();
        }
        return errors.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
