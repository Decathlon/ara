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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.Period;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class ExecutedScenarioHistoryInputDTO {

    private String cucumberId;

    private String cycleName;

    private String branch;

    private String countryCode;

    private String runTypeCode;

    private ExecutedScenarioHistoryDuration duration;

    public Optional<Period> getDuration() {
        if (duration == null || duration.type == null || duration.value < 1) {
            return Optional.empty();
        }
        Period period = Period.ZERO;
        var durationType = duration.getType();
        var durationValue = duration.getValue();
        switch (durationType) {
            case DAY:
                period = Period.ofDays(durationValue);
                break;
            case WEEK:
                period = Period.ofWeeks(durationValue);
                break;
            case MONTH:
                period = Period.ofMonths(durationValue);
                break;
            case YEAR:
                period = Period.ofYears(durationValue);
                break;
        }
        return Optional.of(period);
    }

    @Data
    private class ExecutedScenarioHistoryDuration {
        private int value;
        private ExecutedScenarioHistoryDurationType type;
    }

    private enum ExecutedScenarioHistoryDurationType {
        DAY, WEEK, MONTH, YEAR
    }

}
