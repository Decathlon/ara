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
 * A problem can be OPEN or CLOSED in database. But if it is CLOSED and the problem reappeared after its closing date,
 * the effective status is REAPPEARED, hiding the CLOSED state until it is reopened.
 */
public enum EffectiveProblemStatus {

    /**
     * The problem resolution is in-progress (and can have an open defect).
     */
    OPEN,

    /**
     * The problem is resolved (and can have a closed defect) and did not reappear after the closing date.
     */
    CLOSED,

    /**
     * The problem is resolved (and can have a closed defect) BUT did reappear after the closing date.
     */
    REAPPEARED

}
