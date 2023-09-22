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

package com.decathlon.ara.service.dto.problem;

import java.util.ArrayList;
import java.util.List;

import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.stability.CycleStabilityDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;

public class ProblemAggregateDTO {

    private long patternCount;

    private long errorCount;

    private long scenarioCount;
    private String firstScenarioName;

    private long branchCount;
    private String firstBranch;

    private long releaseCount;
    private String firstRelease;

    private long versionCount;
    private String firstVersion;

    private long countryCount;
    private CountryDTO firstCountry;

    private long typeCount;
    private TypeWithSourceDTO firstType;

    private long platformCount;
    private String firstPlatform;

    private List<CycleStabilityDTO> cycleStabilities = new ArrayList<>();

    public long getPatternCount() {
        return patternCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public long getScenarioCount() {
        return scenarioCount;
    }

    public String getFirstScenarioName() {
        return firstScenarioName;
    }

    public long getBranchCount() {
        return branchCount;
    }

    public String getFirstBranch() {
        return firstBranch;
    }

    public long getReleaseCount() {
        return releaseCount;
    }

    public String getFirstRelease() {
        return firstRelease;
    }

    public long getVersionCount() {
        return versionCount;
    }

    public String getFirstVersion() {
        return firstVersion;
    }

    public long getCountryCount() {
        return countryCount;
    }

    public CountryDTO getFirstCountry() {
        return firstCountry;
    }

    public long getTypeCount() {
        return typeCount;
    }

    public TypeWithSourceDTO getFirstType() {
        return firstType;
    }

    public long getPlatformCount() {
        return platformCount;
    }

    public String getFirstPlatform() {
        return firstPlatform;
    }

    public List<CycleStabilityDTO> getCycleStabilities() {
        return cycleStabilities;
    }

}
