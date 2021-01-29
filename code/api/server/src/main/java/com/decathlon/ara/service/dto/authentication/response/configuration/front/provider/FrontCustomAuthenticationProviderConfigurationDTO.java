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

import java.util.Optional;

public class FrontCustomAuthenticationProviderConfigurationDTO extends FrontAuthenticationProviderConfigurationDTO {

    public FrontCustomAuthenticationProviderConfigurationDTO(Boolean enabled, Optional<String> displayedName, String loginUri) {
        super(enabled, "custom", displayedName.orElse("Custom"), "building", loginUri);
    }

    /**
     * Get the login uri. As a custom provider, the login uri is the first parameter.
     * @param parameters the parameters used to create the login uri (here the first parameter is the custom login uri)
     * @return the login uri
     */
    @Override
    protected String getLoginUrl(String... parameters) {
        String loginUri = parameters[0];
        return loginUri;
    }
}
