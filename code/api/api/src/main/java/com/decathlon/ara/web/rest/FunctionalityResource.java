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
import com.decathlon.ara.coverage.CoverageService;
import com.decathlon.ara.service.FunctionalityService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.coverage.CoverageDTO;
import com.decathlon.ara.service.dto.functionality.ExporterInfoDTO;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.service.dto.functionality.FunctionalityWithChildrenDTO;
import com.decathlon.ara.service.dto.request.MoveFunctionalitiesDTO;
import com.decathlon.ara.service.dto.request.MoveFunctionalityDTO;
import com.decathlon.ara.service.dto.request.NewFunctionalityDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Functionalities.
 */
@RestController
@RequestMapping(FunctionalityResource.PATH)
public class FunctionalityResource {

    static final String PATH = PROJECT_API_PATH + "/functionalities";
    public static final String PATHS = PATH + "/**";

    private static final String EXPORT = "/export";
    private static final String IMPORT = "/import";
    public static final String EXPORT_PATH = PATH + EXPORT;

    private static final String NAME = Entities.FUNCTIONALITY;

    private final FunctionalityService service;

    private final CoverageService coverageService;

    private final ProjectService projectService;

    public FunctionalityResource(FunctionalityService service, CoverageService coverageService,
            ProjectService projectService) {
        this.service = service;
        this.coverageService = coverageService;
        this.projectService = projectService;
    }

    /**
     * GET all entities.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the tree of entities in body
     */
    @GetMapping
    public ResponseEntity<List<FunctionalityWithChildrenDTO>> getAll(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(service.findAllAsTree(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing functionality (or a folder).
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<FunctionalityDTO> updateProperties(@PathVariable String projectCode, @PathVariable Long id, @Valid @RequestBody FunctionalityDTO dtoToUpdate) {
        dtoToUpdate.setId(id); // HTTP PUT requires the URL to be the URL of the entity
        try {
            FunctionalityDTO updatedDto = service.update(projectService.toId(projectCode), dtoToUpdate);
            if (null == updatedDto.getCreated()) {
                updatedDto.setCreated("");
            }
            if (null == updatedDto.getComment()) {
                updatedDto.setComment("");
            }
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * POST to create a new entity.
     *
     * @param projectCode the code of the project in which to work
     * @param newDto the entity to create, relative to another entity
     * @return the ResponseEntity with status 201 (Created) and with body the new entity, or with status 400 (Bad Request) if the entity has
     * already an ID
     */
    @PostMapping
    public ResponseEntity<FunctionalityDTO> create(@PathVariable String projectCode, @Valid @RequestBody NewFunctionalityDTO newDto) {
        if (newDto.getFunctionality().getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.idMustBeEmpty(NAME)).build();
        }
        try {
            FunctionalityDTO createdDto = service.create(projectService.toId(projectCode), newDto);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + createdDto.getId(), projectCode))
                    .headers(HeaderUtil.entityCreated(NAME, createdDto.getId()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * DELETE one entity, and its children, if any.
     *
     * @param projectCode the project code
     * @param id the functionality id to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> delete(@PathVariable String projectCode, @PathVariable Long id) {
        try {
            service.delete(projectService.toId(projectCode), id);
            return ResponseUtil.deleted(NAME, id);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    @DeleteMapping
    public ResponseEntity<List<FunctionalityWithChildrenDTO>> deleteList(
                                                                         @PathVariable String projectCode,
                                                                         @RequestParam("id") List<Long> ids) {
        try {
            List<FunctionalityWithChildrenDTO> updatedTree = service.deleteList(projectService.toId(projectCode), ids);
            return ResponseEntity.ok().body(updatedTree);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * POST to move a functionality or folder to another place in the tree.
     *
     * @param projectCode the code of the project in which to work
     * @param moveRequest the entity to move, relative to another entity
     * @return the ResponseEntity with status 200 (OK)
     */
    @PostMapping("/move")
    public ResponseEntity<FunctionalityDTO> move(@PathVariable String projectCode, @Valid @RequestBody MoveFunctionalityDTO moveRequest) {
        try {
            FunctionalityDTO updatedDto = service.move(projectService.toId(projectCode), moveRequest);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityMoved(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * POST move several functionalities or folders to another place in the tree.
     *
     * @param projectCode the project code
     * @param moveRequest contains the entities to move, as well as the destination entity
     * @return the ResponseEntity with status 200 (OK)
     */
    @PostMapping("/move/list")
    public ResponseEntity<List<FunctionalityWithChildrenDTO>> moveList(@PathVariable String projectCode, @Valid @RequestBody MoveFunctionalitiesDTO moveRequest) {
        try {
            List<FunctionalityWithChildrenDTO> updatedTree = service.moveList(projectService.toId(projectCode), moveRequest);
            return ResponseEntity.ok().body(updatedTree);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET scenarios covering the given functionality.
     *
     * @param projectCode the code of the project in which to work
     * @param id the ID of the functionality to query
     * @return the ResponseEntity with status 200 (OK) and the list of scenarios covering the functionality, status 404 if the entity is not found, or 400 if requesting scenarios of a folder
     */
    @GetMapping("/{id:[0-9]+}/scenarios")
    public ResponseEntity<List<ScenarioDTO>> getScenarios(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.findScenarios(projectService.toId(projectCode), id));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping("/coverage")
    public ResponseEntity<CoverageDTO> getCoverage(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(coverageService.computeCoverage(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Returns the JSON of all the available Cartography exporters in ARA.
     *
     * @param projectCode the current project code
     * @return the JSON of all the exporters available.
     */
    @RequestMapping(method = RequestMethod.OPTIONS, path = EXPORT)
    public ResponseEntity<List<ExporterInfoDTO>> getExportOptions(@PathVariable String projectCode) {
        return ResponseEntity.ok().body(service.listAvailableExporters());
    }

    /**
     * Returns a streamable resource which will contains the wanted functionalities exported in the wanted format.
     *
     * @param projectCode the code of the project which contains the wanted functionalities.
     * @param functionalities the wanted functionalities' ids
     * @param exportType the wanted export format
     * @return a streamable byte array which represents the exported functionalities for download.
     */
    @GetMapping(EXPORT)
    public ResponseEntity<Resource> export(@PathVariable String projectCode, @RequestParam List<Long> functionalities, @RequestParam String exportType, @RequestParam Map<String, String> additionalParams) {
        try {
            // Remove the already known params catched by the global mapping
            additionalParams.remove("functionalities");
            additionalParams.remove("exportType");
            ByteArrayResource resource = service.generateExport(functionalities, exportType, additionalParams);
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        } catch (BadRequestException ex) {
            return ResponseUtil.handle(ex);
        }
    }

    /**
     * Import the given MultipartFile into the given project as a serialized list of functionalities.
     *
     * @param projectCode the project's code which will contains the imported functionalities
     * @param jsonFunctionalities the json which represents the functionalities (ARA-format).
     * @return 200 OK if the import finished in success.
     */
    @PostMapping(IMPORT)
    public ResponseEntity<Resource> importFunctionalities(@PathVariable String projectCode, @RequestParam("functionalities") MultipartFile jsonFunctionalities) {
        try {
            service.importJSONFunctionalities(projectCode, new String(jsonFunctionalities.getBytes(), StandardCharsets.UTF_8));
            return ResponseEntity.ok().build();
        } catch (BadRequestException ex) {
            return ResponseUtil.handle(ex);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .headers(HeaderUtil.exception(FunctionalityResource.NAME, ex))
                    .build();
        }
    }
}
