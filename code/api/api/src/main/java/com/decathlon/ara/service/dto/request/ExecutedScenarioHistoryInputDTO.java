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

package com.decathlon.ara.service.dto.request;

import java.time.Period;
import java.util.Optional;

public class ExecutedScenarioHistoryInputDTO {

    private String cucumberId;

    private String cycleName;

    private String branch;

    private String countryCode;

    private String runTypeCode;

    private ExecutedScenarioHistoryDuration duration;

    public Optional<Period> getDuration() {
        if (duration == null) {
            return Optional.empty();
        }
        return duration.getDuration();
    }

    public String getCucumberId() {
        return cucumberId;
    }

    public String getCycleName() {
        return cycleName;
    }

    public String getBranch() {
        return branch;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRunTypeCode() {
        return runTypeCode;
    }
}