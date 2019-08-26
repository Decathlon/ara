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

package com.decathlon.ara.domain.enumeration;

/**
 * The handling state of a scenario: if at least one erroneous step has one open or closed (but not reappeared) problem,
 * the errors are handled. When an action is needed to attain this state, the scenarion is unhandled. Scenarios without
 * errors are, of course, successes.
 */
public enum Handling {

    /**
     * The scenario has no error.<br>
     * No need to do any action on the scenario.
     */
    SUCCESS,

    /**
     * The scenario has at least one error with an assigned problem that is open, or is closed but not reappeared.<br>
     * No need to do any further action: the errors are already handled by in-progress problems and defects.
     */
    HANDLED,

    /**
     * The scenario has only errors without any assigned problem, or all problems reappeared after closing.<br>
     * An action is required: either append a problem to one of the scenario's error, or reopen one
     * closed-but-reappeared problem/defect.
     */
    UNHANDLED

}
