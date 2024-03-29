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

package com.decathlon.ara.ci.bean;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CycleDef {

    /**
     * True if this cycle should discard the version if we are under NRT quality thresholds.
     */
    private boolean blockingValidation;

    /**
     * For each integration platform, list all countries that must be deployed and what tests should be run.
     */
    @JsonProperty("platforms_rules")
    private Map<String, List<PlatformRule>> platformsRules;

    /**
     * Eg. { "high": { "failure": 80, "warning": 85 }, "sanity-check": { "failure": 90, "warning": 95 } }
     */
    private Map<String, QualityThreshold> qualityThresholds;
    
    public CycleDef() {
    }

    public CycleDef(boolean blockingValidation, Map<String, List<PlatformRule>> platformsRules, Map<String, QualityThreshold> qualityThresholds) {
        this.blockingValidation = blockingValidation;
        this.platformsRules = platformsRules;
        this.qualityThresholds = qualityThresholds;
    }

    public boolean isBlockingValidation() {
        return blockingValidation;
    }

    public Map<String, List<PlatformRule>> getPlatformsRules() {
        return platformsRules;
    }

    public Map<String, QualityThreshold> getQualityThresholds() {
        return qualityThresholds;
    }

}
