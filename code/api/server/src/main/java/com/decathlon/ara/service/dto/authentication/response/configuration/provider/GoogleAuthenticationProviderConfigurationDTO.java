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

package com.decathlon.ara.service.dto.authentication.response.configuration.provider;

import org.apache.commons.lang3.StringUtils;

public class GoogleAuthenticationProviderConfigurationDTO extends AuthenticationProviderConfigurationDTO {

    public GoogleAuthenticationProviderConfigurationDTO(Boolean enabled, String clientId, String frontBaseUrl) {
        super(enabled, "google", "Google", "google", clientId, frontBaseUrl);
    }

    /**
     * Get login uri from client id and front base url
     * @param parameters the parameters used to create the login uri (here the first parameter is the client id and the second is the front base url)
     * @return the login uri
     */
    @Override
    protected String getLoginUrl(String... parameters) {
        String clientId = parameters[0];
        String frontBaseUrl = parameters[1];
        String redirectUri = null;
        if (StringUtils.isNotBlank(frontBaseUrl)) {
            String separator = frontBaseUrl.endsWith("/") ? "" : "/";
            redirectUri = String.format("%s%slogin/google", frontBaseUrl, separator);
        }

        String baseUrl = "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount";
        String responseTypeParameter = "response_type=code";
        String flowNameParameter = "flowName=GeneralOAuthFlow";
        String scopeParameter = "scope=https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email";
        String clientIdParameter = String.format("client_id=%s", clientId);
        String redirectUriParameter = String.format("redirect_uri=%s", redirectUri);
        return String.format("%s?%s&%s&%s&%s&%s", baseUrl, clientIdParameter, responseTypeParameter, redirectUriParameter, scopeParameter, flowNameParameter);
    }
}
