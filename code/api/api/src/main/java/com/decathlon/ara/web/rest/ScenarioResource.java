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

import com.decathlon.ara.scenario.common.service.ScenarioService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreSourceDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioSummaryDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;
import static com.decathlon.ara.web.rest.ScenarioResource.SCENARIO_BASE_API_PATH;

/**
 * REST controller for managing Scenarios.
 */
@RestController
@RequestMapping(SCENARIO_BASE_API_PATH)
public class ScenarioResource {

    public static final String SCENARIO_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/scenarios";
    public static final String SCENARIO_ALL_API_PATHS = SCENARIO_BASE_API_PATH + "/**";

    private final ScenarioService scenarioService;

    private final ProjectService projectService;

    public ScenarioResource(ScenarioService scenarioService, ProjectService projectService) {
        this.scenarioService = scenarioService;
        this.projectService = projectService;
    }

    /**
     * @param projectCode the code of the project in which to work
     * @return all scenarios that have no associated functionalities or have wrong or nonexistent functionality identifier
     */
    @GetMapping("/without-functionalities")
    public ResponseEntity<List<ScenarioSummaryDTO>> getAllWithFunctionalityErrors(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(scenarioService.findAllWithFunctionalityErrors(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * @param projectCode the code of the project in which to work
     * @return for each source (API, Web...), a count of ignored&amp;total scenarios and a list of ignored scenarios by feature file
     */
    @GetMapping("/ignored")
    public ResponseEntity<List<ScenarioIgnoreSourceDTO>> getIgnoredScenarioCounts(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(scenarioService.getIgnoredScenarioCounts(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

}
