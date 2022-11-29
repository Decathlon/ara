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

package com.decathlon.ara.scenario.cypress.resource;

import com.decathlon.ara.scenario.cucumber.bean.Feature;
import com.decathlon.ara.scenario.cypress.upload.CypressScenarioUploader;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

@RestController
@RequestMapping(CypressResource.PATH)
public class CypressResource {

    private static final Logger LOG = LoggerFactory.getLogger(CypressResource.class);

    static final String PATH = PROJECT_API_PATH + "/cypress";
    public static final String PATHS = PATH + "/**";

    private final ProjectService projectService;

    private final CypressScenarioUploader cypressScenarioUploader;

    public CypressResource(ProjectService projectService, CypressScenarioUploader cypressScenarioUploader) {
        this.projectService = projectService;
        this.cypressScenarioUploader = cypressScenarioUploader;
    }

    @PostMapping("scenarios/upload/{sourceCode}")
    public ResponseEntity<Void> uploadCucumberScenarios(@PathVariable String projectCode, @PathVariable String sourceCode, @RequestBody List<Feature> features) {
        try {
            cypressScenarioUploader.uploadScenarios(projectService.toId(projectCode), sourceCode, features);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            LOG.error("Failed to upload Cypress (Cucumber) scenarios for source code {}", sourceCode, e);
            return ResponseUtil.handle(e);
        }
    }

}
