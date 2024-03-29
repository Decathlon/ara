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

package com.decathlon.ara.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.projection.FirstAndLastProblemOccurrence;
import com.decathlon.ara.repository.ProblemRepository;

/**
 * Service for updating de-normalized fields of Problem.
 */
@Service
@Transactional
public class ProblemDenormalizationService {

    private final ProblemRepository problemRepository;

    public ProblemDenormalizationService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public void updateFirstAndLastSeenDateTimes(Collection<Problem> problems) {
        final List<FirstAndLastProblemOccurrence> occurrences =
                problemRepository.findFirstAndLastProblemOccurrences(problems);

        for (Problem problem : problems) {
            final Optional<FirstAndLastProblemOccurrence> occurrence = occurrences.stream()
                    .filter(o -> problem.getId().equals(o.getProblemId()))
                    .findFirst();
            problem.setFirstSeenDateTime(occurrence
                    .map(FirstAndLastProblemOccurrence::getFirstSeenDateTime)
                    .orElse(null));
            problem.setLastSeenDateTime(occurrence
                    .map(FirstAndLastProblemOccurrence::getLastSeenDateTime)
                    .orElse(null));
        }
    }

}
