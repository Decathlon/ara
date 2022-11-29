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

package com.decathlon.ara.security.service;

import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.dto.provider.AuthenticationProviderDTO;
import com.decathlon.ara.security.dto.provider.AuthenticationProvidersDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Objects;

@Service
@Transactional
public class AuthenticationService {

    private final OAuth2ProvidersConfiguration providersConfiguration;

    @Value("${ara.loginStartingUrl}")
    private String loginStartingUrl;

    @Value("${ara.logoutProcessingUrl}")
    private String logoutProcessingUrl;

    public AuthenticationService(OAuth2ProvidersConfiguration providersConfiguration) {
        this.providersConfiguration = providersConfiguration;
    }

    /**
     * Get the authentication configuration
     * @return the authentication configuration
     */
    public AuthenticationProvidersDTO getAuthenticationConfiguration() {
        var providers = CollectionUtils.isEmpty(this.providersConfiguration.getSetup()) ?
                new ArrayList<AuthenticationProviderDTO>() :
                this.providersConfiguration.getSetup()
                        .stream()
                        .map(ProviderSetupConfiguration::getProvider)
                        .filter(Objects::nonNull)
                        .map(provider -> new AuthenticationProviderDTO(provider.getDisplayValue(), provider.getType(), provider.getRegistration()))
                        .toList();
        return new AuthenticationProvidersDTO(
                providers,
                this.loginStartingUrl,
                this.logoutProcessingUrl);
    }
}
