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
 * When a defect (frm an external defect tracking system) is assigned to a problem, its presence is checked.<br>
 * This is the result of that check.
 */
public enum DefectExistence {

    /**
     * The defect tracking system was not responding when creating/updating the problem while assigning a new defect ID:
     * defect status will get queried again on next indexing.
     */
    UNKNOWN,

    /**
     * The defect ID assigned to a problem has been found in the defect tracking system.
     */
    EXISTS,

    /**
     * The defect ID assigned to a problem has NOT been found in the defect tracking system.<br>
     * This should forbid problem creation or update, but if the defect tracking system cannot be contacted in such
     * case, creation/update is still validated, and defect presence is validated asynchronously: nonexistence is then
     * reported later.
     */
    NONEXISTENT

}
