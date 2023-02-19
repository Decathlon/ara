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

import com.decathlon.ara.service.CommunicationService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.communication.CommunicationDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.decathlon.ara.Entities.COMMUNICATION;
import static com.decathlon.ara.web.rest.CommunicationResource.COMMUNICATION_BASE_API_PATH;
import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;

@RestController
@RequestMapping(COMMUNICATION_BASE_API_PATH)
public class CommunicationResource {

    public static final String COMMUNICATION_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/communications";
    public static final String COMMUNICATION_ALL_API_PATHS = COMMUNICATION_BASE_API_PATH + "/**";

    private final CommunicationService service;

    private final ProjectService projectService;

    public CommunicationResource(CommunicationService service, ProjectService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    /**
     * GET all entities.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping
    public ResponseEntity<List<CommunicationDTO>> getAll(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(service.findAll(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<CommunicationDTO> getOneByCode(@PathVariable String projectCode, @PathVariable String code) {
        try {
            return ResponseEntity.ok().body(service.findOneByCode(projectService.toId(projectCode), code));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    @PutMapping("/{code}")
    public ResponseEntity<CommunicationDTO> update(@PathVariable String projectCode,
                                                   @PathVariable String code,
                                                   @Valid @RequestBody CommunicationDTO dtoToUpdate) {
        dtoToUpdate.setCode(code);
        try {
            CommunicationDTO updatedDto = service.update(projectService.toId(projectCode), dtoToUpdate);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(COMMUNICATION, updatedDto.getCode()))
                    .body(updatedDto);
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

}
