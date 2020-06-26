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
public class Error {

    /**
     * Eg. "AssertionError" (usually present at the start of {@link #stack},
     * but if stack is null, name usually contains things like "JSONError",
     * so we can concatenate name and message to get the same result).
     */
    private String name;

    /**
     * The index (0-based) of the assertion among all assertions of the current request (merging request's test's assertions followed by parent folders' test's assertions) if and only if it failed on an assertion.
     * If the JavaScript is faulty and thrown an exception outside assertions, this field is null.
     */
    private Integer index;

    /**
     * Eg. "expected false to be true" (usually present at the middle of {@link #stack},
     * but if stack is null, name usually contains things like "Unexpected token 'j' at 1:1\njava.lang.NullPointerException\n^",
     * so we can concatenate name and message to get the same result).
     */
    private String message;

    /**
     * Eg. "AssertionError: expected false to be true\n   at Object.eval sandbox-script.js:3:23)". May be null in some error cases (like "JSONError: Unexpected token...": see name and message in this case.
     */
    private String stack;

}
