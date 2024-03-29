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

package com.decathlon.ara.scenario.generic.bean.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericExecutedScenarioLogs {

    @JsonProperty("error")
    private String errorStacktraceUrl;

    @JsonProperty("scenario")
    private String executedScenarioUrl;

    @JsonProperty("diff_report")
    private String diffReportUrl;

    @JsonProperty("trace")
    private String executionTraceUrl;

    public String getErrorStacktraceUrl() {
        return errorStacktraceUrl;
    }

    public String getExecutedScenarioUrl() {
        return executedScenarioUrl;
    }

    public String getDiffReportUrl() {
        return diffReportUrl;
    }

    public String getExecutionTraceUrl() {
        return executionTraceUrl;
    }
}
