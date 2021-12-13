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
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Body {

    /**
     * Either:
     * <ul>
     * <li>"formdata": the {@code formData} property has fields sent to the server</li>
     * <li>"urlencoded": the {@code urlEncoded} property has fields sent to the server</li>
     * <li>"raw": the {@code raw} property has the body sent to the server, as a String (usually a JSON content)</li>
     * <li>"file": the file is not saved in the Postman exported collection, so this cannot be played by Newman</li>
     * <li>null: the request has no body</li>
     * </ul>
     */
    private String mode;

    /**
     * @see #mode
     */
    @JsonProperty("formdata")
    private KeyValue[] formData;

    /**
     * @see #mode
     */
    @JsonProperty("urlencoded")
    private KeyValue[] urlEncoded;

    /**
     * @see #mode
     */
    private String raw;

    public String getMode() {
        return mode;
    }

    public KeyValue[] getFormData() {
        return formData;
    }

    public KeyValue[] getUrlEncoded() {
        return urlEncoded;
    }

    public String getRaw() {
        return raw;
    }

}
