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

package com.decathlon.ara.scenario.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Execution {

    /**
     * Object with only the UUID of the request, to map a request's ID with its execution.
     */
    private ItemId item;

    /**
     * Description of the executed request (not for a folder).
     */
    private Request request;

    /**
     * The actual HTTP response of the request.
     */
    private Response response;

    /**
     * All executed assertions on this request (both from the request test-script and parent folders test-scripts).<br>
     * The list of assertions is created by JavaScript: to list all assertions, there is no other way than executing the collection.
     */
    private Assertion[] assertions;

    public ItemId getItem() {
        return item;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public Assertion[] getAssertions() {
        return assertions;
    }

}
