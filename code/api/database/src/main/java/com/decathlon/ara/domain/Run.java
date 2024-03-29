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
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;

import com.decathlon.ara.domain.enumeration.JobStatus;

@Entity
@Table(indexes = @Index(columnList = "execution_id"))
public class Run implements Comparable<Run> {

    public static final String SEVERITY_TAGS_SEPARATOR = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "run_id")
    @SequenceGenerator(name = "run_id", sequenceName = "run_id", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id")
    private Execution execution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id")
    private Type type;

    /**
     * A comment or title or description to display just above the associated {@link Run} in the GUI.
     */
    private String comment;

    /**
     * The platform/environment/server on which this run executed the test-type for the given country.
     */
    @Column(length = 32)
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

    /**
     * ARA's workflow-status of the execution (interpreted from the many statuses of the Continuous Integration job).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private JobStatus status;

    /**
     * Comma-separated {@link Country#getCode()} list of @country-* Cucumber tags to run.<br>
     * Can contain "all" to include @country-all core scenarios.<br>
     * Eg. "be,cn" for BE+CN specific scenarios, or "all,be" for core+BE scenarios.
     */
    @Column(length = 32)
    private String countryTags;

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
     * The actual duration of the remote job, in milliseconds, AFTER it has finished (may be 0 while running).
     */
    private Long duration;

    /**
     * The {@link Severity#code codes of all severity} where this run is configured to execute.<br>
     * Can be {@link Severity#ALL} or one or more codes, separated by commas.<br>
     * Eg. "all" or "sanity-check,high"...
     *
     * @see #SEVERITY_TAGS_SEPARATOR the separator used to join country-codes together
     */
    @Column(length = 64)
    private String severityTags;

    /**
     * True to include the succeed/failed scenario counts in the quality thresholds computation of the parent execution.
     */
    private Boolean includeInThresholds;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "run", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @SortNatural
    private Set<ExecutedScenario> executedScenarios = new TreeSet<>();

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public void addExecutedScenario(ExecutedScenario executedScenario) {
        // Set the child-entity's foreign-key BEFORE adding the child-entity to the TreeSet,
        // as the foreign-key is required to place the child-entity in the right order (with child-entity's compareTo)
        // and is required not to change while the child-entity is in the TreeSet
        executedScenario.setRun(this);
        executedScenarios.add(executedScenario);
    }

    public void addExecutedScenarios(Iterable<? extends ExecutedScenario> executedScenariosToAdd) {
        for (ExecutedScenario executedScenario : executedScenariosToAdd) {
            addExecutedScenario(executedScenario);
        }
    }

    @SuppressWarnings("squid:S2250") // Collection methods with O(n) performance should be used carefully
    public void removeExecutedScenario(ExecutedScenario executedScenario) {
        executedScenarios.remove(executedScenario);
        executedScenario.setRun(null);
    }

    @Override
    public int compareTo(Run other) {
        Comparator<Run> executionIdComparator = comparing(Run::getExecutionId, nullsFirst(naturalOrder()));
        Comparator<Run> countryComparator = comparing(Run::getCountry, nullsFirst(naturalOrder()));
        Comparator<Run> typeComparator = comparing(Run::getType, nullsFirst(naturalOrder()));
        return nullsFirst(executionIdComparator
                .thenComparing(countryComparator)
                .thenComparing(typeComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, getExecutionId(), type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Run)) {
            return false;
        }
        Run other = (Run) obj;
        return Objects.equals(country, other.country) && Objects.equals(getExecutionId(), other.getExecutionId())
                && Objects.equals(type, other.type);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getCountryTags() {
        return countryTags;
    }

    public void setCountryTags(String countryTags) {
        this.countryTags = countryTags;
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

    public String getSeverityTags() {
        return severityTags;
    }

    public void setSeverityTags(String severityTags) {
        this.severityTags = severityTags;
    }

    public Boolean getIncludeInThresholds() {
        return includeInThresholds;
    }

    public void setIncludeInThresholds(Boolean includeInThresholds) {
        this.includeInThresholds = includeInThresholds;
    }

    public Set<ExecutedScenario> getExecutedScenarios() {
        return executedScenarios;
    }

    public Execution getExecution() {
        return execution;
    }

}
