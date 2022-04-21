/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.TechnologySetting;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.repository.TechnologySettingRepository;
import com.decathlon.ara.scenario.common.settings.AvailableTechnologySettings;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.dto.setting.SettingDTO.SettingDTOBuilder;
import com.decathlon.ara.service.dto.setting.TechnologySettingGroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TechnologySettingService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologySettingService.class);

    private final TechnologySettingRepository technologySettingRepository;

    private final SettingService settingService;

    public TechnologySettingService(TechnologySettingRepository technologySettingRepository,
            SettingService settingService) {
        this.technologySettingRepository = technologySettingRepository;
        this.settingService = settingService;
    }

    /**
     * Get all the available technology setting groups.
     * If a setting value is saved, then the setting holds this value, otherwise it holds the default value.
     * @param projectId the project id
     * @return the technology setting groups
     */
    @Transactional(readOnly = true)
    public List<TechnologySettingGroupDTO> getAllGroups(Long projectId) {
        List<TechnologySettingGroupDTO> groups = getTechnologySettingGroupDefinition();
        List<TechnologySetting> savedSettings = technologySettingRepository.findByProjectId(projectId);

        for (TechnologySettingGroupDTO group : groups) {
            List<SettingDTO> settings = group.getSettings();
            for (SettingDTO setting : settings) {
                String code = setting.getCode();
                Optional<String> value = getValueFromSavedSettings(savedSettings, code);
                value.ifPresent(setting::setValue);
            }
        }
        return groups;
    }

    /**
     * Get all the possible technology setting groups
     * @return the default technology setting groups
     */
    private List<TechnologySettingGroupDTO> getTechnologySettingGroupDefinition() {
        List<Technology> availableTechnologies = Arrays.asList(Technology.values());
        List<TechnologySettingGroupDTO> groups = availableTechnologies.stream()
                .map(technology -> Pair.of(technology, getAvailableTechnologySettings(technology)))
                .map(pair -> Pair.of(
                        pair.getFirst(),
                        pair.getSecond()
                                .stream()
                                .map(this::getSettingDTOFromAvailableTechnologySetting)
                                .toList()))
                .map(this::getTechnologySettingGroup)
                .toList();
        return groups;
    }

    /**
     * Get all then enum values setting
     * ({@link AvailableTechnologySettings}, e.g {@link com.decathlon.ara.scenario.postman.settings.PostmanSettings})
     * from a given technology.
     * @param technology the technology
     * @return the available technology settings
     */
    private List<AvailableTechnologySettings> getAvailableTechnologySettings(Technology technology) {
        List<AvailableTechnologySettings> availableTechnologySettings = new ArrayList<>();
        String enumName = getFullEnumNameFromTechnology(technology);
        try {
            Class<?> technologySettingClass = Class.forName(enumName);
            AvailableTechnologySettings[] availableSettings = (AvailableTechnologySettings[]) technologySettingClass.getEnumConstants();
            if (availableSettings != null) {
                return Arrays.asList(availableSettings);
            }
        } catch (ClassNotFoundException e) {
            LOG.warn("SETTING|technology|The class {} was not found", enumName, e);
        } catch (ClassCastException e) {
            LOG.warn("SETTING|technology|The class {} should be an instance of {}", enumName, AvailableTechnologySettings.class.getName(), e);
        }
        return availableTechnologySettings;
    }

    /**
     * Get the whole class name of the enum matching a given technology, e.g. :
     * Technology.POSTMAN  -> "com.decathlon.ara.scenario.postman.settings.PostmanSettings", or
     * Technology.CUCUMBER -> "com.decathlon.ara.scenario.cucumber.settings.CucumberSettings"
     * @param technology the technology
     * @return the matching {@link AvailableTechnologySettings} whole class name
     */
    private String getFullEnumNameFromTechnology(Technology technology) {
        String technologyName = technology.toString();
        String packageName = String.format("com.decathlon.ara.scenario.%s.settings", technologyName.toLowerCase());
        String className = String.format("%sSettings", WordUtils.capitalizeFully(technologyName));
        String fullClassName = String.format("%s.%s", packageName, className);
        return fullClassName;
    }

    /**
     * Create a {@link SettingDTO} from an {@link AvailableTechnologySettings}
     * @param availableSetting the available technology setting
     * @return a matching setting
     */
    private SettingDTO getSettingDTOFromAvailableTechnologySetting(AvailableTechnologySettings availableSetting) {
        String defaultValue = availableSetting.getDefaultValue();
        return new SettingDTOBuilder()
                .withCode(availableSetting.getCode())
                .withName(availableSetting.getName())
                .withHelp(availableSetting.getHelp())
                .withType(availableSetting.getType())
                .withDefaultValue(defaultValue)
                .withRequired(availableSetting.isRequired())
                .withValue(defaultValue).build();
    }

    /**
     * Create a technology setting group from a {@link Technology} and a list of {@link SettingDTO}
     * @param pair a pair containing a technology and a list of settings
     * @return a matching technology setting group
     */
    private TechnologySettingGroupDTO getTechnologySettingGroup(Pair<Technology, List<SettingDTO>> pair) {
        Technology technology = pair.getFirst();
        String groupName = WordUtils.capitalizeFully(technology.toString());
        List<SettingDTO> settings = pair.getSecond();
        return new TechnologySettingGroupDTO(groupName, settings, technology);
    }

    /**
     * Get a value from a list of saved settings ({@link TechnologySetting}) and a code, if found
     * @param technologySettings the saved technology setting
     * @param code the code
     * @return the value, if found
     */
    private Optional<String> getValueFromSavedSettings(List<TechnologySetting> technologySettings, String code) {
        return technologySettings.stream()
                .filter(setting -> StringUtils.isNotBlank(setting.getCode()))
                .filter(setting -> setting.getCode().equals(code))
                .map(TechnologySetting::getValue)
                .filter(StringUtils::isNotBlank)
                .findFirst();
    }

    /**
     * Get the value from the project id and the {@link AvailableTechnologySettings}, if found
     * @param projectId the project id
     * @param availableTechnologySettings the available technology setting
     * @return the value, if found
     */
    @Transactional(readOnly = true)
    public Optional<String> getSettingValue(Long projectId, AvailableTechnologySettings availableTechnologySettings) {
        String code = availableTechnologySettings.getCode();
        Technology technology = availableTechnologySettings.getTechnology();
        Optional<TechnologySetting> savedSetting = technologySettingRepository.findByProjectIdAndCodeAndTechnology(projectId, code, technology);
        String value = null;
        if (savedSetting.isPresent()) {
            return Optional.of(savedSetting.get().getValue());
        }

        Optional<SettingDTO> setting = getSettingDefinitionFromTechnologyAndCode(technology, code);
        if (setting.isPresent()) {
            value = setting.get().getDefaultValue();
        }

        return Optional.ofNullable(value);
    }

    /**
     * Get the default setting technology matching a code and a technology, if found
     * @param technology the technology
     * @param code the code
     * @return the matching setting, if found
     */
    private Optional<SettingDTO> getSettingDefinitionFromTechnologyAndCode(Technology technology, String code) {
        List<TechnologySettingGroupDTO> groups = getTechnologySettingGroupDefinition();
        Optional<TechnologySettingGroupDTO> technologyGroup = groups.stream()
                .filter(group -> technology.equals(group.getTechnology()))
                .findFirst();
        if (!technologyGroup.isPresent()) {
            return Optional.empty();
        }

        Optional<SettingDTO> matchingSetting = technologyGroup.get().getSettings().stream()
                .filter(setting -> setting.getCode().equals(code))
                .findFirst();

        return matchingSetting;
    }

    /**
     * Update a technology setting
     * @param projectId the project id
     * @param code the technology setting code
     * @param technology the concerned technology
     * @param newValue the value to update
     * @throws BadRequestException if something goes wrong, e.g. the code doesn't exist, the value is not validated
     */
    @Transactional
    public void update(Long projectId, String code, Technology technology, String newValue) throws BadRequestException {
        String message = String.format(Messages.NOT_FOUND_TECHNOLOGY_SETTING, code);
        SettingDTO setting = getSettingDefinitionFromTechnologyAndCode(technology, code)
                .orElseThrow(() -> new NotFoundException(message, Entities.TECHNOLOGY_SETTING));

        settingService.validateNewValue(newValue, setting);

        TechnologySetting settingToSave = technologySettingRepository.findByProjectIdAndCodeAndTechnology(projectId, code, technology)
                .orElse(
                        new TechnologySetting(projectId, code, technology));
        settingToSave.setValue(newValue);
        technologySettingRepository.save(settingToSave);
    }

}
