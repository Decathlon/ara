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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Failure {

    /**
     * The pre-request script thrown an error and is faulty.
     */
    public static final String AT_PRE_REQUEST_SCRIPT = "prerequest-script";

    /**
     * The test script thrown an error outside of an assertion and is faulty.
     */
    public static final String AT_TEST_SCRIPT = "test-script";

    /**
     * The error message & stacktrace of the failure, optionaly with its assertion index if the failure is related to an assertion execution.
     */
    private Error error;

    /**
     * Eg.:
     * <ul>
     * <li>"assertion:0 in test-script" (first assertion of the execution)</li>
     * <li>"assertion:2 in test-script" (third assertion of the execution)</li>
     * <li>AT_PRE_REQUEST (the pre-request script thrown an error and is faulty)</li>
     * <li>AT_TEST_SCRIPT (the test script thrown an error outside of an assertion and is faulty)</li>
     * </ul>
     */
    private String at;

    /**
     * Used to map the UUID of the request with its execution and failure(s).<br>
     * Should be an Item, but it duplicates a lot of information, so we retrieve only id here
     */
    private Source source;

}
