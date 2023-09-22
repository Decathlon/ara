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

package com.decathlon.ara.domain.projection;

import com.decathlon.ara.domain.Source;

public class ScenarioSummary {

    private Long id;
    private Source source;
    private String featureFile;
    private String featureName;
    private String name;

    private long functionalityCount;
    private boolean hasCountryCodes;
    private boolean hasSeverity;

    private String wrongFunctionalityIds;
    private String wrongCountryCodes;
    private String wrongSeverityCode;

    public ScenarioSummary() {
    }

    public ScenarioSummary(Long id, Source source, String featureFile, String featureName, String name,
            long functionalityCount, boolean hasCountryCodes, boolean hasSeverity, String wrongFunctionalityIds,
            String wrongCountryCodes, String wrongSeverityCode) {
        this.id = id;
        this.source = source;
        this.featureFile = featureFile;
        this.featureName = featureName;
        this.name = name;
        this.functionalityCount = functionalityCount;
        this.hasCountryCodes = hasCountryCodes;
        this.hasSeverity = hasSeverity;
        this.wrongFunctionalityIds = wrongFunctionalityIds;
        this.wrongCountryCodes = wrongCountryCodes;
        this.wrongSeverityCode = wrongSeverityCode;
    }

    public Long getId() {
        return id;
    }

    public Source getSource() {
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

    public long getFunctionalityCount() {
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
