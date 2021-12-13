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
public class Url {

    /**
     * Protocol (eg. "http" or "https").
     */
    private String protocol;

    /**
     * Port (eg. "8080" or "{{server_port}}").
     */
    private String port;

    /**
     * Path (eg. [ "the", "path" ] for "/the/path").
     */
    private String[] path;

    /**
     * Host (eg. [ "example", "org" ] for "example.org").
     */
    private String[] host;

    /**
     * All encoded query parameters (eg. "parameter: foo%26bar" for parameter "parameter"="foo&bar").
     */
    private KeyValue[] query;

    public String getProtocol() {
        return protocol;
    }

    public String getPort() {
        return port;
    }

    public String[] getPath() {
        return path;
    }

    public String[] getHost() {
        return host;
    }

    public KeyValue[] getQuery() {
        return query;
    }

}
