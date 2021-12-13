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

package com.decathlon.ara.service.dto.problempattern;

import javax.validation.constraints.Size;

import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;

public class ProblemPatternDTO {

    private Long id;

    @Size(max = 256, message = "The feature file must not exceed {max} characters.")
    private String featureFile;

    @Size(max = 256, message = "The feature must not exceed {max} characters.")
    private String featureName;

    @Size(max = 512, message = "The scenario must not exceed {max} characters.")
    private String scenarioName;

    private boolean scenarioNameStartsWith;

    @Size(max = 2048, message = "The step must not exceed {max} characters.")
    private String step;

    private boolean stepStartsWith;

    @Size(max = 2048, message = "The step definition must not exceed {max} characters.")
    private String stepDefinition;

    private boolean stepDefinitionStartsWith;

    private String exception;

    @Size(max = 32, message = "The release must not exceed {max} characters.")
    private String release;

    private CountryDTO country;

    private TypeWithSourceDTO type;

    private Boolean typeIsBrowser;

    private Boolean typeIsMobile;

    @Size(max = 32, message = "The platform must not exceed {max} characters.")
    private String platform;

    public ProblemPatternDTO() {
    }

    public ProblemPatternDTO(
            String stepDefinition,
            String exception) {
        this.stepDefinition = stepDefinition;
        this.exception = exception;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public boolean isScenarioNameStartsWith() {
        return scenarioNameStartsWith;
    }

    public String getStep() {
        return step;
    }

    public boolean isStepStartsWith() {
        return stepStartsWith;
    }

    public String getStepDefinition() {
        return stepDefinition;
    }

    public boolean isStepDefinitionStartsWith() {
        return stepDefinitionStartsWith;
    }

    public String getException() {
        return exception;
    }

    public String getRelease() {
        return release;
    }

    public CountryDTO getCountry() {
        return country;
    }

    public TypeWithSourceDTO getType() {
        return type;
    }

    public Boolean getTypeIsBrowser() {
        return typeIsBrowser;
    }

    public Boolean getTypeIsMobile() {
        return typeIsMobile;
    }

    public String getPlatform() {
        return platform;
    }

}
