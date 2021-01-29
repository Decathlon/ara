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

package com.decathlon.ara.service.dto.authentication.response.configuration.front.provider;

import lombok.Data;

@Data
public abstract class FrontAuthenticationProviderConfigurationDTO {

    public FrontAuthenticationProviderConfigurationDTO(Boolean enabled, String name, String displayedName, String icon, String... parameters) {
        this.enabled = enabled;
        this.name = name;
        this.display = displayedName;
        this.icon = icon;
        this.uri = getLoginUrl(parameters);
    }

    protected Boolean enabled;

    protected String name;

    protected String display;

    protected String icon;

    protected String uri;

    /**
     * Get the login uri
     * @param parameters the parameters used to create the login uri (e.g. client id, etc...)
     * @return the login uri
     */
    protected abstract String getLoginUrl(String... parameters);
}
