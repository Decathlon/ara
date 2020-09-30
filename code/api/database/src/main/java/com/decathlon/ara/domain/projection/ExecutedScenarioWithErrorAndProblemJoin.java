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

package com.decathlon.ara.domain.projection;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * A join of {@link ExecutedScenario}, {@link Error} and {@link Problem}: provide a few information about the scenario,
 * with handled (with not reappeared problem) and unhandled (without problem, or with reappeared problem) errors, if
 * any.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@With
public class ExecutedScenarioWithErrorAndProblemJoin {

    /**
     * The {@link ExecutedScenario#id} of this entity.
     */
    private long id;

    /**
     * The {@link Run#id} in which this scenario is.
     */
    private long runId;

    /**
     * The {@link Severity#code} of this scenario (can be a code not existing in database: it is user-provided).
     */
    private String severity;

    /**
     * The name of this scenario.
     */
    private String name;

    /**
     * Is greater than 0 if there are any unhandled errors for this scenario.<br>
     * If both {@code unhandledCount} and {@link #handledCount} are greater than 1, the scenario is considered handled.<br>
     * If both {@code unhandledCount} and {@link #handledCount} are 0, the scenario is successful (it has no error).
     */
    private long unhandledCount;

    /**
     * Is greater than 0 if there are any handled errors for this scenario.<br>
     * If both {@link #unhandledCount} and {@code handledCount} are greater than 1, the scenario is considered handled.<br>
     * If both {@link #unhandledCount} and {@code handledCount} are 0, the scenario is successful (it has no error).
     */
    private long handledCount;

}
