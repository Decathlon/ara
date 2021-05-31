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

package com.decathlon.ara.configuration.authentication.provider.custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationCustomValueConfiguration {

    protected String uri;

    protected String headerValues;

    protected String bodyValues;

    protected String method;

    /**
     * Parse the configuration file and return its representation as a map.
     * For instance a string 'attribute1|value1,attribute2|value2,attribute3' matches the following map:
     * - Entry 1 -> ("attribute1", "value1")
     * - Entry 2 -> ("attribute2", "value2")
     * - Entry 3 -> ("attribute3", "")
     * If the parameter map contains the following entry ("parameter_attribute", "value_to_insert")
     * and the raw configuration string is: 'attribute1|value1 {{parameter_attribute}},attribute2|value2,attribute3',
     * the map becomes:
     * - Entry 1 -> ("attribute1", "value1 value_to_insert")
     * - Entry 2 -> ("attribute2", "value2")
     * - Entry 3 -> ("attribute3", "")
     * @param rawValues the unprocessed configuration string
     * @param parameters the parameters to replace
     * @return a map containing these values
     */
    private Map<String, String> getValuesByAttributes(String rawValues, Map<String, String> parameters) {
        String[] splitRawValues = StringUtils.isNotBlank(rawValues) ? rawValues.split("\\|") : new String[0];
        Map<String, String> valuesByAttribute = Arrays.stream(splitRawValues)
                .map(rawValue -> rawValue.split(","))
                .filter(splitValue -> splitValue.length > 0)
                .collect(
                        Collectors.toMap(splitValue -> splitValue[0], splitValue -> splitValue.length == 2 ? splitValue[1] : "")
                );
        boolean hasParameters = !CollectionUtils.isEmpty(parameters);
        Map<String, String> formattedParameters = hasParameters ?
                parameters
                        .entrySet()
                        .stream()
                        .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                        .collect(
                                Collectors.toMap(entry -> String.format("{{%s}}", entry.getKey()), Map.Entry::getValue)
                        ) :
                new HashMap<>();
        for (Map.Entry<String, String> parameter: formattedParameters.entrySet()) {
            String stringToReplace = parameter.getKey();
            String value = parameter.getValue();
            valuesByAttribute = valuesByAttribute
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        final String rawValue = entry.getValue();
                        final String updatedValue = rawValue.replace(stringToReplace, value);
                        entry.setValue(updatedValue);
                        return entry;
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return valuesByAttribute;
    }

    /**
     * Get a request containing a header and a body, after parsing the configuration.
     * Also replace variables with their matching values, if needed.
     * @param parameters the parameters
     * @return the request
     */
    public HttpEntity<MultiValueMap<String, String>> getRequest(Map<String, String> parameters) {
        Map<String, String> bodyValuesByAttributes = getValuesByAttributes(bodyValues, parameters);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.setAll(bodyValuesByAttributes);

        Map<String, String> headerValuesByAttribute = getValuesByAttributes(headerValues, parameters);
        HttpHeaders header = new HttpHeaders();
        header.setAll(headerValuesByAttribute);
        return new HttpEntity<>(body, header);
    }

    /**
     * Get the HTTP method to use when calling the custom user API
     * @return the HTTP method
     */
    public HttpMethod getHttpMethod() {
        if (StringUtils.isBlank(method)) {
            return HttpMethod.GET;
        }
        String upperCasedMethod = method.toUpperCase();
        if ("POST".equals(upperCasedMethod)) {
            return HttpMethod.POST;
        }
        return HttpMethod.GET;
    }
}
