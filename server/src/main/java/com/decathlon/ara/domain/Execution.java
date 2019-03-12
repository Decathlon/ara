package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.ci.bean.Result;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "cycleDefinition", "testDateTime" })
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private String branch;

    private String name;

    /**
     * The release of the version having been tested by this execution
     * (eg. "v2" or "1904" for the release encompassing all versions of April 2019).<br>
     * Optional: will not be filterable by problems if not provided.
     */
    private String release;

    /**
     * The version having been tested by this execution
     * (eg. "1904.3" for the third version of the April 2019 release, a timestamped version, or the Git commit ID).<br>
     * Optional: for display only.
     */
    private String version;

    /**
     * The date & time at which the version having been tested by this execution was created (compiled...).<br>
     * Optional: for display only.
     */
    @Column(name = "build_date_time", columnDefinition = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date buildDateTime;

    /**
     * The date & time at which the execution was run on continuous Continuous Integration.<br>
     * Mandatory.
     */
    @Column(name = "test_date_time", columnDefinition = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date testDateTime;

    /**
     * The URL of the Continuous Integration job, visible in the client GUI to access logs of the job.
     */
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
    private JobStatus status;

    /**
     * The raw status of the Continuous Integration job for this execution.
     */
    @Enumerated(EnumType.STRING)
    private Result result;

    /**
     * An execution is accepted, discarded (or not yet accepted nor discarded).
     */
    @Enumerated(EnumType.STRING)
    private ExecutionAcceptance acceptance;

    /**
     * If the {@link #acceptance} is {@link ExecutionAcceptance#DISCARDED DISCARDED}, the mandatory reason explains why
     * a user discarded the execution (too many network errors, etc.).
     */
    private String discardReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_definition_id")
    private CycleDefinition cycleDefinition;

    private Boolean blockingValidation;

    private String qualityThresholds;

    @Enumerated(EnumType.STRING)
    private QualityStatus qualityStatus;

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

}
