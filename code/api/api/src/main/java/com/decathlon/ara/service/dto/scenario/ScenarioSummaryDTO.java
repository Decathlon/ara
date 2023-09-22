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

public class ScenarioSummaryDTO {

    private Long id;
    private SourceDTO source;
    private String featureFile;
    private String featureName;
    private String name;

    private int functionalityCount;
    private boolean hasCountryCodes;
    private boolean hasSeverity;

    private String wrongFunctionalityIds;
    private String wrongCountryCodes;
    private String wrongSeverityCode;

    public Long getId() {
        return id;
    }

    public SourceDTO getSource() {
        return source;
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getName() {
        return name;
    }

    public int getFunctionalityCount() {
        return functionalityCount;
    }

    public boolean isHasCountryCodes() {
        return hasCountryCodes;
    }

    public boolean isHasSeverity() {
        return hasSeverity;
    }

    public String getWrongFunctionalityIds() {
        return wrongFunctionalityIds;
    }

    public String getWrongCountryCodes() {
        return wrongCountryCodes;
    }

    public String getWrongSeverityCode() {
        return wrongSeverityCode;
    }

}
