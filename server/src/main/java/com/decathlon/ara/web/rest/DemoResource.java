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
import com.decathlon.ara.service.DemoService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.loader.DemoLoaderConstants.PROJECT_CODE_DEMO;
import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;
import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Cycle Runs.
 */
@RestController
@RequestMapping(DemoResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoResource {

    static final String PATH = API_PATH + "/demo";

    @NonNull
    private final DemoService service;

    /**
     * POST to create the demo project.
     *
     * @return the ResponseEntity with status 201 (Created) and with body the new demo project, or with status 400
     * (Bad Request) if the demo project already exists
     */
    @PostMapping
    @Timed
    public ResponseEntity<ProjectDTO> create() {
        try {
            final ProjectDTO project = service.create();
            return ResponseEntity.created(HeaderUtil.uri(PROJECT_API_PATH, project.getCode()))
                    .headers(HeaderUtil.entityCreated(Entities.PROJECT, project.getCode()))
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
    @Timed
    public ResponseEntity<Void> delete() {
        try {
            service.delete();
            return ResponseUtil.deleted(Entities.PROJECT, PROJECT_CODE_DEMO);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
