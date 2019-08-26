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

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;
import java.util.List;

public interface ScenarioRepositoryCustom {

    /**
     * @param projectId the ID of the project in which to work
     * @return all scenarios that have no associated functionalities or have wrong or nonexistent functionality identifier
     */
    List<ScenarioSummary> findAllWithFunctionalityErrors(long projectId);

    /**
     * @param projectId the ID of the project in which to work
     * @return the count of ignored and not ignored scenarios for each source (API, Web...) and severity
     */
    List<ScenarioIgnoreCount> findIgnoreCounts(long projectId);

    /**
     * @param projectId the ID of the project in which to work
     * @return a summary of all ignored scenarios, ordered by source code, feature file and scenario name
     */
    List<IgnoredScenario> findIgnoredScenarios(long projectId);

    /**
     * @param projectId   the ID of the project in which to work
     * @param countryCode a {@link Country#code}
     * @return true if and only if at least one scenario has this country code among its {@link Scenario#countryCodes}
     */
    boolean existsByProjectIdAndCountryCode(long projectId, String countryCode);

}
