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

package com.decathlon.ara.scenario.karate.resource;

import com.decathlon.ara.scenario.karate.bean.KarateExecutedScenarioReport;
import com.decathlon.ara.scenario.karate.upload.KarateScenarioUploader;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

@RestController
@RequestMapping(PROJECT_API_PATH + "/karate")
public class KarateResource {

    private static final Logger LOG = LoggerFactory.getLogger(KarateResource.class);

    private final ProjectService projectService;

    private final KarateScenarioUploader karateScenarioUploader;

    public KarateResource(ProjectService projectService, KarateScenarioUploader karateScenarioUploader) {
        this.projectService = projectService;
        this.karateScenarioUploader = karateScenarioUploader;
    }

    @PostMapping("scenarios/upload/{sourceCode}")
    public ResponseEntity<Void> uploadKarateScenarios(
                                                       @PathVariable String projectCode,
                                                       @PathVariable String sourceCode,
                                                       @RequestBody List<@Valid KarateExecutedScenarioReport> karateReports) {
        try {
            Long projectId = projectService.toId(projectCode);
            karateScenarioUploader.upload(projectId, sourceCode, karateReports);
        } catch (BadRequestException e) {
            LOG.error("Ara failed to add karate scenarios ({})", sourceCode, e);
            return ResponseUtil.handle(e);
        }
        return ResponseEntity.ok().build();
    }
}
