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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.service.dto.problem.ProblemDTO;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Problem.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
public class ProblemTransformer {

    @Autowired
    private TeamTransformer teamTransformer;

    @Autowired
    private RootCauseTransformer rootCauseTransformer;

    /**
     * Transform the given Problem DO to a ProblemDTO object.
     * <p>
     * Returns an empty ProblemDTO if the parameter is null.
     *
     * @param problem the DO to transform
     * @return the result DTO.
     */
    ProblemDTO toDto(Problem problem) {
        ProblemDTO result = new ProblemDTO();
        result.setId(0L);
        if (null != problem) {
            result.setId(problem.getId());
            result.setName(problem.getName());
            result.setComment(problem.getComment());
            result.setStatus(problem.getStatus());
            result.setEffectiveStatus(problem.getEffectiveStatus());
            result.setBlamedTeam(teamTransformer.toDto(problem.getBlamedTeam()));
            result.setDefectId(problem.getDefectId());
            result.setDefectExistence(problem.getDefectExistence());
            result.setClosingDateTime(problem.getClosingDateTime());
            result.setRootCause(rootCauseTransformer.toDto(problem.getRootCause()));
            result.setCreationDateTime(problem.getCreationDateTime());
            result.setFirstSeenDateTime(problem.getFirstSeenDateTime());
            result.setLastSeenDateTime(problem.getLastSeenDateTime());
        }
        return result;
    }

    /**
     * Transform the given list of Problem DO to a list of ProblemDTO.
     * <p>
     * Returns an empty list if the parameter is null or empty.
     *
     * @param problems the list of DO to transform
     * @return the list of resulting DTO.
     */
    public List<ProblemDTO> toDtos(Collection<Problem> problems) {
        if (null == problems) {
            return new ArrayList<>();
        }
        return problems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
