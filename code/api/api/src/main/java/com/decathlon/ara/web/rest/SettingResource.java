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
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.TechnologySettingService;
import com.decathlon.ara.service.dto.setting.SettingGroupDTO;
import com.decathlon.ara.service.dto.setting.SettingValueDTO;
import com.decathlon.ara.service.dto.setting.TechnologySettingGroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Settings.
 */
@Slf4j
@RestController
@RequestMapping(SettingResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingResource {

    private static final String NAME = Entities.SETTING;
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";

    @NonNull
    private final SettingService settingService;

    @NonNull
    private final ProjectService projectService;

    @NonNull
    private final TechnologySettingService technologySettingService;

    /**
     * PUT to update an existing entity.
     *
     * @param projectCode  the code of the project in which to work
     * @param code         the code of the setting to update
     * @param settingValue the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     * @throws NotFoundException   on unknown project, on unknown setting code or one invalid in the current context of
     *                             the project, or setting an unknown value for a setting with only a limited list of
     *                             allowed values
     * @throws BadRequestException when setting an empty value to a required setting without default value, wrong value
     *                             format, or unmet business rules
     */
    @PutMapping("/{code:.+}")
    public List<SettingGroupDTO> update(@PathVariable String projectCode,
                                        @PathVariable String code,
                                        @Valid @RequestBody SettingValueDTO settingValue) throws BadRequestException {
        final long projectId = projectService.toId(projectCode);

        settingService.update(projectId, code, StringUtils.trimToNull(settingValue.getValue()));

        // Changing some properties (like the current indexer or defect adapter)
        // can lead to an entire group of settings to be replaced by another ones:
        // refresh the client GUI with the new sets of settings
        return settingService.getTree(projectId);
    }

    /**
     * GET all the "regular" settings for a given project.
     *
     * @param projectCode the project code
     * @return the ResponseEntity with status 200 (OK) containing the entities in the body
     * @throws NotFoundException if the project is unknown
     */
    @GetMapping("")
    public List<SettingGroupDTO> getAll(@PathVariable String projectCode) throws NotFoundException {
        return settingService.getTree(projectService.toId(projectCode));
    }

    /**
     * Get all the technology settings for a given project.
     *
     * @param projectCode the project code
     * @return the ResponseEntity with status 200 (OK) containing the entities in the body
     * @throws NotFoundException if the project is unknown
     */
    @GetMapping("/technology")
    public List<TechnologySettingGroupDTO> getAllTechnologySettings(@PathVariable String projectCode) throws NotFoundException {
        return technologySettingService.getAllGroups(projectService.toId(projectCode));
    }

    /**
     * Update a given technology setting.
     *
     * @param projectCode  the project code
     * @param code         the code of the setting to update
     * @param settingValue the technology setting to update
     * @param technology the related technology
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     * @throws NotFoundException   on unknown project, on unknown setting code or one invalid in the current context of
     *                             the project, or setting an unknown value for a setting with only a limited list of
     *                             allowed values
     * @throws BadRequestException when setting an empty value to a required setting without default value, wrong value
     *                             format, or unmet business rules
     */
    @PutMapping("/{code:.+}/technology/{technology}")
    public List<TechnologySettingGroupDTO> update(
            @PathVariable String projectCode,
            @PathVariable String code,
            @Valid @RequestBody SettingValueDTO settingValue,
            @PathVariable String technology


    ) throws BadRequestException {
        final Long projectId = projectService.toId(projectCode);

        Technology matchingTechnology;
        try {
            matchingTechnology = Technology.valueOf(technology.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("SETTING|technology|Please check that the technology {} exists", technology, e);
            String message = String.format(Messages.NOT_FOUND_TECHNOLOGY, technology);
            throw new BadRequestException(message, Entities.TECHNOLOGY_SETTING, "unknown_technology");
        }

        technologySettingService.update(projectId, code, matchingTechnology, StringUtils.trimToNull(settingValue.getValue()));

        return technologySettingService.getAllGroups(projectId);
    }

}
