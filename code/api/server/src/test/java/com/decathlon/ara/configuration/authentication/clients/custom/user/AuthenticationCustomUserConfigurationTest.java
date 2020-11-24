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

package com.decathlon.ara.configuration.authentication.clients.custom.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthenticationCustomUserConfigurationTest {

    @Test
    public void getHttpMethod_getGETMethod_whenMethodBlank() {
        // Given
        AuthenticationCustomUserConfiguration customUserConfiguration = new AuthenticationCustomUserConfiguration()
                .withMethod(null);

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void getHttpMethod_getGETMethod_whenMethodIsGet() {
        // Given
        AuthenticationCustomUserConfiguration customUserConfiguration = new AuthenticationCustomUserConfiguration()
                .withMethod("get");

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void getHttpMethod_getGETMethod_whenMethodIsNeitherGetNorPost() {
        // Given
        AuthenticationCustomUserConfiguration customUserConfiguration = new AuthenticationCustomUserConfiguration()
                .withMethod("neither-get-nor-post");

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void getHttpMethod_getPostMethod_whenMethodIsPost() {
        // Given
        AuthenticationCustomUserConfiguration customUserConfiguration = new AuthenticationCustomUserConfiguration()
                .withMethod("post");

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.POST);
    }
}
