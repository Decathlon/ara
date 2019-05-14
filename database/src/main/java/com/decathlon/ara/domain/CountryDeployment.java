package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.Result;
import java.util.Comparator;
import java.util.Date;
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

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "executionId", "country" })
public class CountryDeployment implements Comparable<CountryDeployment> {

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

    /**
     * The platform/environment/server on which this country was deployed.
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
    @Column(name = "start_date_time", columnDefinition = "TIMESTAMP")
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

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setExecution(Execution execution) {
        this.execution = execution;
        this.executionId = (execution == null ? null : execution.getId());
    }

    @Override
    public int compareTo(CountryDeployment other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<CountryDeployment> executionIdComparator = comparing(d -> d.executionId, nullsFirst(naturalOrder()));
        Comparator<CountryDeployment> countryComparator = comparing(CountryDeployment::getCountry, nullsFirst(naturalOrder()));
        return nullsFirst(executionIdComparator
                .thenComparing(countryComparator)).compare(this, other);
    }

}
