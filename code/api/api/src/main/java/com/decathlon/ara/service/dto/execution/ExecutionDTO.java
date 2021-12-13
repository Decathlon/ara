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

package com.decathlon.ara.service.dto.execution;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.decathlon.ara.util.JsonUtil.StringToListDeserializer;
import com.decathlon.ara.util.JsonUtil.StringToMapDeserializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ExecutionDTO {

    private static final TypeReference<Map<String, QualityThreshold>> QUALITY_THRESHOLDS_TYPE_REF = new TypeReference<>() {
    };
    private static final TypeReference<List<QualitySeverityDTO>> QUALITY_SEVERITIES_TYPE_REF = new TypeReference<>() {
    };

    private Long id;

    private String branch;

    private String name;

    private String release;

    private String version;

    private Date buildDateTime;

    private Date testDateTime;

    private String jobUrl;

    private JobStatus status;

    private Result result;

    private ExecutionAcceptance acceptance;

    private String discardReason;

    private boolean blockingValidation;

    @JsonDeserialize(using = QualityThreholdsDeserializer.class)
    private Map<String, QualityThreshold> qualityThresholds;

    private QualityStatus qualityStatus;

    @JsonDeserialize(using = QualitySeveritiesDeserializer.class)
    private List<QualitySeverityDTO> qualitySeverities;

    private Long duration;

    private Long estimatedDuration;

    public Long getId() {
        return id;
    }

    public String getBranch() {
        return branch;
    }

    public String getName() {
        return name;
    }

    public String getRelease() {
        return release;
    }

    public String getVersion() {
        return version;
    }

    public Date getBuildDateTime() {
        return buildDateTime;
    }

    public Date getTestDateTime() {
        return testDateTime;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Result getResult() {
        return result;
    }

    public ExecutionAcceptance getAcceptance() {
        return acceptance;
    }

    public String getDiscardReason() {
        return discardReason;
    }

    public boolean isBlockingValidation() {
        return blockingValidation;
    }

    public Map<String, QualityThreshold> getQualityThresholds() {
        return qualityThresholds;
    }

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public List<QualitySeverityDTO> getQualitySeverities() {
        return qualitySeverities;
    }

    public Long getDuration() {
        return duration;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    private static class QualityThreholdsDeserializer extends StringToMapDeserializer<String, QualityThreshold> {

        private static final long serialVersionUID = 1L;

        protected QualityThreholdsDeserializer() {
            super(QUALITY_THRESHOLDS_TYPE_REF);
        }

    }

    private static class QualitySeveritiesDeserializer extends StringToListDeserializer<QualitySeverityDTO> {

        private static final long serialVersionUID = 1L;

        protected QualitySeveritiesDeserializer() {
            super(QUALITY_SEVERITIES_TYPE_REF);
        }

    }

}
