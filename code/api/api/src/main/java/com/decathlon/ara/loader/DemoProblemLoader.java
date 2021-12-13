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

package com.decathlon.ara.loader;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.ProblemService;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;

/**
 * Service for loading problems into the Demo project.
 */
@Service
@Transactional
public class DemoProblemLoader {

    private final ProblemService problemService;

    private final RootCauseRepository rootCauseRepository;

    public DemoProblemLoader(ProblemService problemService, RootCauseRepository rootCauseRepository) {
        this.problemService = problemService;
        this.rootCauseRepository = rootCauseRepository;
    }

    public void createProblems(long projectId, List<TeamDTO> teams) throws BadRequestException {
        final Long rootCauseId = rootCauseRepository.findByProjectIdAndName(projectId, "Fragile test").getId();

        problemService.create(projectId, new ProblemWithPatternsDTO(
                "Cannot add several products to cart",
                "Whatever the number of products added to cart, only one remains.",
                teams.get(2), // Buy
                "42",
                new RootCauseDTO(rootCauseId),
                Collections.singletonList(
                        new ProblemPatternDTO("^the cart page shows (\\d+) product[s]?$", "java.lang.AssertionError: expected:<[%]> but was:<[1]>"))));
    }

}
