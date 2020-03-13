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

package com.decathlon.ara.postman.model;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.postman.bean.Execution;
import com.decathlon.ara.postman.bean.Failure;
import com.decathlon.ara.postman.bean.Item;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewmanScenario {

    /**
     * The failures of the item's execution: never null, but can be empty if no failure happened.
     */
    private final List<Failure> failures = new ArrayList<>();

    /**
     * The resulting ExecutedScenario currently built by the 3 Newman report elements relative to this folder or request and its execution and optional failure.
     */
    private ExecutedScenario scenario;

    /**
     * A folder or request, never null.
     */
    private Item item;

    /**
     * The execution of the item.<br>
     * Can be null if it had not got a chance to execute (request was in a not-ran folder, or item is a folder).<br>
     * BUT we remove all NewmanScenarios without any execution early on in the processing, so in most processing code, execution will not be null.
     */
    private Execution execution;

}
