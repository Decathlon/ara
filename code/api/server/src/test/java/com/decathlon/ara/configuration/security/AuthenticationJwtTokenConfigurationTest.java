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

package com.decathlon.ara.configuration.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuthenticationJwtTokenConfigurationTest {

    @Test
    void getAccessTokenExpirationInMillisecond_returnZero_whenAccessTokenExpirationIsNull() {
        // Given
        AuthenticationJwtTokenConfiguration tokenConfiguration = new AuthenticationJwtTokenConfiguration();
        tokenConfiguration.setAccessTokenExpirationInSecond(null);

        // When

        // Then
        Long expirationInMillisecond = tokenConfiguration.getAccessTokenExpirationInMillisecond();
        assertThat(expirationInMillisecond).isZero();
    }

    @Test
    void getAccessTokenExpirationInMillisecond_returnAccessTokenExpirationInSecond_whenAccessTokenExpirationIsNotNull() {
        // Given
        AuthenticationJwtTokenConfiguration tokenConfiguration = new AuthenticationJwtTokenConfiguration();
        tokenConfiguration.setAccessTokenExpirationInSecond(60L);

        // When

        // Then
        Long expirationInMillisecond = tokenConfiguration.getAccessTokenExpirationInMillisecond();
        assertThat(expirationInMillisecond).isEqualTo(60000L);
    }

    @Test
    void isUsingHttps_returnFalse_whenUsingHttpsIsNull() {
        // Given
        AuthenticationJwtTokenConfiguration tokenConfiguration = new AuthenticationJwtTokenConfiguration();
        tokenConfiguration.setUsingHttps(null);

        // When

        // Then
        Boolean isUsingHttps = tokenConfiguration.isUsingHttps();
        assertThat(isUsingHttps).isFalse();
    }

    @Test
    void isUsingHttps_returnUsingHttps_whenUsingHttpsIsNotNull() {
        // Given
        AuthenticationJwtTokenConfiguration tokenConfiguration = new AuthenticationJwtTokenConfiguration();
        tokenConfiguration.setUsingHttps(true);

        // When

        // Then
        Boolean isUsingHttps = tokenConfiguration.isUsingHttps();
        assertThat(isUsingHttps).isTrue();
    }

}
