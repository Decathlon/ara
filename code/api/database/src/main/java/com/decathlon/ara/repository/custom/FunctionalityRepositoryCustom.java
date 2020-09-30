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
import java.util.Map;

public interface FunctionalityRepositoryCustom {

    Map<Long, Long> getFunctionalityTeamIds(long projectId);

    /**
     * @param projectId the ID of the project in which to work
     * @param countryCode a {@link Country#code}
     * @return true if and only if at least one functionality has this country code among its {@link Scenario#countryCodes}
     */
    boolean existsByProjectIdAndCountryCode(long projectId, String countryCode);

}
