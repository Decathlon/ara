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

package com.decathlon.ara.service.dto.run;

import java.util.Map;

import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.domain.Team;

public class RunWithQualitiesDTO extends RunDTO {

    /**
     * Key: {@link Severity#code};<br>
     * Value: the totals of scenarios for this severity for the run.
     */
    private Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity;

    /**
     * Key 1: {@link Team#id} as a String;<br>
     * Key 2: {@link Severity#code};<br>
     * Value: the totals of scenarios for this team and severity for the run.
     */
    private Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> qualitiesPerTeamAndSeverity;

    public Map<String, ExecutedScenarioHandlingCountsDTO> getQualitiesPerSeverity() {
        return qualitiesPerSeverity;
    }

    public void setQualitiesPerSeverity(Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity) {
        this.qualitiesPerSeverity = qualitiesPerSeverity;
    }

    public Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> getQualitiesPerTeamAndSeverity() {
        return qualitiesPerTeamAndSeverity;
    }

    public void setQualitiesPerTeamAndSeverity(
                                               Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> qualitiesPerTeamAndSeverity) {
        this.qualitiesPerTeamAndSeverity = qualitiesPerTeamAndSeverity;
    }

}
