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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthenticationProviderConfigurationDTOTest {

    @Test
    public void getLoginUri_returnLoginUri_whenInstanceOfGithubAuthenticationProviderConfigurationDTO() {
        // Given
        GithubAuthenticationProviderConfigurationDTO provider = new GithubAuthenticationProviderConfigurationDTO(true, "my-client-id");

        // When

        // Then
        String loginUri = provider.getUri();
        assertThat(loginUri).isEqualTo("https://github.com/login/oauth/authorize?client_id=my-client-id");
    }

    @Test
    public void getLoginUri_returnLoginUri_whenInstanceOfGoogleAuthenticationProviderConfigurationDTOAndFrontBaseUrlDoesNotEndWithSlash() {
        // Given
        GoogleAuthenticationProviderConfigurationDTO provider = new GoogleAuthenticationProviderConfigurationDTO(true, "my-client-id", "http://my-front-base-url.org");

        // When

        // Then
        String loginUri = provider.getUri();
        assertThat(loginUri).isEqualTo(
                "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?" +
                        "client_id=my-client-id&" +
                        "response_type=code&" +
                        "redirect_uri=http://my-front-base-url.org/login/google&" +
                        "scope=https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email&" +
                        "flowName=GeneralOAuthFlow"
        );
    }

    @Test
    public void getLoginUri_returnLoginUri_whenInstanceOfGoogleAuthenticationProviderConfigurationDTOAndFrontBaseUrlEndsWithSlash() {
        // Given
        GoogleAuthenticationProviderConfigurationDTO provider = new GoogleAuthenticationProviderConfigurationDTO(true, "my-client-id", "http://my-front-base-url.org/");

        // When

        // Then
        String loginUri = provider.getUri();
        assertThat(loginUri).isEqualTo(
                "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?" +
                        "client_id=my-client-id&" +
                        "response_type=code&" +
                        "redirect_uri=http://my-front-base-url.org/login/google&" +
                        "scope=https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email&" +
                        "flowName=GeneralOAuthFlow"
        );
    }

    @Test
    public void getLoginUri_returnLoginUri_whenInstanceOfCustomAuthenticationProviderConfigurationDTO() {
        // Given
        String displayedName = "My Company Name";
        String customLoginUrl = "http://my-custom-login-url.com";
        CustomAuthenticationProviderConfigurationDTO provider = new CustomAuthenticationProviderConfigurationDTO(true, Optional.of(displayedName), customLoginUrl);

        // When

        // Then
        String loginUri = provider.getUri();
        assertThat(loginUri).isEqualTo(customLoginUrl);
    }

    @Test
    public void getDisplay_returnDisplayedName_whenInstanceOfCustomAuthenticationProviderConfigurationDTOAndDisplayedNameGiven() {
        // Given
        String displayedName = "My Company Name";
        String customLoginUrl = "http://my-custom-login-url.com";
        CustomAuthenticationProviderConfigurationDTO provider = new CustomAuthenticationProviderConfigurationDTO(true, Optional.of(displayedName), customLoginUrl);

        // When

        // Then
        String display = provider.getDisplay();
        assertThat(display).isEqualTo(displayedName);
    }

    @Test
    public void getDisplay_returnDisplayedNameAsCustom_whenInstanceOfCustomAuthenticationProviderConfigurationDTOAndNoDisplayedNameGiven() {
        // Given
        String customLoginUrl = "http://my-custom-login-url.com";
        CustomAuthenticationProviderConfigurationDTO provider = new CustomAuthenticationProviderConfigurationDTO(true, Optional.empty(), customLoginUrl);

        // When

        // Then
        String display = provider.getDisplay();
        assertThat(display).isEqualTo("Custom");
    }
}
