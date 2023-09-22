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

package com.decathlon.ara.service.dto.execution;

public class ExecutionCriteriaDTO {

    String country;
    String exception;
    String feature;
    String handling;
    Long problem;
    String scenario;
    boolean scenarioDetails;
    String severity;
    String step;
    Long team;
    String type;
    boolean withSucceed;

    public String getCountry() {
        return country;
    }

    public String getException() {
        return exception;
    }

    public String getFeature() {
        return feature;
    }

    public String getHandling() {
        return handling;
    }

    public Long getProblem() {
        return problem;
    }

    public String getScenario() {
        return scenario;
    }

    public boolean isScenarioDetails() {
        return scenarioDetails;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStep() {
        return step;
    }

    public Long getTeam() {
        return team;
    }

    public String getType() {
        return type;
    }

    public boolean isWithSucceed() {
        return withSucceed;
    }

    public void setWithSucceed(boolean withSucceed) {
        this.withSucceed = withSucceed;
    }
}
