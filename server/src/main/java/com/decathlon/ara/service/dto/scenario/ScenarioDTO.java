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

package com.decathlon.ara.service.dto.scenario;

import com.decathlon.ara.service.dto.source.SourceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ScenarioDTO {

    private Long id;

    private SourceDTO source;

    private String featureFile;

    private String featureName;

    private String featureTags;

    private String tags;

    private boolean ignored;

    private String countryCodes;

    private String severity;

    private String name;

    private String wrongFunctionalityIds;

    private String wrongCountryCodes;

    private String wrongSeverityCode;

    private int line;

    private String content;
}
