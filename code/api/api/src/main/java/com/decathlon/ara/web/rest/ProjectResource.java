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
import com.decathlon.ara.loader.DemoLoaderConstants;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.decathlon.ara.web.rest.util.RestConstants.*;

/**
 * REST controller for managing Projects.
 */
@RestController
@RequestMapping(ProjectResource.PATH)
public class ProjectResource {

    private static final String NAME = Entities.PROJECT;
    public static final String PATH = API_PATH + "/" + NAME + "s";
    public static final String CODE_PATH = PROJECT_API_PATH;

    public static final String DEMO_PATHS = PATH + "/" + DemoLoaderConstants.DEMO_PROJECT_CODE + "/**";

    private final ProjectService projectService;

    private final UserAccountService userAccountService;

    public ProjectResource(ProjectService projectService, UserAccountService userAccountService) {
        this.projectService = projectService;
        this.userAccountService = userAccountService;
    }

    /**
     * Create a new project.
     * The logged-in user becomes admin of the project he just created.
     *
     * @param dtoToCreate the project to create
     * @return the ResponseEntity with status 201 (Created) and with body the new project, or with status 400 (Bad Request) if the project has
     * already an ID
     */
    @PostMapping
    public ResponseEntity<ProjectDTO> create(@Valid @RequestBody ProjectDTO dtoToCreate) {
        if (dtoToCreate.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.idMustBeEmpty(NAME)).build();
        }
        try {
            var creationUser = userAccountService.getCurrentUserEntity().orElseThrow(() -> new ForbiddenException(Entities.PROJECT, "project creation"));
            ProjectDTO createdDto = projectService.create(dtoToCreate, creationUser);
            userAccountService.updateCurrentUserAccountProjectScope(createdDto.getCode(), UserAccountScopeRole.ADMIN);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + createdDto.getId()))
                    .headers(HeaderUtil.entityCreated(NAME, createdDto.getId()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Update an existing project.
     *
     * @param projectCode          the code of the project to update
     * @param dtoToUpdate the project to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated project, or with status 400 (Bad Request) if the project is not
     * valid, or with status 500 (Internal Server Error) if the project couldn't be updated
     */
    @PutMapping(PROJECT_CODE_REQUEST_PARAMETER)
    public ResponseEntity<ProjectDTO> update(@PathVariable String projectCode, @Valid @RequestBody ProjectDTO dtoToUpdate) {
        try {
            var projectId = projectService.toId(projectCode);
            dtoToUpdate.setId(projectId); // HTTP PUT requires the URL to be the URL of the entity
            var updateUser = userAccountService.getCurrentUserEntity().orElseThrow(() -> new ForbiddenException(Entities.PROJECT, "project update", Pair.of("code", projectCode)));
            ProjectDTO updatedDto = projectService.update(dtoToUpdate, updateUser);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Get all projects (within the logged-in user scope).
     *
     * @return the ResponseEntity with status 200 (OK) and the list of projects in body
     */
    @GetMapping
    public List<ProjectDTO> getAll() {
        return projectService.findAll();
    }

    /**
     * Delete a specific project
     * @param projectCode the project to delete
     * @return a ResponseEntity with 200 status code if the project was successfully deleted
     */
    @DeleteMapping(PROJECT_CODE_REQUEST_PARAMETER)
    public ResponseEntity<Void> deleteProject(@PathVariable String projectCode) {
        try {
            projectService.delete(projectCode);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
        return ResponseUtil.deleted(NAME, projectCode);
    }

}
