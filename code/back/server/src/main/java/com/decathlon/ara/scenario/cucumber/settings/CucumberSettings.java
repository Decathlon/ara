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

package com.decathlon.ara.scenario.cucumber.settings;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.settings.AvailableTechnologySettings;
import com.decathlon.ara.service.dto.setting.SettingType;

public enum CucumberSettings implements AvailableTechnologySettings {

    REPORT_PATH(
            "report.path",
            "Cucumber report path",
            "Cucumber reports are extracted from this path. Eg. \"/report.json\", appended to the run's job folder.",
            SettingType.STRING,
            "/report.json",
            true
    ),
    STEP_DEFINITIONS_PATH(
            "step.definitions.path",
            "Cucumber step definitions path",
            "Cucumber step definitions are extracted from this path. Eg. \" /stepDefinitions.json\", appended to the run's job folder. If not provided, the cycle-definitions will not be downloaded",
            SettingType.STRING,
            "/stepDefinitions.json",
            true
    );

    private final String code;
    private final String name;
    private final String help;
    private final SettingType type;
    private final String defaultValue;
    private final Boolean required;

    CucumberSettings(String code, String name, String help, SettingType type, String defaultValue, Boolean required) {
        this.code = code;
        this.name = name;
        this.help = help;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    @Override
    public Technology getTechnology() {
        return Technology.CUCUMBER;
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
