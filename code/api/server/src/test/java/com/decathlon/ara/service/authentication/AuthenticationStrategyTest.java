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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void getAuthenticator_returnEmptyOptional_whenNameIsBlank() {
        // Given

        // When

        // Then
        Optional<Authenticator> authenticator = authenticationStrategy.getAuthenticator(null);
        assertThat(authenticator)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void getAuthenticator_returnCustomAuthenticator_whenCustomProviderNameGiven() {
        // Given
        String providerName = "custom";

        // When

        // Then
        Optional<Authenticator> authenticator = authenticationStrategy.getAuthenticator(providerName);
        assertThat(authenticator)
                .isNotNull()
                .isNotEmpty()
                .containsInstanceOf(CustomAuthenticator.class);
    }

    @Test
    public void getAuthenticator_returnGithubAuthenticator_whenGithubProviderNameGiven() {
        // Given
        String providerName = "github";

        // When

        // Then
        Optional<Authenticator> authenticator = authenticationStrategy.getAuthenticator(providerName);
        assertThat(authenticator)
                .isNotNull()
                .isNotEmpty()
                .containsInstanceOf(GithubAuthenticator.class);
    }

    @Test
    public void getAuthenticator_returnGoogleAuthenticator_whenGoogleProviderNameGiven() {
        // Given
        String providerName = "google";

        // When

        // Then
        Optional<Authenticator> authenticator = authenticationStrategy.getAuthenticator(providerName);
        assertThat(authenticator)
                .isNotNull()
                .isNotEmpty()
                .containsInstanceOf(GoogleAuthenticator.class);
    }

    @Test
    public void getAuthenticator_returnEmptyOptional_whenUnknownProviderNameGiven() {
        // Given
        String providerName = "<unknown-provider-name>";

        // When

        // Then
        Optional<Authenticator> authenticator = authenticationStrategy.getAuthenticator(providerName);
        assertThat(authenticator)
                .isNotNull()
                .isEmpty();
    }
}
