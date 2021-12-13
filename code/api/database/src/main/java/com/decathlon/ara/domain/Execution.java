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

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;

import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.domain.enumeration.Result;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "cycle_definition_id", "test_date_time" })
})
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "execution_id")
    @SequenceGenerator(name = "execution_id", sequenceName = "execution_id", allocationSize = 1)
    private Long id;

    @Column(length = 16)
    private String branch;

    @Column(length = 16)
    private String name;

    /**
     * The release of the version having been tested by this execution
     * (eg. "v2" or "1904" for the release encompassing all versions of April 2019).<br>
     * Optional: will not be filterable by problems if not provided.
     */
    @Column(length = 32)
    private String release;

    /**
     * The version having been tested by this execution
     * (eg. "1904.3" for the third version of the April 2019 release, a timestamped version, or the Git commit ID).<br>
     * Optional: for display only.
     */
    @Column(length = 64)
    private String version;

    /**
     * the date and time at which the version having been tested by this execution was created (compiled...).<br>
     * Optional: for display only.
     */
    @Column(name = "build_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date buildDateTime;

    /**
     * the date and time at which the execution was run on continuous Continuous Integration.<br>
     * Mandatory.
     */
    @Column(name = "test_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date testDateTime;

    /**
     * The URL of the Continuous Integration job, visible in the client GUI to access logs of the job.
     */
    @Column(length = 512, unique = true)
    private String jobUrl;

    /**
     * An alternate URL for the job, only for internal indexing needs (optional: either the local directory from which
     * to index or an intermediary service used to eg. compute the Continuous Integration job's hierarchy).
     */
    private String jobLink;

    /**
     * ARA's workflow-status of the execution (interpreted from the many statuses of the Continuous Integration job).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private JobStatus status;

    /**
     * The raw status of the Continuous Integration job for this execution.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Result result;

    /**
     * An execution is accepted, discarded (or not yet accepted nor discarded).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ExecutionAcceptance acceptance;

    /**
     * If the {@link #acceptance} is {@link ExecutionAcceptance#DISCARDED DISCARDED}, the mandatory reason explains why
     * a user discarded the execution (too many network errors, etc.).
     */
    @Column(length = 512)
    private String discardReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_definition_id")
    private CycleDefinition cycleDefinition;

    private Boolean blockingValidation;

    private String qualityThresholds;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private QualityStatus qualityStatus;

    @Column(length = 4096)
    private String qualitySeverities;

    /**
     * The actual duration of the remote job, in milliseconds, AFTER it has finished (may be 0 while running).
     */
    private Long duration;

    /**
     * The estimated duration of the remote job, in milliseconds: can be used with startDateTime and the current
     * date-time to display a progress bar.
     */
    private Long estimatedDuration;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "execution", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    // Run, Country & Type have compareTo(other) implementing "ORDER BY country.name ASC, type.name ASC"
    @SortNatural
    private Set<Run> runs = new TreeSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "execution", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @SortNatural
    private Set<CountryDeployment> countryDeployments = new TreeSet<>();

    public void addRun(Run run) {
        // Set the child-entity's foreign-key BEFORE adding the child-entity to the TreeSet,
        // as the foreign-key is required to place the child-entity in the right order (with child-entity's compareTo)
        // and is required not to change while the child-entity is in the TreeSet
        run.setExecution(this);
        runs.add(run);
    }

    public void addRuns(Iterable<? extends Run> runsToAdd) {
        for (Run run : runsToAdd) {
            addRun(run);
        }
    }

    @SuppressWarnings("squid:S2250") // Collection methods with O(n) performance should be used carefully
    public void removeRun(Run run) {
        runs.remove(run);
        run.setExecution(null);
    }

    public void addCountryDeployment(CountryDeployment countryDeployment) {
        // Set the child-entity's foreign-key BEFORE adding the child-entity to the TreeSet,
        // as the foreign-key is required to place the child-entity in the right order (with child-entity's compareTo)
        // and is required not to change while the child-entity is in the TreeSet
        countryDeployment.setExecution(this);
        this.countryDeployments.add(countryDeployment);
    }

    public void addCountryDeployments(Iterable<? extends CountryDeployment> countryDeploymentsToAdd) {
        for (CountryDeployment countryDeployment : countryDeploymentsToAdd) {
            addCountryDeployment(countryDeployment);
        }
    }

    public void removeCountryDeployment(CountryDeployment countryDeployment) {
        this.countryDeployments.remove(countryDeployment);
        countryDeployment.setExecution(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getBuildDateTime() {
        return buildDateTime;
    }

    public void setBuildDateTime(Date buildDateTime) {
        this.buildDateTime = buildDateTime;
    }

    public Date getTestDateTime() {
        return testDateTime;
    }

    public void setTestDateTime(Date testDateTime) {
        this.testDateTime = testDateTime;
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

    public ExecutionAcceptance getAcceptance() {
        return acceptance;
    }

    public void setAcceptance(ExecutionAcceptance acceptance) {
        this.acceptance = acceptance;
    }

    public String getDiscardReason() {
        return discardReason;
    }

    public void setDiscardReason(String discardReason) {
        this.discardReason = discardReason;
    }

    public CycleDefinition getCycleDefinition() {
        return cycleDefinition;
    }

    public void setCycleDefinition(CycleDefinition cycleDefinition) {
        this.cycleDefinition = cycleDefinition;
    }

    public Boolean getBlockingValidation() {
        return blockingValidation;
    }

    public void setBlockingValidation(Boolean blockingValidation) {
        this.blockingValidation = blockingValidation;
    }

    public String getQualityThresholds() {
        return qualityThresholds;
    }

    public void setQualityThresholds(String qualityThresholds) {
        this.qualityThresholds = qualityThresholds;
    }

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public String getQualitySeverities() {
        return qualitySeverities;
    }

    public void setQualitySeverities(String qualitySeverities) {
        this.qualitySeverities = qualitySeverities;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Set<Run> getRuns() {
        return runs;
    }

    public Set<CountryDeployment> getCountryDeployments() {
        return countryDeployments;
    }

}
