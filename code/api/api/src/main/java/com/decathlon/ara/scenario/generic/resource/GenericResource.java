/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.scenario.generic.resource;

import com.decathlon.ara.scenario.generic.bean.GenericExecutedScenarioReport;
import com.decathlon.ara.scenario.generic.upload.GenericScenarioUploader;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.decathlon.ara.scenario.generic.resource.GenericResource.GENERIC_SCENARIO_BASE_API_PATH;
import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;

@RestController
@RequestMapping(GENERIC_SCENARIO_BASE_API_PATH)
public class GenericResource {

    public static final String GENERIC_SCENARIO_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/generic";
    public static final String GENERIC_SCENARIO_ALL_API_PATHS = GENERIC_SCENARIO_BASE_API_PATH + "/**";

    private static final Logger LOG = LoggerFactory.getLogger(GenericResource.class);

    private final ProjectService projectService;

    private final GenericScenarioUploader genericScenarioUploader;

    public GenericResource(ProjectService projectService, GenericScenarioUploader genericScenarioUploader) {
        this.projectService = projectService;
        this.genericScenarioUploader = genericScenarioUploader;
    }

    @PostMapping("scenarios/upload/{sourceCode}")
    public ResponseEntity<Void> uploadGenericScenarios(
                                                       @PathVariable String projectCode,
                                                       @PathVariable String sourceCode,
                                                       @RequestBody List<@Valid GenericExecutedScenarioReport> genericReports) {
        try {
            Long projectId = projectService.toId(projectCode);
            genericScenarioUploader.upload(projectId, sourceCode, genericReports);
        } catch (BadRequestException e) {
            LOG.error("Ara failed to add generic scenarios ({})", sourceCode, e);
            return ResponseUtil.handle(e);
        }
        return ResponseEntity.ok().build();
    }
}
