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

package com.decathlon.ara.scenario.karate.settings;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.settings.AvailableTechnologySettings;
import com.decathlon.ara.service.dto.setting.SettingType;

public enum KarateSettings implements AvailableTechnologySettings {
    REPORTS_LOCATION(
            "reports.path",
            "Karate reports path",
            "Karates reports are extracted from this path. Eg. \"/reports\", appended to the run's jobUrl.",
            SettingType.STRING,
            "/reports",
            true
    );

    private final String code;
    private final String name;
    private final String help;
    private final SettingType type;
    private final String defaultValue;
    private final Boolean required;

    KarateSettings(String code, String name, String help, SettingType type, String defaultValue, Boolean required) {
        this.code = code;
        this.name = name;
        this.help = help;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    @Override
    public Technology getTechnology() {
        return Technology.KARATE;
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
