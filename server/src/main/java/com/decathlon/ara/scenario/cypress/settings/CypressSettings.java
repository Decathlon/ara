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

package com.decathlon.ara.scenario.cypress.settings;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.settings.AvailableTechnologySettings;
import com.decathlon.ara.service.dto.setting.SettingType;

public enum CypressSettings implements AvailableTechnologySettings {

    CUCUMBER_REPORTS_FOLDER_PATHS(
            "cucumber.reports.folder.paths",
            "Cypress (Cucumber) reports folder path",
            "The path where to find the folder containing the Cypress (Cucumber) reports folder. By default, you can find them in \"/reports/cucumber\", from your run (type) folder.",
            SettingType.STRING,
            "/reports/cucumber",
            true
    ),
    CUCUMBER_FILE_NAME_SUFFIX_VALUE(
            "cucumber.file.name.suffix.value",
            "Cypress (Cucumber) report file name suffix value",
            "This is the suffix value that enables to link each Cypress (Cucumber) report with its matching step definitions file (if any)." +
                    "For instance, if the suffix values are \"cucumber\" and \"stepDefinitions\", then 2 files named \"report.cucumber.json\" and \"report.stepDefinitions.json\" are linked to each other",
            SettingType.STRING,
            "cucumber",
            true
    ),

    STEP_DEFINITIONS_FOLDER_PATH(
            "step.definitions.folder.path",
            "Cypress (Cucumber) step definitions folder path",
            "The path where to find the folder containing the Cypress (Cucumber) step definitions folder. By default, you can find them in \"/stepDefinitions\", from your run (type) folder.",
            SettingType.STRING,
            "/stepDefinitions",
            true
    ),
    STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE(
            "step.definitions.file.name.suffix.value",
            "Cypress (Cucumber) step definitions file name suffix value",
            "The value to link step definitions files to their respective Cucumber reports. For more details check the \"Cypress (Cucumber) report file name suffix value\" setting",
            SettingType.STRING,
            "stepDefinitions",
            true
    ),

    MEDIA_FILE_PATH(
            "media.file.path",
            "Media file path",
            "Where to find the media file, i.e. the file that matches every (Cucumber) features with its matching tests executions medias (videos and snapshots) url/path.",
            SettingType.STRING,
            "media.json",
            true
    );

    private final String code;
    private final String name;
    private final String help;
    private final SettingType type;
    private final String defaultValue;
    private final Boolean required;

    CypressSettings(String code, String name, String help, SettingType type, String defaultValue, Boolean required) {
        this.code = code;
        this.name = name;
        this.help = help;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    @Override
    public Technology getTechnology() {
        return Technology.CYPRESS;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHelp() {
        return help;
    }

    @Override
    public SettingType getType() {
        return type;
    }

    @Override
    public Boolean isRequired() {
        return required;
    }
}
