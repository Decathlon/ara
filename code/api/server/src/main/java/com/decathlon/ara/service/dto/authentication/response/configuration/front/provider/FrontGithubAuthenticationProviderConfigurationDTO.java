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

public class FrontGithubAuthenticationProviderConfigurationDTO extends FrontAuthenticationProviderConfigurationDTO {

    public FrontGithubAuthenticationProviderConfigurationDTO(Boolean enabled, String clientId) {
        super(enabled, "github", "Github", "github", clientId);
    }

    /**
     * Get login uri from client id
     * @param parameters the parameters used to create the login uri (here the first parameter is the client id)
     * @return the login uri
     */
    @Override
    protected String getLoginUrl(String... parameters) {
        String clientId = parameters[0];
        return String.format("https://github.com/login/oauth/authorize?client_id=%s", clientId);
    }
}
