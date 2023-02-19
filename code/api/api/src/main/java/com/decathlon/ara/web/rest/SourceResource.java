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

import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SourceService;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.dto.support.Upsert;
import com.decathlon.ara.service.dto.support.UpsertResultDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.decathlon.ara.Entities.SOURCE;
import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;
import static com.decathlon.ara.web.rest.SourceResource.SOURCE_BASE_API_PATH;

/**
 * REST controller for managing Sources.
 */
@RestController
@RequestMapping(SOURCE_BASE_API_PATH)
public class SourceResource {

    public static final String SOURCE_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/sources";
    public static final String SOURCE_ALL_API_PATHS = SOURCE_BASE_API_PATH + "/**";

    private final SourceService sourceService;

    private final ProjectService projectService;

    public SourceResource(SourceService sourceService, ProjectService projectService) {
        this.sourceService = sourceService;
        this.projectService = projectService;
    }

    /**
     * GET all entities, ordered by name.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping
    public ResponseEntity<List<SourceDTO>> getAll(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(sourceService.findAll(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * POST to create a new entity.
     *
     * @param projectCode the code of the project in which to work
     * @param dtoToCreate the entity to create
     * @return the ResponseEntity with status 201 (Created) and with body the new entity, or with status 400 (Bad Request) if the entity has
     * already an code
     */
    @PostMapping
    public ResponseEntity<SourceDTO> create(@PathVariable String projectCode, @Valid @RequestBody SourceDTO dtoToCreate) {
        try {
            SourceDTO createdDto = sourceService.create(projectService.toId(projectCode), dtoToCreate);
            return ResponseEntity.created(HeaderUtil.uri(String.format("%s/%s", SOURCE_BASE_API_PATH, createdDto.getCode()), projectCode))
                    .headers(HeaderUtil.entityCreated(SOURCE, createdDto.getCode()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing entity.
     *
     * @param projectCode the code of the project in which to work
     * @param code the CODE of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{code}")
    public ResponseEntity<SourceDTO> createOrUpdate(@PathVariable String projectCode, @PathVariable String code, @Valid @RequestBody SourceDTO dtoToUpdate) {
        dtoToUpdate.setCode(code); // HTTP PUT requires the URL to be the URL of the entity
        try {
            final UpsertResultDTO<SourceDTO> result = sourceService.createOrUpdate(projectService.toId(projectCode), dtoToUpdate);
            final boolean isNew = result.getOperation() == Upsert.INSERT;
            final String newCode = result.getUpsertedDto().getCode();

            return ResponseEntity
                    .status(isNew ? HttpStatus.CREATED : HttpStatus.OK)
                    .headers(isNew ? HeaderUtil.entityCreated(SOURCE, newCode) : HeaderUtil.entityUpdated(SOURCE, newCode))
                    .body(result.getUpsertedDto());
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * DELETE one entity.
     *
     * @param projectCode the code of the project in which to work
     * @param code the code of the entity to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String projectCode, @PathVariable String code) {
        try {
            sourceService.delete(projectService.toId(projectCode), code);
            return ResponseUtil.deleted(SOURCE, code);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
