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

import java.util.ArrayList;
import java.util.List;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Type;

public class ProblemAggregate {

    /**
     * Eg. "-----OOEOE" showing the last x executions with '-' being a nonexistent execution, 'O' the problem did not appear (OK) and 'E' the
     * problem appears (ERROR).
     */
    private final List<CycleStability> cycleStabilities = new ArrayList<>();

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
    private Country firstCountry;

    private long typeCount;
    private Type firstType;

    private long platformCount;
    private String firstPlatform;

    public List<CycleStability> getCycleStabilities() {
        return cycleStabilities;
    }

    public long getPatternCount() {
        return patternCount;
    }

    public void setPatternCount(long patternCount) {
        this.patternCount = patternCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getScenarioCount() {
        return scenarioCount;
    }

    public void setScenarioCount(long scenarioCount) {
        this.scenarioCount = scenarioCount;
    }

    public String getFirstScenarioName() {
        return firstScenarioName;
    }

    public void setFirstScenarioName(String firstScenarioName) {
        this.firstScenarioName = firstScenarioName;
    }

    public long getBranchCount() {
        return branchCount;
    }

    public void setBranchCount(long branchCount) {
        this.branchCount = branchCount;
    }

    public String getFirstBranch() {
        return firstBranch;
    }

    public void setFirstBranch(String firstBranch) {
        this.firstBranch = firstBranch;
    }

    public long getReleaseCount() {
        return releaseCount;
    }

    public void setReleaseCount(long releaseCount) {
        this.releaseCount = releaseCount;
    }

    public String getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(String firstRelease) {
        this.firstRelease = firstRelease;
    }

    public long getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(long versionCount) {
        this.versionCount = versionCount;
    }

    public String getFirstVersion() {
        return firstVersion;
    }

    public void setFirstVersion(String firstVersion) {
        this.firstVersion = firstVersion;
    }

    public long getCountryCount() {
        return countryCount;
    }

    public void setCountryCount(long countryCount) {
        this.countryCount = countryCount;
    }

    public Country getFirstCountry() {
        return firstCountry;
    }

    public void setFirstCountry(Country firstCountry) {
        this.firstCountry = firstCountry;
    }

    public long getTypeCount() {
        return typeCount;
    }

    public void setTypeCount(long typeCount) {
        this.typeCount = typeCount;
    }

    public Type getFirstType() {
        return firstType;
    }

    public void setFirstType(Type firstType) {
        this.firstType = firstType;
    }

    public long getPlatformCount() {
        return platformCount;
    }

    public void setPlatformCount(long platformCount) {
        this.platformCount = platformCount;
    }

    public String getFirstPlatform() {
        return firstPlatform;
    }

    public void setFirstPlatform(String firstPlatform) {
        this.firstPlatform = firstPlatform;
    }

}
