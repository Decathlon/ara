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
import com.decathlon.ara.service.CountryService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.support.Upsert;
import com.decathlon.ara.service.dto.support.UpsertResultDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import com.decathlon.ara.Entities;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Countries.
 */
@RestController
@RequestMapping(CountryResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CountryResource {

    static final String PATH = PROJECT_API_PATH + "/countries";
    private static final String NAME = Entities.COUNTRY;

    @NonNull
    private final CountryService service;

    @NonNull
    private final ProjectService projectService;

    /**
     * POST to create a new entity.
     *
     * @param projectCode the code of the project in which to work
     * @param dtoToCreate the entity to create
     * @return the ResponseEntity with status 201 (Created) and with body the new entity, or with status 400 (Bad
     * Request) if an entity with the same code or name already exists
     */
    @PostMapping("")
    @Timed
    public ResponseEntity<CountryDTO> create(@PathVariable String projectCode, @Valid @RequestBody CountryDTO dtoToCreate) {
        try {
            CountryDTO createdDto = service.create(projectService.toId(projectCode), dtoToCreate);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + createdDto.getCode(), projectCode))
                    .headers(HeaderUtil.entityCreated(NAME, createdDto.getCode()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing entity OR insert a new one if the code does not exist yet.
     *
     * @param projectCode         the code of the project in which to work
     * @param code                the code of the entity to update
     * @param dtoToInsertOrUpdate the entity to insert or update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be inserted nor updated
     */
    @PutMapping("/{code}")
    @Timed
    public ResponseEntity<CountryDTO> createOrUpdate(@PathVariable String projectCode, @PathVariable String code, @Valid @RequestBody CountryDTO dtoToInsertOrUpdate) {
        dtoToInsertOrUpdate.setCode(code); // HTTP PUT requires the URL to be the URL of the entity
        try {
            final UpsertResultDTO<CountryDTO> result = service.createOrUpdate(projectService.toId(projectCode), dtoToInsertOrUpdate);
            final boolean isNew = result.getOperation() == Upsert.INSERT;
            final String newCode = result.getUpsertedDto().getCode();
            return ResponseEntity
                    .status(isNew ? HttpStatus.CREATED : HttpStatus.OK)
                    .headers(isNew ? HeaderUtil.entityCreated(NAME, newCode) : HeaderUtil.entityUpdated(NAME, newCode))
                    .body(result.getUpsertedDto());
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
    @GetMapping("")
    @Timed
    public ResponseEntity<List<CountryDTO>> getAll(@PathVariable String projectCode) {
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
     * @param code        the code of the entity to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/{code}")
    @Timed
    public ResponseEntity<Void> delete(@PathVariable String projectCode, @PathVariable String code) {
        try {
            service.delete(projectService.toId(projectCode), code);
            return ResponseUtil.deleted(NAME, code);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
