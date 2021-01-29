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
import com.decathlon.ara.service.dto.authentication.response.configuration.front.provider.FrontAuthenticationProvidersConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.front.provider.FrontCustomAuthenticationProviderConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.front.provider.FrontGithubAuthenticationProviderConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.front.provider.FrontGoogleAuthenticationProviderConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationProviderDetailsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationStrategyTest {

    @Mock
    private CustomAuthenticator customAuthenticator;

    @Mock
    private GithubAuthenticator githubAuthenticator;

    @Mock
    private GoogleAuthenticator googleAuthenticator;

    @InjectMocks
    private AuthenticationStrategy authenticationStrategy;

    @Test
    public void getAuthenticatorAndProviderDetails_returnEmptyOptional_whenNameIsBlank() {
        // Given
        FrontAuthenticationProvidersConfigurationDTO providersConfiguration = mock(FrontAuthenticationProvidersConfigurationDTO.class);

        // When

        // Then
        Optional<Pair<Authenticator, AuthenticationProviderDetailsDTO>> authenticatorAndProviderDetails = authenticationStrategy.getAuthenticatorAndProviderDetails(null, providersConfiguration);
        assertThat(authenticatorAndProviderDetails)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void getAuthenticatorAndProviderDetails_returnEmptyOptional_whenProviderIsNull() {
        // Given
        String providerName = "any_provider_name";

        // When

        // Then
        Optional<Pair<Authenticator, AuthenticationProviderDetailsDTO>> authenticatorAndProviderDetails = authenticationStrategy.getAuthenticatorAndProviderDetails(providerName, null);
        assertThat(authenticatorAndProviderDetails)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void getAuthenticatorAndProviderDetails_returnCustomAuthenticator_whenCustomProviderNameGiven() {
        // Given
        String providerName = "custom";
        FrontAuthenticationProvidersConfigurationDTO providersConfiguration = mock(FrontAuthenticationProvidersConfigurationDTO.class);
        FrontCustomAuthenticationProviderConfigurationDTO customConfiguration = new FrontCustomAuthenticationProviderConfigurationDTO(true, Optional.empty(), "login-uri");
        FrontGoogleAuthenticationProviderConfigurationDTO googleConfiguration = new FrontGoogleAuthenticationProviderConfigurationDTO(true, "google-client-id", "base-url");
        FrontGithubAuthenticationProviderConfigurationDTO githubConfiguration = new FrontGithubAuthenticationProviderConfigurationDTO(true, "github-client-id");

        // When
        when(providersConfiguration.getCustom()).thenReturn(customConfiguration);
        when(providersConfiguration.getGoogle()).thenReturn(googleConfiguration);
        when(providersConfiguration.getGithub()).thenReturn(githubConfiguration);

        // Then
        Optional<Pair<Authenticator, AuthenticationProviderDetailsDTO>> authenticatorAndProviderDetails = authenticationStrategy.getAuthenticatorAndProviderDetails(providerName, providersConfiguration);
        assertThat(authenticatorAndProviderDetails)
                .isNotNull()
                .isNotEmpty();
        assertThat(authenticatorAndProviderDetails.get().getFirst()).isInstanceOf(CustomAuthenticator.class);
        assertThat(authenticatorAndProviderDetails.get().getSecond()).extracting("name", "code").containsExactly("Custom", "custom");
    }

    @Test
    public void getAuthenticatorAndProviderDetails_returnGithubAuthenticator_whenGithubProviderNameGiven() {
        // Given
        String providerName = "github";
        FrontAuthenticationProvidersConfigurationDTO providersConfiguration = mock(FrontAuthenticationProvidersConfigurationDTO.class);
        FrontCustomAuthenticationProviderConfigurationDTO customConfiguration = new FrontCustomAuthenticationProviderConfigurationDTO(true, Optional.empty(), "login-uri");
        FrontGoogleAuthenticationProviderConfigurationDTO googleConfiguration = new FrontGoogleAuthenticationProviderConfigurationDTO(true, "google-client-id", "base-url");
        FrontGithubAuthenticationProviderConfigurationDTO githubConfiguration = new FrontGithubAuthenticationProviderConfigurationDTO(true, "github-client-id");

        // When
        when(providersConfiguration.getCustom()).thenReturn(customConfiguration);
        when(providersConfiguration.getGoogle()).thenReturn(googleConfiguration);
        when(providersConfiguration.getGithub()).thenReturn(githubConfiguration);

        // Then
        Optional<Pair<Authenticator, AuthenticationProviderDetailsDTO>> authenticatorAndProviderDetails = authenticationStrategy.getAuthenticatorAndProviderDetails(providerName, providersConfiguration);
        assertThat(authenticatorAndProviderDetails)
                .isNotNull()
                .isNotEmpty();
        assertThat(authenticatorAndProviderDetails.get().getFirst()).isInstanceOf(GithubAuthenticator.class);
        assertThat(authenticatorAndProviderDetails.get().getSecond()).extracting("name", "code").containsExactly("Github", "github");
    }

    @Test
    public void getAuthenticatorAndProviderDetails_returnGoogleAuthenticator_whenGoogleProviderNameGiven() {
        // Given
        String providerName = "google";
        FrontAuthenticationProvidersConfigurationDTO providersConfiguration = mock(FrontAuthenticationProvidersConfigurationDTO.class);
        FrontCustomAuthenticationProviderConfigurationDTO customConfiguration = new FrontCustomAuthenticationProviderConfigurationDTO(true, Optional.empty(), "login-uri");
        FrontGoogleAuthenticationProviderConfigurationDTO googleConfiguration = new FrontGoogleAuthenticationProviderConfigurationDTO(true, "google-client-id", "base-url");
        FrontGithubAuthenticationProviderConfigurationDTO githubConfiguration = new FrontGithubAuthenticationProviderConfigurationDTO(true, "github-client-id");

        // When
        when(providersConfiguration.getCustom()).thenReturn(customConfiguration);
        when(providersConfiguration.getGoogle()).thenReturn(googleConfiguration);
        when(providersConfiguration.getGithub()).thenReturn(githubConfiguration);

        // Then
        Optional<Pair<Authenticator, AuthenticationProviderDetailsDTO>> authenticatorAndProviderDetails = authenticationStrategy.getAuthenticatorAndProviderDetails(providerName, providersConfiguration);
        assertThat(authenticatorAndProviderDetails)
                .isNotNull()
                .isNotEmpty();
        assertThat(authenticatorAndProviderDetails.get().getFirst()).isInstanceOf(GoogleAuthenticator.class);
        assertThat(authenticatorAndProviderDetails.get().getSecond()).extracting("name", "code").containsExactly("Google", "google");
    }

    @Test
    public void getAuthenticatorAndProviderDetails_returnEmptyOptional_whenUnknownProviderNameGiven() {
        // Given
        String providerName = "<unknown-provider-name>";
        FrontAuthenticationProvidersConfigurationDTO providersConfiguration = mock(FrontAuthenticationProvidersConfigurationDTO.class);
        FrontCustomAuthenticationProviderConfigurationDTO customConfiguration = new FrontCustomAuthenticationProviderConfigurationDTO(true, Optional.empty(), "login-uri");
        FrontGoogleAuthenticationProviderConfigurationDTO googleConfiguration = new FrontGoogleAuthenticationProviderConfigurationDTO(true, "google-client-id", "base-url");
        FrontGithubAuthenticationProviderConfigurationDTO githubConfiguration = new FrontGithubAuthenticationProviderConfigurationDTO(true, "github-client-id");

        // When
        when(providersConfiguration.getCustom()).thenReturn(customConfiguration);
        when(providersConfiguration.getGoogle()).thenReturn(googleConfiguration);
        when(providersConfiguration.getGithub()).thenReturn(githubConfiguration);

        // Then
        Optional<Pair<Authenticator, AuthenticationProviderDetailsDTO>> authenticatorAndProviderDetails = authenticationStrategy.getAuthenticatorAndProviderDetails(providerName, providersConfiguration);
        assertThat(authenticatorAndProviderDetails)
                .isNotNull()
                .isEmpty();
    }
}
