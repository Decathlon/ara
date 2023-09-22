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

public class IgnoredScenario {

    private Source source;
    private String featureFile;
    private String featureName;
    private String severity;
    private String name;

    public IgnoredScenario() {
    }

    public IgnoredScenario(Source source, String featureFile, String featureName, String severity, String name) {
        this.source = source;
        this.featureFile = featureFile;
        this.featureName = featureName;
        this.severity = severity;
        this.name = name;
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

    public String getSeverity() {
        return severity;
    }

    public String getName() {
        return name;
    }

}
