/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemOccurrence;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

            List<Problem> problems = error.getProblemOccurrences().stream()
                    .map(ProblemOccurrence::getProblemPattern)
                    .map(ProblemPattern::getProblem)
                    .sorted(Comparator.nullsLast(Problem::compareTo))
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
