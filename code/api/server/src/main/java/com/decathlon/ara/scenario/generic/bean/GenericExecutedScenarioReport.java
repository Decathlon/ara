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

package com.decathlon.ara.scenario.generic.bean;

import com.decathlon.ara.scenario.generic.bean.description.GenericExecutedScenarioDescription;
import com.decathlon.ara.scenario.generic.bean.display.GenericExecutedScenarioResultsDisplay;
import com.decathlon.ara.scenario.generic.bean.error.GenericExecutedScenarioError;
import com.decathlon.ara.scenario.generic.bean.feature.GenericExecutedScenarioFeature;
import com.decathlon.ara.scenario.generic.bean.log.GenericExecutedScenarioLogs;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericExecutedScenarioReport {

    private String code;

    private String name;

    private GenericExecutedScenarioFeature feature;

    private GenericExecutedScenarioDescription description;

    @JsonProperty("start")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date startDate;

    private List<GenericExecutedScenarioError> errors;

    private List<Long> cartography;

    private GenericExecutedScenarioResultsDisplay display;

    private GenericExecutedScenarioLogs logs;

    private List<String> tags;

    private String severity;

    @JsonProperty("server")
    private String serverName;

    private String comment;
}
