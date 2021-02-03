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

package com.decathlon.ara.service.authentication;

import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.authentication.provider.custom.CustomAuthenticator;
import com.decathlon.ara.service.authentication.provider.github.GithubAuthenticator;
import com.decathlon.ara.service.authentication.provider.google.GoogleAuthenticator;
import com.decathlon.ara.service.dto.authentication.response.configuration.provider.*;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationProviderDetailsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AuthenticationStrategy {

    @NonNull
    private final CustomAuthenticator customAuthenticator;

    @NonNull
    private final GithubAuthenticator githubAuthenticator;

    @NonNull
    private final GoogleAuthenticator googleAuthenticator;

    /**
     * If found, return an authenticator and the provider details, matching the provider name given
     * @param providerName the provider name (e.g. "custom", "google", "github", etc.)
     *                     Note that it is case insensitive
     * @param providersConfiguration the provider configuration
     * @return the matching authenticator and provider details, if any
     */
    public Optional<Pair<Authenticator, AuthenticationProviderDetailsDTO>> getAuthenticatorAndProviderDetails(
            String providerName,
            AuthenticationProvidersConfigurationDTO providersConfiguration
    ) {
        if (StringUtils.isBlank(providerName) || providersConfiguration == null) {
            return Optional.empty();
        }

        CustomAuthenticationProviderConfigurationDTO customConfiguration = providersConfiguration.getCustom();
        GithubAuthenticationProviderConfigurationDTO githubConfiguration = providersConfiguration.getGithub();
        GoogleAuthenticationProviderConfigurationDTO googleConfiguration = providersConfiguration.getGoogle();

        AuthenticationProviderDetailsDTO customProviderDetails = getProviderDetails(customConfiguration);
        AuthenticationProviderDetailsDTO githubProviderDetails = getProviderDetails(githubConfiguration);
        AuthenticationProviderDetailsDTO googleProviderDetails = getProviderDetails(googleConfiguration);

        Map<String, Pair<Authenticator, AuthenticationProviderDetailsDTO>> authenticatorAndProviderDetailsByProviderName = Map.ofEntries(
                entry("custom", Pair.of(customAuthenticator, customProviderDetails)),
                entry("github", Pair.of(githubAuthenticator, githubProviderDetails)),
                entry("google", Pair.of(googleAuthenticator, googleProviderDetails))
        );

        Pair<Authenticator, AuthenticationProviderDetailsDTO> authenticatorAndProviderDetails = authenticatorAndProviderDetailsByProviderName.get(providerName.toLowerCase());
        return Optional.ofNullable(authenticatorAndProviderDetails);
    }

    /**
     * Get provider details from the provider configuration
     * @param providerConfiguration the provider configuration
     * @return the provider details
     */
    private AuthenticationProviderDetailsDTO getProviderDetails(AuthenticationProviderConfigurationDTO providerConfiguration) {
        String name = providerConfiguration.getDisplay();
        String code = providerConfiguration.getName();
        return new AuthenticationProviderDetailsDTO()
                .withName(name)
                .withCode(code);
    }
}
