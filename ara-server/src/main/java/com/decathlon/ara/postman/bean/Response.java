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

package com.decathlon.ara.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    /**
     * The status line of the HTTP response (eg. "OK" or "Internal Server Error").
     */
    private String status;

    /**
     * The status code of the HTTP response (eg. 200 or 500).
     */
    private int code;

    /**
     * All the actual headers sent with the request (eg. "Content-Type: application/json"):
     * both configured in the collection and added by Newman during the session.
     */
    private KeyValue[] header;

    /**
     * The HTTP body of the response.
     */
    private Stream stream;

    /**
     * The time in milliseconds between request sent and response received.
     */
    private long responseTime;

}
