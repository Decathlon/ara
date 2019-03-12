package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.setting.SettingGroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.Entities;
import com.decathlon.ara.service.dto.setting.SettingValueDTO;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Settings.
 */
@RestController
@RequestMapping(SettingResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingResource {

    private static final String NAME = Entities.SETTING;
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";

    @NonNull
    private final SettingService service;

    @NonNull
    private final ProjectService projectService;

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
    @Timed
    public List<SettingGroupDTO> update(@PathVariable String projectCode,
                                        @PathVariable String code,
                                        @Valid @RequestBody SettingValueDTO settingValue) throws BadRequestException {
        final long projectId = projectService.toId(projectCode);

        service.update(projectId, code, StringUtils.trimToNull(settingValue.getValue()));

        // Changing some properties (like the current indexer or defect adapter)
        // can lead to an entire group of settings to be replaced by another ones:
        // refresh the client GUI with the new sets of settings
        return service.getTree(projectId);
    }

    /**
     * GET all entities.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     * @throws NotFoundException on unknown project
     */
    @GetMapping("")
    @Timed
    public List<SettingGroupDTO> getAll(@PathVariable String projectCode) throws NotFoundException {
        return service.getTree(projectService.toId(projectCode));
    }

}
