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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthenticationConfigurationTest {

    @Test
    public void isEnabled_returnFalse_whenEnabledIsNull() {
        // Given
        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        authenticationConfiguration.setEnabled(null);

        // When

        // Then
        Boolean isEnabled = authenticationConfiguration.isEnabled();
        assertThat(isEnabled).isFalse();
    }

    @Test
    public void isEnabled_returnEnabledValue_whenEnabledIsNotNull() {
        // Given
        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        authenticationConfiguration.setEnabled(true);

        // When

        // Then
        Boolean isEnabled = authenticationConfiguration.isEnabled();
        assertThat(isEnabled).isTrue();
    }
}
