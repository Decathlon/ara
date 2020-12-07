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

import com.decathlon.ara.configuration.authentication.clients.custom.AuthenticationCustomValueConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationCustomUserConfiguration extends AuthenticationCustomValueConfiguration {

    private String method;

    private AuthenticationCustomUserFieldsConfiguration fields;

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