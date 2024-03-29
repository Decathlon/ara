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

package com.decathlon.ara.scenario.generic.bean.display;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericExecutedScenarioResultsDisplay {

    @JsonProperty("video")
    private String videoUrl;

    @JsonProperty("image")
    private String screenshotUrl;

    @JsonProperty("other")
    private String otherResultsDisplayUrl;

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public String getOtherResultsDisplayUrl() {
        return otherResultsDisplayUrl;
    }
}
