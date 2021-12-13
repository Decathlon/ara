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

package com.decathlon.ara.domain;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.Result;

@Entity
public class CountryDeployment implements Comparable<CountryDeployment> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "country_deployment_id")
    @SequenceGenerator(name = "country_deployment_id", sequenceName = "country_deployment_id", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id")
    private Execution execution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id")
    private Country country;

    /**
     * The platform/environment/server on which this country was deployed.
     */
    private String platform;

    /**
     * The URL of the Continuous Integration job, visible in the client GUI to access logs of the job.
     */
    @Column(length = 512)
    private String jobUrl;

    /**
     * An alternate URL for the job, only for internal indexing needs (optional: either the local directory from which
     * to index or an intermediary service used to eg. compute the Continuous Integration job's hierarchy).
     */
    private String jobLink;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    /**
     * The result status of the remote job.
     */
    @Enumerated(EnumType.STRING)
    private Result result;

    /**
     * The date and time the remote job started. Null if not started yet.
     */
    @Column(name = "start_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;

    /**
     * The estimated duration of the remote job, in milliseconds: can be used with startDateTime and the current
     * date-time to display a progress bar.
     */
    private Long estimatedDuration;

    /**
     * The actual duration of the job, in milliseconds, AFTER it has finished (may be 0 while running).
     */
    private Long duration;

    @Override
    public int compareTo(CountryDeployment other) {
        Comparator<CountryDeployment> executionIdComparator = comparing(CountryDeployment::getExecutionId, nullsFirst(naturalOrder()));
        Comparator<CountryDeployment> countryComparator = comparing(CountryDeployment::getCountry, nullsFirst(naturalOrder()));
        return nullsFirst(executionIdComparator
                .thenComparing(countryComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, getExecutionId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CountryDeployment)) {
            return false;
        }
        CountryDeployment other = (CountryDeployment) obj;
        return Objects.equals(country, other.country) && Objects.equals(getExecutionId(), other.getExecutionId());
    }

    public Long getId() {
        return id;
    }

    public Long getExecutionId() {
        return execution == null ? null : execution.getId();
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getJobLink() {
        return jobLink;
    }

    public void setJobLink(String jobLink) {
        this.jobLink = jobLink;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

}
