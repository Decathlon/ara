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

package com.decathlon.ara.service.dto.setting;

import java.util.List;

public class SettingGroupDTO {

    /**
     * User-visible name of the group of settings to display in the GUI.
     */
    protected String name;

    /**
     * The list of settings for the current project and group.
     */
    protected List<SettingDTO> settings;

    public SettingGroupDTO() {
    }

    public SettingGroupDTO(String name, List<SettingDTO> settings) {
        this.name = name;
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public List<SettingDTO> getSettings() {
        return settings;
    }

}
