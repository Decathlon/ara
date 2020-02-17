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

public enum QualityStatus {

    /**
     * Either not all blocking runs did not execute, or they did not produce the needed JSON reports (yet?).
     */
    INCOMPLETE,

    /**
     * The cycle is complete (all runs having the includeInThresholds flag set are status=DONE),
     * and quality thresholds were not met.
     */
    FAILED,

    /**
     * The cycle is complete (all runs having the includeInThresholds flag set are status=DONE),
     * and quality thresholds were met (above error thresholds), but still under warning thresholds.
     */
    WARNING,

    /**
     * The cycle is complete (all runs having the includeInThresholds flag set are status=DONE),
     * and quality thresholds were met (above both error and warning thresholds).
     */
    PASSED;

    /**
     * @return true if the quality is enough to make the build eligible (status is WARNING or PASSED)
     */
    public boolean isAcceptable() {
        return this == WARNING || this == PASSED;
    }

}
