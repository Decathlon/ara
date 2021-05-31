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


import com.decathlon.ara.Entities;
import com.decathlon.ara.scenario.postman.upload.PostmanScenarioUploader;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

@Slf4j
@RestController
@RequestMapping(PostmanResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PostmanResource {

    static final String PATH = PROJECT_API_PATH + "/postman";

    @NonNull
    private final ProjectService projectService;

    @NonNull
    private final PostmanScenarioUploader postmanScenarioUploader;

    @PostMapping("scenarios/upload/{sourceCode}")
    public ResponseEntity<Void> uploadScenarios(@PathVariable String projectCode,
                                                @PathVariable String sourceCode,
                                                @RequestParam("file") MultipartFile file) {
        File tempZipFile = null;
        try {
            tempZipFile = File.createTempFile("ara_scenario_upload_", ".zip");
            tempZipFile.deleteOnExit();
            file.transferTo(tempZipFile);
            postmanScenarioUploader.uploadPostman(projectService.toId(projectCode), sourceCode, tempZipFile);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            log.error("Failed to upload ZIP file containing Postman requests for source code {}", sourceCode, e);
            return ResponseUtil.handle(e);
        } catch (IOException e) {
            log.error("Failed to upload ZIP file containing Postman requests for source code {}", sourceCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(HeaderUtil.exception(Entities.SCENARIO, e))
                    .build();
        } finally {
            FileUtils.deleteQuietly(tempZipFile);
        }
    }
}
