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

package com.decathlon.ara.service.dto.executedscenario;

import java.util.List;
import java.util.Set;

import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutionDTO;

public class ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO extends ExecutedScenarioDTO {

    private Handling handling;

    private RunWithExecutionDTO run;

    /**
     * All the teams associated to the functionalities associated to the scenario
     */
    private Set<Long> teamIds;

    private List<ErrorWithProblemsDTO> errors;

    public Handling getHandling() {
        return handling;
    }

    public RunWithExecutionDTO getRun() {
        return run;
    }

    public Set<Long> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(Set<Long> teamIds) {
        this.teamIds = teamIds;
    }

    public List<ErrorWithProblemsDTO> getErrors() {
        return errors;
    }

}
