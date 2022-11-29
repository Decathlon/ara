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

package com.decathlon.ara.web.rest;

import com.decathlon.ara.Entities;
import com.decathlon.ara.service.ErrorService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.DistinctStatisticsDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Unidentified Errors.
 */
@RestController
@RequestMapping(ErrorResource.PATH)
public class ErrorResource {

    private static final String NAME = Entities.ERROR;
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";
    public static final String PATHS = PATH + "/**";

    private static final String MATCHING = "/matching";
    public static final String MATCHING_PATH = PATH + MATCHING;

    private final ErrorService service;

    private final ProjectService projectService;

    public ErrorResource(ErrorService service, ProjectService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    /**
     * GET one entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the id of the entity to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the entity, or with status 404 (Not Found)
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ErrorWithExecutedScenarioAndRunAndExecutionDTO> getOne(
                                                                                 @PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.findOne(projectService.toId(projectCode), id));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET all errors matching a given pattern.
     *
     * @param projectCode the code of the project in which to work
     * @param pattern     the pattern to search in errors
     * @param pageable    the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @PostMapping(MATCHING)
    public ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO>> getMatchingErrors(
                                                                                                             @PathVariable String projectCode, @RequestBody ProblemPatternDTO pattern, Pageable pageable) {
        try {
            return ResponseEntity.ok().body(service.findMatchingErrors(projectService.toId(projectCode), pattern, pageable));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET all distinct properties for errors.
     *
     * @param projectCode the code of the project in which to work
     * @param property    the name of the property of the Error entity to get distinct values
     * @return the ResponseEntity with status 200 (OK) and the list of distinct
     */
    @GetMapping("/distinct/{property:[a-zA-Z]+}")
    public ResponseEntity<DistinctStatisticsDTO> getDistinct(@PathVariable String projectCode, @PathVariable String property) {
        try {
            return ResponseEntity.ok().body(service.findDistinctProperties(projectService.toId(projectCode), property));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

}
