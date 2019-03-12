package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.domain.Setting;
import com.decathlon.ara.ci.fetcher.FileSystemFetcher;
import com.decathlon.ara.repository.SettingRepository;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.dto.setting.SettingGroupDTO;
import com.decathlon.ara.service.dto.setting.SettingType;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Settings of a Project.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingService {

    private static final String VALIDATION_ERROR_KEY = "validation";

    @NonNull
    private final SettingRepository repository;

    @NonNull
    private final SettingProviderService settingProviderService;

    // A SettingService can be used by several threads at once:
    // make sure the global cache is reliably thread-safe with ConcurrentHashMap.computeIfAbsent and synchronizedMap()
    private Map<Long, Map<String, String>> projectsValuesCache = new ConcurrentHashMap<>();

    /**
     * Get the project settings, organized as a tree of groups, with values and descriptions.
     *
     * @param projectId the ID of the project in which to work
     * @return the settings, tailored to the GUI: definitions, editing helps and values (passwords are hidden)
     */
    @Transactional(readOnly = true)
    public List<SettingGroupDTO> getTree(long projectId) {
        Map<String, String> values = getValues(projectId);
        List<SettingGroupDTO> definitions = settingProviderService.getDefinitions(projectId, values);
        populateValues(definitions, values);
        return definitions;
    }

    /**
     * Get the value of a setting for a given project, with the default value replacing the actual one if none is set.
     * This is for internal use by ARA.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the setting to retrieve for the project
     * @return the value of the setting for the project, or its default value if not set, as a string
     */
    @Transactional(readOnly = true)
    public String get(long projectId, String code) {
        String value = getValues(projectId).get(code);
        if (StringUtils.isEmpty(value)) {
            value = getSettingDefinition(projectId, code)
                    .map(SettingDTO::getDefaultValue)
                    .orElse(null);
        }
        return value;
    }

    /**
     * Get the value of a setting for a given project as an integer. The default value is replacing the actual one if
     * none is set. INT settings MUST have a default value. This is for internal use by ARA.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the setting to retrieve for the project
     * @return the value of the setting for the project, or its default value if not set, as an integer
     */
    @Transactional(readOnly = true)
    public int getInt(long projectId, String code) {
        String value = get(projectId, code);
        if (value == null) {
            throw new NotGonnaHappenException("Integer settings must have a default value defined in the code.");
        }
        return Integer.parseInt(value);
    }

    /**
     * Get the value of a setting for a given project as a boolean. The default value is replacing the actual one if
     * none is set. This is for internal use by ARA.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the setting to retrieve for the project
     * @return the value of the setting for the project, or its default value if not set, as a boolean
     */
    @Transactional(readOnly = true)
    public boolean getBoolean(long projectId, String code) {
        String value = get(projectId, code);
        return Boolean.TRUE.toString().equals(value);
    }

    /**
     * Get the value of a setting for a given project as a list of strings separated by commas (','). The default value
     * is replacing the actual one if none is set. Can return an empty list if there is no effective value, but will
     * never return null. This is for internal use by ARA.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the setting to retrieve for the project
     * @return the value of the setting for the project, or its default value if not set, as a list of strings
     */
    @Transactional(readOnly = true)
    public List<String> getList(long projectId, String code) {
        String value = get(projectId, code);
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    /**
     * Update a setting of a project, making sure the setting code exists and is valid for the current state of the
     * project, making sure the value is correct regarding the setting's business rules.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the setting to update in the project
     * @param newValue  the new value, as entered by users (will be validated)
     * @throws BadRequestException if the code is not known or the value format and/or business rules are not respected
     */
    @Transactional
    public void update(long projectId, String code, String newValue) throws BadRequestException {
        // Setting code exists and is valid for the current project context?
        final SettingDTO settingDefinition = getSettingDefinition(projectId, code)
                .orElseThrow(() -> new NotFoundException(Messages.NOT_FOUND_SETTING, Entities.SETTING));

        // New value if valid against all its setting's business rules?
        validateNewValue(newValue, settingDefinition);

        // Change value in database
        Setting setting = repository.findByProjectIdAndCode(projectId, code);
        if (setting == null) {
            setting = new Setting();
            setting.setProjectId(projectId);
            setting.setCode(code);
        } else if (StringUtils.compare(setting.getValue(), newValue) == 0) {
            return;
        }
        setting.setValue(newValue);
        repository.save(setting);

        // Some settings need to execute code after a change (clear a cache, update dependent data, etc.)
        final Consumer<String> changeApplier = settingDefinition.getApplyChange();
        if (changeApplier != null) {
            changeApplier.accept(newValue);
        }

        // Save was successful: update cache
        getValues(projectId).put(code, newValue);
    }

    /**
     * Returns True if (and only if) the given projectId use the Filesystem Indexer.
     *
     * @param projectId the id of the project check
     * @return true if the project use the filesystem indexer, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean useFileSystemIndexer(long projectId) {
        return FileSystemFetcher.FILESYSTEM.equals(this.get(projectId, Settings.EXECUTION_INDEXER));
    }

    /**
     * Returns all settings of a project, making only one database access and keeping values in a memory cache for fast
     * use anywhere in the ARA source code.
     *
     * @param projectId the ID of the project in which to work
     * @return all setting raw values for the requested project, for ARA internal working
     */
    @Transactional
    public Map<String, String> getValues(long projectId) {
        // Lazy-loading of the cache (thread-safe with ConcurrentHashMap.computeIfAbsent and synchronizedMap())
        return projectsValuesCache.computeIfAbsent(projectId, key ->
                Collections.synchronizedMap(repository.getProjectSettings(projectId)));
    }

    /**
     * Validate the new setting value is defined if required and no default value, the data format of the setting type
     * is correct and the custom business rules of the setting are respected.
     *
     * @param newValue          the new setting value to validate against its setting definition
     * @param settingDefinition the setting definition
     * @throws BadRequestException if the required setting would not be set, the data type format is not respected or
     *                             the setting's business rules are not met
     */
    void validateNewValue(String newValue, SettingDTO settingDefinition) throws BadRequestException {
        if (StringUtils.isEmpty(newValue)) {
            validateRequiredSettingIsPresent(settingDefinition);
            // Do not validate format on a missing value
            return;
        }

        validateSettingTypeFormat(newValue, settingDefinition);
        validateSettingCustomValidator(newValue, settingDefinition);
    }

    private void validateRequiredSettingIsPresent(SettingDTO settingDefinition) throws BadRequestException {
        if (settingDefinition.isRequired() && StringUtils.isEmpty(settingDefinition.getDefaultValue())) {
            throw new BadRequestException(Messages.RULE_SETTING_REQUIRED, Entities.SETTING, "required");
        }
    }

    private void validateSettingTypeFormat(String newValue, SettingDTO settingDefinition) throws BadRequestException {
        switch (settingDefinition.getType()) {
            case SELECT:
                if (settingDefinition.getOptions().stream().noneMatch(option -> option.getValue().equals(newValue))) {
                    throw new NotFoundException(Messages.NOT_FOUND_SETTING_OPTION, Entities.SETTING);
                }
                break;
            case BOOLEAN:
                if (!Boolean.TRUE.toString().equals(newValue) && !Boolean.FALSE.toString().equals(newValue)) {
                    throw new BadRequestException(Messages.RULE_SETTING_WRONG_FORMAT_BOOLEAN, Entities.SETTING, VALIDATION_ERROR_KEY);
                }
                break;
            case INT:
                try {
                    Integer.parseInt(newValue);
                    // Catching exception is not fast, but it is a user-input, and not a frequent one
                    // Moreover, with parseInt (compared to a RegEx) we are sure the number is in the supported range
                } catch (NumberFormatException e) {
                    throw new BadRequestException(Messages.RULE_SETTING_WRONG_FORMAT_INT, Entities.SETTING, VALIDATION_ERROR_KEY);
                }
                break;
            case STRING:
            case TEXTAREA:
            case PASSWORD:
                // String values: no format to validate
                break;
            default:
                throw new NotGonnaHappenException("A new SettingType has been introduced, not handled in validation and not tested yet.");
        }
    }

    private void validateSettingCustomValidator(String newValue, SettingDTO settingDefinition) throws BadRequestException {
        final Function<String, String> validator = settingDefinition.getValidate();
        if (validator != null) {
            final String validationError = validator.apply(newValue);
            if (StringUtils.isNotEmpty(validationError)) {
                throw new BadRequestException(validationError, Entities.SETTING, VALIDATION_ERROR_KEY);
            }
        }
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param code      the code of the setting to get inside the project context
     * @return the setting definition, if known and allowed for the current state of the project (if some adapter is not
     * enabled, the properties of such adapters are not available)
     */
    Optional<SettingDTO> getSettingDefinition(long projectId, String code) {
        for (SettingGroupDTO group : settingProviderService.getDefinitions(projectId, getValues(projectId))) {
            for (SettingDTO setting : group.getSettings()) {
                if (setting.getCode().equals(code)) {
                    return Optional.of(setting);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Replace the values of the settings in {@code groups} with the ones found in {@code values}, of their default
     * values, or values hiding the passwords.
     *
     * @param groups all settings to set their values
     * @param values the values of all settings for the project, to use for these settings
     */
    void populateValues(List<SettingGroupDTO> groups, Map<String, String> values) {
        for (SettingGroupDTO group : groups) {
            for (SettingDTO setting : group.getSettings()) {
                populateValue(setting, values);
            }
        }
    }

    /**
     * Replace the value of the {@code setting} with the one found in {@code values}, of its default value, or a value
     * hiding the password.
     *
     * @param setting the setting to set its value
     * @param values  the values of all settings for the project, to use for this setting
     */
    private void populateValue(SettingDTO setting, Map<String, String> values) {
        if (setting.getType() == SettingType.PASSWORD) {
            // Write-only
            if (StringUtils.isNotEmpty(values.get(setting.getCode()))) {
                setting.setValue("\u2022\u2022\u2022\u2022\u2022");
            } else {
                setting.setValue(null);
            }
        } else {
            // Set value, or the default one
            setting.setValue(values.get(setting.getCode()));
            if (StringUtils.isEmpty(setting.getValue())) {
                setting.setValue(setting.getDefaultValue());
            }
        }
    }

}
