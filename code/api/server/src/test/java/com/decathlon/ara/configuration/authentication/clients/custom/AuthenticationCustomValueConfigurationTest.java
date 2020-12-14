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

package com.decathlon.ara.configuration.authentication.clients.custom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthenticationCustomValueConfigurationTest {

    @Test
    public void getRequest_returnRequestWithNoHeaderAndNoBody_whenNoneGiven() {
        // Given
        AuthenticationCustomValueConfiguration customValueConfiguration = new AuthenticationCustomValueConfiguration()
                .withHeaderValues(null)
                .withBodyValues(null);

        // When

        // Then
        HttpEntity<MultiValueMap<String, String>> request = customValueConfiguration.getRequest(null);
        assertThat(request.getHeaders()).isEmpty();
        assertThat(request.hasBody()).isTrue();
        assertThat(request.getBody()).isEmpty();
    }

    @Test
    public void getRequest_returnRequestWithHeaderAndBody_whenBothGivenButNoParameters() {
        // Given
        String rawHeader = "header_attribute1,header_value1|header_attribute2,header_value2|header_attribute3";
        String rawBody = "body_attribute1,body_value1|body_attribute2,body_value2";

        AuthenticationCustomValueConfiguration customValueConfiguration = new AuthenticationCustomValueConfiguration()
                .withHeaderValues(rawHeader)
                .withBodyValues(rawBody);

        // When

        // Then
        HttpEntity<MultiValueMap<String, String>> request = customValueConfiguration.getRequest(null);
        assertThat(request.getHeaders()).hasSize(3);
        assertThat(request.getHeaders().get("header_attribute1")).isEqualTo(Arrays.asList("header_value1"));
        assertThat(request.getHeaders().get("header_attribute2")).isEqualTo(Arrays.asList("header_value2"));
        assertThat(request.getHeaders().get("header_attribute3")).isEqualTo(Arrays.asList(""));

        assertThat(request.hasBody()).isTrue();
        assertThat(request.getBody()).isNotEmpty().hasSize(2);
        assertThat(request.getBody().get("body_attribute1")).isEqualTo(Arrays.asList("body_value1"));
        assertThat(request.getBody().get("body_attribute2")).isEqualTo(Arrays.asList("body_value2"));
    }

    @Test
    public void getRequest_returnRequestWithHeaderAndBody_whenBothGivenWithParameters() {
        // Given
        String rawHeader = "header_attribute1,{{header_parameter1}}|header_attribute2,{{header_parameter2}} {{header_parameter3}} and some string|header_attribute3";
        String rawBody = "body_attribute1,body_value1|body_attribute2,some value {{body_parameter1}}";

        AuthenticationCustomValueConfiguration customValueConfiguration = new AuthenticationCustomValueConfiguration()
                .withHeaderValues(rawHeader)
                .withBodyValues(rawBody);

        Map<String, String> parameters = Map.ofEntries(
                entry("header_parameter1", "custom_value1"),
                entry("header_parameter2", "custom_value2"),
                entry("header_parameter3", "custom_value3"),
                entry("body_parameter1", "custom_value4"),
                entry("unknown_parameter", "a value")
        );

        // When

        // Then
        HttpEntity<MultiValueMap<String, String>> request = customValueConfiguration.getRequest(parameters);
        assertThat(request.getHeaders()).hasSize(3);
        assertThat(request.getHeaders().get("header_attribute1")).isEqualTo(Arrays.asList("custom_value1"));
        assertThat(request.getHeaders().get("header_attribute2")).isEqualTo(Arrays.asList("custom_value2 custom_value3 and some string"));
        assertThat(request.getHeaders().get("header_attribute3")).isEqualTo(Arrays.asList(""));

        assertThat(request.hasBody()).isTrue();
        assertThat(request.getBody()).isNotEmpty().hasSize(2);
        assertThat(request.getBody().get("body_attribute1")).isEqualTo(Arrays.asList("body_value1"));
        assertThat(request.getBody().get("body_attribute2")).isEqualTo(Arrays.asList("some value custom_value4"));
    }

    @Test
    public void getHttpMethod_getGETMethod_whenMethodBlank() {
        // Given
        AuthenticationCustomValueConfiguration customUserConfiguration = new AuthenticationCustomValueConfiguration()
                .withMethod(null);

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void getHttpMethod_getGETMethod_whenMethodIsGet() {
        // Given
        AuthenticationCustomValueConfiguration customUserConfiguration = new AuthenticationCustomValueConfiguration()
                .withMethod("get");

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void getHttpMethod_getGETMethod_whenMethodIsNeitherGetNorPost() {
        // Given
        AuthenticationCustomValueConfiguration customUserConfiguration = new AuthenticationCustomValueConfiguration()
                .withMethod("neither-get-nor-post");

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void getHttpMethod_getPostMethod_whenMethodIsPost() {
        // Given
        AuthenticationCustomValueConfiguration customUserConfiguration = new AuthenticationCustomValueConfiguration()
                .withMethod("post");

        // When

        // Then
        HttpMethod httpMethod = customUserConfiguration.getHttpMethod();
        assertThat(httpMethod).isEqualTo(HttpMethod.POST);
    }
}
