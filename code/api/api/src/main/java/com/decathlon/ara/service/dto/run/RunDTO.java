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

package com.decathlon.ara.service.dto.run;

import java.util.Date;

import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;

public class RunDTO {

    private Long id;

    private CountryDTO country;

    private TypeWithSourceDTO type;

    private String comment;

    private String platform;

    private String jobUrl;

    private JobStatus status;

    private String countryTags;

    private String severityTags;

    private Boolean includeInThresholds;

    private Date startDateTime;

    private Long estimatedDuration;

    private Long duration;

    public Long getId() {
        return id;
    }

    public CountryDTO getCountry() {
        return country;
    }

    public TypeWithSourceDTO getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public String getPlatform() {
        return platform;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public JobStatus getStatus() {
        return status;
    }

    public String getCountryTags() {
        return countryTags;
    }

    public String getSeverityTags() {
        return severityTags;
    }

    public Boolean getIncludeInThresholds() {
        return includeInThresholds;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    public Long getDuration() {
        return duration;
    }

}
