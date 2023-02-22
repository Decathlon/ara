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

import com.decathlon.ara.service.DemoService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.Entities.PROJECT;
import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;
import static com.decathlon.ara.security.access.SecurityConfiguration.BASE_API_PATH;
import static com.decathlon.ara.web.rest.DemoResource.DEMO_BASE_API_PATH;
import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;

/**
 * REST controller for managing Cycle Runs.
 */
@RestController
@RequestMapping(DEMO_BASE_API_PATH)
public class DemoResource {

    public static final String DEMO_BASE_API_PATH = BASE_API_PATH + "/demo";

    public static final String DEMO_ALL_API_PATHS = DEMO_BASE_API_PATH + "/**";

    private final DemoService demoService;

    public DemoResource(DemoService demoService) {
        this.demoService = demoService;
    }

    /**
     * POST to create the demo project.
     *
     * @return the ResponseEntity with status 201 (Created) and with body the new demo project, or with status 400
     * (Bad Request) if the demo project already exists
     */
    @PostMapping
    public ResponseEntity<ProjectDTO> create() {
        try {
            final ProjectDTO project = demoService.create();
            return ResponseEntity.created(HeaderUtil.uri(PROJECT_CODE_BASE_API_PATH, project.getCode()))
                    .headers(HeaderUtil.entityCreated(PROJECT, project.getCode()))
                    .body(project);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * DELETE the demo project.
     *
     * @return the ResponseEntity with status 200 (OK) or 404 (Not Found) if the demo project does not exist
     */
    @DeleteMapping
    public ResponseEntity<Void> delete() {
        try {
            demoService.delete();
            return ResponseUtil.deleted(PROJECT, DEMO_PROJECT_CODE);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
