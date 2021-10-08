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

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;

import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ExecutedScenarioRepositoryCustom {

    List<ExecutedScenario> findHistory(long projectId, String cucumberId, String branch, String cycleName, String countryCode, String runTypeCode, Optional<Period> duration);

    /**
     * @param runIds the IDs of the Runs where to find ExecutedScenarios
     * @return all executed-scenario of the runs, with minimal information (id, runId, name, severity) and count of errors and problem-patterns
     */
    // NO projectId: runIds is already restrained to the correct project
    List<ExecutedScenarioWithErrorAndProblemJoin> findAllErrorAndProblemCounts(Set<Long> runIds);

}
