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

package com.decathlon.ara.service.dto.quality;

import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.service.dto.severity.SeverityDTO;

public class QualitySeverityDTO {

    private SeverityDTO severity;

    private ScenarioCountDTO scenarioCounts;

    /**
     * Deduced from {@code scenarioCounts}.
     */
    private int percent;

    /**
     * Deduced from {@code percent} and the quality thresholds of this {@code severity} at the time of execution.
     */
    private QualityStatus status;

    public SeverityDTO getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityDTO severity) {
        this.severity = severity;
    }

    public ScenarioCountDTO getScenarioCounts() {
        return scenarioCounts;
    }

    public void setScenarioCounts(ScenarioCountDTO scenarioCounts) {
        this.scenarioCounts = scenarioCounts;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public QualityStatus getStatus() {
        return status;
    }

    public void setStatus(QualityStatus status) {
        this.status = status;
    }

}
