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

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutedScenarioDTO {

    private Long id;

    private String featureFile;

    private String featureName;

    private String featureTags;

    private String tags;

    private String severity;

    private String name;

    private String cucumberId;

    private int line;

    private String content;

    private Date startDateTime;

    private String screenshotUrl;

    private String videoUrl;

    private String logsUrl;

    private String httpRequestsUrl;

    private String javaScriptErrorsUrl;

    private String diffReportUrl;

    private String cucumberReportUrl;

    private String apiServer;

    private String seleniumNode;

}
