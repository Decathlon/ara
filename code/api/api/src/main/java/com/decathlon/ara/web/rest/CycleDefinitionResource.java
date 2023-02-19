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

import com.decathlon.ara.service.CycleDefinitionService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.decathlon.ara.Entities.CYCLE_DEFINITION;
import static com.decathlon.ara.web.rest.CycleDefinitionResource.CYCLE_DEFINITION_BASE_API_PATH;
import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;

/**
 * REST controller for managing Cycle Definitions.
 */
@RestController
@RequestMapping(CYCLE_DEFINITION_BASE_API_PATH)
public class CycleDefinitionResource {

    public static final String CYCLE_DEFINITION_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/cycle-definitions";
    public static final String CYCLE_DEFINITION_ALL_API_PATHS = CYCLE_DEFINITION_BASE_API_PATH + "/**";

    private final CycleDefinitionService service;

    private final ProjectService projectService;

    public CycleDefinitionResource(CycleDefinitionService service, ProjectService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    /**
     * POST to create a new entity.
     *
     * @param projectCode the code of the project in which to work
     * @param dtoToCreate the entity to create
     * @return the ResponseEntity with status 201 (Created) and with body the new entity, or with status 400 (Bad Request) if the entity has
     * already an ID
     */
    @PostMapping
    public ResponseEntity<CycleDefinitionDTO> create(@PathVariable String projectCode, @Valid @RequestBody CycleDefinitionDTO dtoToCreate) {

        if (dtoToCreate.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.idMustBeEmpty(CYCLE_DEFINITION)).build();
        }

        try {
            CycleDefinitionDTO createdDto = service.create(projectService.toId(projectCode), dtoToCreate);
            return ResponseEntity.created(HeaderUtil.uri(String.format("%s/%d", CYCLE_DEFINITION_BASE_API_PATH, createdDto.getId()), projectCode))
                    .headers(HeaderUtil.entityCreated(CYCLE_DEFINITION, createdDto.getId()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }

    }

    /**
     * PUT to update an existing entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<CycleDefinitionDTO> update(@PathVariable String projectCode, @PathVariable Long id, @Valid @RequestBody CycleDefinitionDTO dtoToUpdate) {
        dtoToUpdate.setId(id); // HTTP PUT requires the URL to be the URL of the entity
        try {
            CycleDefinitionDTO updatedDto = service.update(projectService.toId(projectCode), dtoToUpdate);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(CYCLE_DEFINITION, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET all entities.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping
    public ResponseEntity<List<CycleDefinitionDTO>> getAll(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(service.findAll(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * DELETE one entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the id of the entity to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> delete(@PathVariable String projectCode, @PathVariable long id) {
        try {
            service.delete(projectService.toId(projectCode), id);
            return ResponseUtil.deleted(CYCLE_DEFINITION, id);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
