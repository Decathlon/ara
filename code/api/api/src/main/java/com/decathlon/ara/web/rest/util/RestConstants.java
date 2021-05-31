/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
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

package com.decathlon.ara.web.rest.util;

import lombok.experimental.UtilityClass;

import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;

@UtilityClass
public final class RestConstants {

    public static final String AUTH_PATH = "/auth";

    /**
     * The root path of all REST API resources.
     */
    public static final String API_PATH = "/api";

    public static final String PROJECT_CODE_REQUEST_PARAMETER = "{projectCode:" + CODE_PATTERN + "}";

    /**
     * The root path of all REST API resources requiring the context of a project to be able to operate.
     */
    public static final String PROJECT_API_PATH = API_PATH + "/projects/" + PROJECT_CODE_REQUEST_PARAMETER;

}
