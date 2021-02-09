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

package com.decathlon.ara.configuration.authentication;

import com.decathlon.ara.configuration.authentication.provider.custom.AuthenticationCustomConfiguration;
import com.decathlon.ara.configuration.authentication.provider.github.AuthenticationGithubConfiguration;
import com.decathlon.ara.configuration.authentication.provider.google.AuthenticationGoogleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationConfigurationTest {

    @Mock
    private AuthenticationGithubConfiguration github;

    @Mock
    private AuthenticationGoogleConfiguration google;

    @Mock
    private AuthenticationCustomConfiguration custom;

    @InjectMocks
    private AuthenticationConfiguration authenticationConfiguration;

    @Test
    void isEnabled_returnTrue_whenAtLeastOneProviderEnabled() {
        // Given

        // When
        when(github.isEnabled()).thenReturn(false);
        when(google.isEnabled()).thenReturn(false);
        when(custom.isEnabled()).thenReturn(true);

        // Then
        Boolean isEnabled = authenticationConfiguration.isEnabled();
        assertThat(isEnabled).isTrue();
    }

    @Test
    void isEnabled_returnFalse_whenAllProvidersDisabled() {
        // Given

        // When
        when(github.isEnabled()).thenReturn(false);
        when(google.isEnabled()).thenReturn(false);
        when(custom.isEnabled()).thenReturn(false);

        // Then
        Boolean isEnabled = authenticationConfiguration.isEnabled();
        assertThat(isEnabled).isFalse();
    }
}
