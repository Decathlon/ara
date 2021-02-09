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

import com.decathlon.ara.configuration.AraConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.AuthenticationCustomConfiguration;
import com.decathlon.ara.configuration.authentication.provider.github.AuthenticationGithubConfiguration;
import com.decathlon.ara.configuration.authentication.provider.google.AuthenticationGoogleConfiguration;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.AuthenticationConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.provider.AuthenticationProvidersConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.provider.CustomAuthenticationProviderConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.provider.GithubAuthenticationProviderConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.provider.GoogleAuthenticationProviderConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationProviderDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationService {

    @NonNull
    private final AuthenticationStrategy authenticationStrategy;

    @NonNull
    private final AraConfiguration araConfiguration;

    @NonNull
    private final AuthenticationGithubConfiguration githubConfiguration;

    @NonNull
    private final AuthenticationGoogleConfiguration googleConfiguration;

    @NonNull
    private final AuthenticationCustomConfiguration customConfiguration;

    /**
     * Authenticate an user and return the authentication details
     * @param request the request sent to authenticate the user
     * @return the authentication response
     * @throws AuthenticationException thrown if the authentication has failed
     */
    public ResponseEntity<UserAuthenticationDetailsDTO> authenticate(UserAuthenticationRequestDTO request) throws AuthenticationException {
        Pair<Authenticator, AuthenticationProviderDetailsDTO> authenticatorAndProviderDetails = getAuthenticatorAndProviderDetails(request);
        Authenticator authenticator = authenticatorAndProviderDetails.getFirst();
        AuthenticationProviderDetailsDTO providerDetailsDTO = authenticatorAndProviderDetails.getSecond();
        ResponseEntity<UserAuthenticationDetailsDTO> authenticationResponse = authenticator.authenticate(request);
        UserAuthenticationDetailsDTO authenticationResponseBody = authenticationResponse.getBody();
        if (authenticationResponseBody != null) {
            authenticationResponseBody.setProvider(providerDetailsDTO);
        }
        return authenticationResponse;
    }

    /**
     * Authenticate an  external application and return the authentication details
     * @param request the request sent to authenticate the application
     * @return the authentication response
     * @throws AuthenticationException thrown if the authentication has failed
     */
    public ResponseEntity<AppAuthenticationDetailsDTO> authenticate(AppAuthenticationRequestDTO request) throws AuthenticationException {
        Pair<Authenticator, AuthenticationProviderDetailsDTO> authenticatorAndProviderDetails = getAuthenticatorAndProviderDetails(request);
        Authenticator authenticator = authenticatorAndProviderDetails.getFirst();
        return authenticator.authenticate(request);
    }

    /**
     * Get an authenticator and its provider details from a request, if any one matching
     * @param request the request
     * @return the authenticator and its provider details
     * @throws AuthenticationException if no match found
     */
    private Pair<Authenticator, AuthenticationProviderDetailsDTO> getAuthenticatorAndProviderDetails(AuthenticationRequestDTO request) throws AuthenticationException {
        if (request == null) {
            throw new AuthenticationException("Could not authenticate because the request was null");
        }
        String providerName = request.getProvider();
        AuthenticationConfigurationDTO authenticationConfiguration = getAuthenticationConfiguration();
        return authenticationStrategy
                .getAuthenticatorAndProviderDetails(providerName, authenticationConfiguration.getProviders())
                .orElseThrow(() ->
                        new AuthenticationException(String.format("The provider given (%s) is not supported", providerName))
                );
    }

    /**
     * Get the provider authentication configuration
     * @return the provider authentication configuration
     */
    public AuthenticationConfigurationDTO getAuthenticationConfiguration() {
        GoogleAuthenticationProviderConfigurationDTO google = getGoogleConfiguration();
        GithubAuthenticationProviderConfigurationDTO github = getGithubConfiguration();
        CustomAuthenticationProviderConfigurationDTO custom = getCustomConfiguration();

        AuthenticationProvidersConfigurationDTO providers = new AuthenticationProvidersConfigurationDTO()
                .withGoogle(google)
                .withGithub(github)
                .withCustom(custom);
        return new AuthenticationConfigurationDTO(providers);
    }

    /**
     * Get the Google configuration
     * @return the Google configuration
     */
    private GoogleAuthenticationProviderConfigurationDTO getGoogleConfiguration() {
        String clientId = googleConfiguration.getClientId();
        if (StringUtils.isBlank(clientId)) {
            log.warn("Google client id is not found");
        }
        String frontBaseUrl = araConfiguration.getClientBaseUrl();
        if (StringUtils.isBlank(frontBaseUrl)) {
            log.warn("The client base url (used by Google) is not found");
        }
        Boolean isEnabled = googleConfiguration.isEnabled();
        return new GoogleAuthenticationProviderConfigurationDTO(isEnabled, clientId, frontBaseUrl);
    }

    /**
     * Get the Github configuration
     * @return the Github configuration
     */
    private GithubAuthenticationProviderConfigurationDTO getGithubConfiguration() {
        String clientId = githubConfiguration.getClientId();
        if (StringUtils.isBlank(clientId)) {
            log.warn("Github client id is not found");
        }
        Boolean isEnabled = githubConfiguration.isEnabled();
        return new GithubAuthenticationProviderConfigurationDTO(isEnabled, clientId);
    }

    /**
     * Get the Github configuration
     * @return the Github configuration
     */
    private CustomAuthenticationProviderConfigurationDTO getCustomConfiguration() {
        String displayedName = customConfiguration.getDisplayedName();
        String loginUri = customConfiguration.getLoginUri();
        if (StringUtils.isBlank(loginUri)) {
            log.warn("Custom login url is not found");
        }
        Boolean isEnabled = customConfiguration.isEnabled();
        return new CustomAuthenticationProviderConfigurationDTO(isEnabled, Optional.ofNullable(displayedName), loginUri);
    }
}
