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

package com.decathlon.ara.service.dto.response;

import java.util.List;

import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;

public class DistinctStatisticsDTO {

    private List<String> releases;
    private List<CountryDTO> countries;
    private List<TypeWithSourceDTO> types;
    private List<String> platforms;
    private List<String> featureNames;
    private List<String> featureFiles;
    private List<String> scenarioNames;
    private List<String> steps;
    private List<String> stepDefinitions;

    public List<String> getReleases() {
        return releases;
    }

    public void setReleases(List<String> releases) {
        this.releases = releases;
    }

    public List<CountryDTO> getCountries() {
        return countries;
    }

    public void setCountries(List<CountryDTO> countries) {
        this.countries = countries;
    }

    public List<TypeWithSourceDTO> getTypes() {
        return types;
    }

    public void setTypes(List<TypeWithSourceDTO> types) {
        this.types = types;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public List<String> getFeatureNames() {
        return featureNames;
    }

    public void setFeatureNames(List<String> featureNames) {
        this.featureNames = featureNames;
    }

    public List<String> getFeatureFiles() {
        return featureFiles;
    }

    public void setFeatureFiles(List<String> featureFiles) {
        this.featureFiles = featureFiles;
    }

    public List<String> getScenarioNames() {
        return scenarioNames;
    }

    public void setScenarioNames(List<String> scenarioNames) {
        this.scenarioNames = scenarioNames;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public List<String> getStepDefinitions() {
        return stepDefinitions;
    }

    public void setStepDefinitions(List<String> stepDefinitions) {
        this.stepDefinitions = stepDefinitions;
    }

}
