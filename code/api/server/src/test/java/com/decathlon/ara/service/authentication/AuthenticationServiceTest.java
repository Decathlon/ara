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
import com.decathlon.ara.configuration.authentication.AuthenticationConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.AuthenticationCustomConfiguration;
import com.decathlon.ara.configuration.authentication.provider.github.AuthenticationGithubConfiguration;
import com.decathlon.ara.configuration.authentication.provider.google.AuthenticationGoogleConfiguration;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.AuthenticationConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.provider.AuthenticationProvidersConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationProviderDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private AuthenticationStrategy authenticationStrategy;

    @Mock
    private AraConfiguration araConfiguration;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationGithubConfiguration githubConfiguration;

    @Mock
    private AuthenticationGoogleConfiguration googleConfiguration;

    @Mock
    private AuthenticationCustomConfiguration customConfiguration;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void authenticate_throwAuthenticationException_whenUserRequestIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate((UserAuthenticationRequestDTO) null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoUserAuthenticatorFound() {
        // Given
        UserAuthenticationRequestDTO authenticationRequest = mock(UserAuthenticationRequestDTO.class);

        String provider = "provider";

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticatorAndProviderDetails(anyString(), any(AuthenticationProvidersConfigurationDTO.class))).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(AuthenticationException.class);
        ArgumentCaptor<String> providerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(authenticationStrategy).getAuthenticatorAndProviderDetails(providerArgumentCaptor.capture(), any(AuthenticationProvidersConfigurationDTO.class));
        String capturedProvider = providerArgumentCaptor.getValue();
        assertThat(capturedProvider).isEqualTo(provider);
    }

    @Test
    public void authenticate_returnUserAuthenticationResponse_whenAuthenticatorFound() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO authenticationRequest = mock(UserAuthenticationRequestDTO.class);

        String provider = "provider";

        Authenticator authenticator = mock(Authenticator.class);
        AuthenticationProviderDetailsDTO providerDetails = mock(AuthenticationProviderDetailsDTO.class);

        ResponseEntity<UserAuthenticationDetailsDTO> authenticationResponse = mock(ResponseEntity.class);
        UserAuthenticationDetailsDTO responseBody = mock(UserAuthenticationDetailsDTO.class);

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticatorAndProviderDetails(anyString(), any(AuthenticationProvidersConfigurationDTO.class))).thenReturn(Optional.of(Pair.of(authenticator, providerDetails)));
        when(authenticator.authenticate(authenticationRequest)).thenReturn(authenticationResponse);
        when(authenticationResponse.getBody()).thenReturn(responseBody);

        // Then
        ResponseEntity<UserAuthenticationDetailsDTO> result = authenticationService.authenticate(authenticationRequest);
        assertThat(result).isEqualTo(authenticationResponse);
        ArgumentCaptor<String> providerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(authenticationStrategy).getAuthenticatorAndProviderDetails(providerArgumentCaptor.capture(), any(AuthenticationProvidersConfigurationDTO.class));
        String capturedProvider = providerArgumentCaptor.getValue();
        assertThat(capturedProvider).isEqualTo(provider);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenAppRequestIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate((AppAuthenticationRequestDTO) null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoAppAuthenticatorFound() {
        // Given
        AppAuthenticationRequestDTO authenticationRequest = mock(AppAuthenticationRequestDTO.class);

        String provider = "provider";

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticatorAndProviderDetails(anyString(), any(AuthenticationProvidersConfigurationDTO.class))).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(AuthenticationException.class);
        ArgumentCaptor<String> providerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(authenticationStrategy).getAuthenticatorAndProviderDetails(providerArgumentCaptor.capture(), any(AuthenticationProvidersConfigurationDTO.class));
        String capturedProvider = providerArgumentCaptor.getValue();
        assertThat(capturedProvider).isEqualTo(provider);
    }

    @Test
    public void authenticate_returnAppAuthenticationDetails_whenAuthenticatorFound() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO authenticationRequest = mock(AppAuthenticationRequestDTO.class);

        String provider = "provider";

        Authenticator authenticator = mock(Authenticator.class);
        AuthenticationProviderDetailsDTO providerDetails = mock(AuthenticationProviderDetailsDTO.class);

        ResponseEntity<AppAuthenticationDetailsDTO> authenticationResponse = mock(ResponseEntity.class);
        AppAuthenticationDetailsDTO responseBody = mock(AppAuthenticationDetailsDTO.class);

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticatorAndProviderDetails(anyString(), any(AuthenticationProvidersConfigurationDTO.class))).thenReturn(Optional.of(Pair.of(authenticator, providerDetails)));
        when(authenticator.authenticate(authenticationRequest)).thenReturn(authenticationResponse);

        // Then
        ResponseEntity<AppAuthenticationDetailsDTO> result = authenticationService.authenticate(authenticationRequest);
        assertThat(result).isEqualTo(authenticationResponse);
        ArgumentCaptor<String> providerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(authenticationStrategy).getAuthenticatorAndProviderDetails(providerArgumentCaptor.capture(), any(AuthenticationProvidersConfigurationDTO.class));
        String capturedProvider = providerArgumentCaptor.getValue();
        assertThat(capturedProvider).isEqualTo(provider);
    }

    @Test
    public void getAuthenticationConfiguration_returnClientConfiguration_whenNoProviderConfigurationMissing() {
        // Given
        String googleClientId = "google-client-id";
        String clientBaseUrl = "http://my-front-base-url.org";
        String githubClientId = "github-client-id";
        String customDisplayedName = "My Company Name";
        String customLoginUri = "http://my-custom-login-url.com";

        // When
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(googleConfiguration.getClientId()).thenReturn(googleClientId);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(githubConfiguration.isEnabled()).thenReturn(true);
        when(githubConfiguration.getClientId()).thenReturn(githubClientId);
        when(customConfiguration.isEnabled()).thenReturn(true);
        when(customConfiguration.getDisplayedName()).thenReturn(customDisplayedName);
        when(customConfiguration.getLoginUri()).thenReturn(customLoginUri);

        // Then
        AuthenticationConfigurationDTO authenticationConfiguration = authenticationService.getAuthenticationConfiguration();
        assertThat(authenticationConfiguration)
                .extracting(
                        "providers.google.enabled",
                        "providers.google.name",
                        "providers.google.display",
                        "providers.google.icon",
                        "providers.google.uri",

                        "providers.github.enabled",
                        "providers.github.name",
                        "providers.github.display",
                        "providers.github.icon",
                        "providers.github.uri",

                        "providers.custom.enabled",
                        "providers.custom.name",
                        "providers.custom.display",
                        "providers.custom.icon",
                        "providers.custom.uri"
                )
                .containsExactly(
                        true,
                        "google",
                        "Google",
                        "google",
                        "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?" +
                                "client_id=google-client-id&" +
                                "response_type=code&" +
                                "redirect_uri=http://my-front-base-url.org/login/google&" +
                                "scope=https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email&" +
                                "flowName=GeneralOAuthFlow",

                        true,
                        "github",
                        "Github",
                        "github",
                        "https://github.com/login/oauth/authorize?client_id=github-client-id",

                        true,
                        "custom",
                        "My Company Name",
                        "building",
                        "http://my-custom-login-url.com"
                );
    }
}
