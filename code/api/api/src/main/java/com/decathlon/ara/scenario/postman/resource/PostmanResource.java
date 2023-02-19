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

package com.decathlon.ara.scenario.postman.resource;

import com.decathlon.ara.scenario.postman.upload.PostmanScenarioUploader;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.decathlon.ara.Entities.SCENARIO;
import static com.decathlon.ara.scenario.postman.resource.PostmanResource.POSTMAN_SCENARIO_BASE_API_PATH;
import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;

@RestController
@RequestMapping(POSTMAN_SCENARIO_BASE_API_PATH)
public class PostmanResource {

    private static final Logger LOG = LoggerFactory.getLogger(PostmanResource.class);

    public static final String POSTMAN_SCENARIO_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/postman";
    public static final String POSTMAN_SCENARIO_ALL_API_PATHS = POSTMAN_SCENARIO_BASE_API_PATH + "/**";

    private final ProjectService projectService;

    private final PostmanScenarioUploader postmanScenarioUploader;

    public PostmanResource(ProjectService projectService, PostmanScenarioUploader postmanScenarioUploader) {
        this.projectService = projectService;
        this.postmanScenarioUploader = postmanScenarioUploader;
    }

    @PostMapping("scenarios/upload/{sourceCode}")
    public ResponseEntity<Void> uploadScenarios(@PathVariable String projectCode,
                                                @PathVariable String sourceCode,
                                                @RequestParam("file") MultipartFile file) {
        LOG.info("SCENARIO|postman|Project: {} -> Receiving postman scenarios ({}) zip for upload", projectCode, sourceCode);
        File tempZipFile = null;
        try {
            tempZipFile = File.createTempFile("ara_scenario_upload_", ".zip");
            tempZipFile.deleteOnExit();
            file.transferTo(tempZipFile);
            postmanScenarioUploader.uploadPostman(projectService.toId(projectCode), sourceCode, tempZipFile);
            LOG.info("SCENARIO|postman|Project: {} -> Postman scenarios ({}) successfully uploaded", projectCode, sourceCode);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            LOG.error("SCENARIO|postman|Project: {} -> Failed to upload ZIP file containing Postman requests for source code {}", projectCode, sourceCode, e);
            return ResponseUtil.handle(e);
        } catch (IOException e) {
            LOG.error("SCENARIO|postman|Project: {} -> Failed to upload ZIP file containing Postman requests for source code {}", projectCode, sourceCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(HeaderUtil.exception(SCENARIO, e))
                    .build();
        } finally {
            FileUtils.deleteQuietly(tempZipFile);
        }
    }
}
