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

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.Entities;
import com.decathlon.ara.service.ExecutedScenarioService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.request.ExecutedScenarioHistoryInputDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing ExecutedScenarios.
 */
@RestController
@RequestMapping(ExecutedScenarioResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutedScenarioResource {

    private static final String NAME = Entities.EXECUTED_SCENARIO;
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";

    @NonNull
    private final ExecutedScenarioService executedScenarioService;

    @NonNull
    private final ProjectService projectService;

    /**
     * @param projectCode the code of the project in which to work
     * @param input containing the mandatory cucumberId of the scenario to get history, and optional filter parameters
     * @return history of the execution of a scenario by its cucumberId
     */
    @PostMapping("/history")
    @Timed
    public ResponseEntity<List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO>> getHistory(
            @PathVariable String projectCode, @Valid @RequestBody ExecutedScenarioHistoryInputDTO input) {
        try {
            return ResponseEntity.ok().body(executedScenarioService.findHistory(projectService.toId(projectCode), input));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Return the executed scenario linked to the given id.
     *
     * @param projectCode the code of the project which holds the executed scenario
     * @param id          the id of the executed scenario
     * @return the Executed scenario
     */
    @GetMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<ExecutedScenarioDTO> getExecutedScenario(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(executedScenarioService.findOne(projectService.toId(projectCode), id));
        } catch (BadRequestException ex) {
            return ResponseUtil.handle(ex);
        }
    }

}
