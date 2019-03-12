package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.JobStatus;
import java.util.Comparator;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "executionId", "country", "type" })
public class Run implements Comparable<Run> {

    public static final String SEVERITY_TAGS_SEPARATOR = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    // 1/2 for @EqualsAndHashCode to work: used when an entity is fetched by JPA
    @Column(name = "execution_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long executionId;

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
    private String platform;

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
     * Comma-separated {@link Country#getCode()} list of @country-* Cucumber tags to run.<br>
     * Can contain "all" to include @country-all core scenarios.<br>
     * Eg. "be,cn" for BE+CN specific scenarios, or "all,be" for core+BE scenarios.
     */
    private String countryTags;

    /**
     * The date and time the remote job started. Null if not started yet.
     */
    @Column(name = "start_date_time", columnDefinition = "TIMESTAMP")
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
    private String severityTags;

    /**
     * True to include the succeed/failed scenario counts in the quality thresholds computation of the parent execution.
     */
    private Boolean includeInThresholds;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "run", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @SortNatural
    private Set<ExecutedScenario> executedScenarios = new TreeSet<>();

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setExecution(Execution execution) {
        this.execution = execution;
        this.executionId = (execution == null ? null : execution.getId());
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
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Run> executionIdComparator = comparing(r -> r.executionId, nullsFirst(naturalOrder()));
        Comparator<Run> countryComparator = comparing(Run::getCountry, nullsFirst(naturalOrder()));
        Comparator<Run> typeComparator = comparing(Run::getType, nullsFirst(naturalOrder()));
        return nullsFirst(executionIdComparator
                .thenComparing(countryComparator)
                .thenComparing(typeComparator)).compare(this, other);
    }

}
