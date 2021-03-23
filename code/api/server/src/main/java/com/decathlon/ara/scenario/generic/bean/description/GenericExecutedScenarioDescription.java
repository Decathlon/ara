/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.scenario.generic.bean.description;

import com.decathlon.ara.scenario.generic.bean.description.step.GenericExecutedScenarioStep;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericExecutedScenarioDescription {

    private List<GenericExecutedScenarioStep> steps;

    @JsonProperty("start_line")
    private Integer startLineNumber;

    /**
     * Get a string displaying all the steps content
     * @return the steps content
     */
    public String getStepsContent() {
        if (CollectionUtils.isEmpty(steps)) {
            return "";
        }
        return steps.stream()
                .map(GenericExecutedScenarioStep::getStepLine)
                .collect(Collectors.joining("\n"));
    }
}
