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

package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ErrorRepositoryCustom {

    void assignPatternToErrors(long projectId, ProblemPattern pattern);

    // NO projectId: errors is already restrained to the correct project
    Map<Error, List<Problem>> getErrorsProblems(Collection<Error> errors);

    /**
     * When new errors get indexed into ARA, this method will assign them existing problems if at least one of the
     * problems's patterns match the errors.
     *
     * @param projectId the ID of the project in which to work
     * @param errorIds  the IDs of the new errors that were just created
     * @return all problems that were assigned one of the given new errors by this method
     */
    Set<Problem> autoAssignProblemsToNewErrors(long projectId, List<Long> errorIds);

}
