package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.Handling;
import com.querydsl.core.annotations.QueryInit;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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
@EqualsAndHashCode(of = { "runId", "featureFile", "name", "line" })
public class ExecutedScenario implements Comparable<ExecutedScenario> {

    public static final int CUCUMBER_ID_MAX_SIZE = 640;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    // 1/2 for @EqualsAndHashCode to work: used when an entity is fetched by JPA
    @Column(name = "run_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long runId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id")
    @QueryInit("*.*") // Requires Q* class regeneration https://github.com/querydsl/querydsl/issues/255
    private Run run;

    private String featureFile;

    private String featureName;

    private String featureTags;

    private String tags;

    private String severity;

    private String name;

    private String cucumberId;

    private int line;

    @Lob
    private String content;

    @Column(name = "start_date_time", columnDefinition = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;

    private String screenshotUrl;

    private String videoUrl;

    private String logsUrl;

    private String httpRequestsUrl;

    private String javaScriptErrorsUrl;

    private String diffReportUrl;

    private String cucumberReportUrl;

    private String apiServer;

    private String seleniumNode;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "executedScenario", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @SortNatural
    @Fetch(FetchMode.SUBSELECT)
    private Set<Error> errors = new TreeSet<>();

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setRun(Run run) {
        this.run = run;
        this.runId = (run == null ? null : run.getId());
    }

    public void addError(Error error) {
        // Set the child-entity's foreign-key BEFORE adding the child-entity to the TreeSet,
        // as the foreign-key is required to place the child-entity in the right order (with child-entity's compareTo)
        // and is required not to change while the child-entity is in the TreeSet
        error.setExecutedScenario(this);
        this.errors.add(error);
    }

    public void addErrors(Iterable<? extends Error> errorsToAdd) {
        for (Error error : errorsToAdd) {
            addError(error);
        }
    }

    public void removeError(Error error) {
        this.errors.remove(error);
        error.setExecutedScenario(null);
    }

    /**
     * @return SUCCESS if the scenario has no error, HANDLED if at least one error has at least one problem that is open
     * or did not reappear after closing date, UNHANDLED otherwise (has errors with only open or reappeared problems)
     */
    public Handling getHandling() {
        if (getErrors().isEmpty()) {
            return Handling.SUCCESS;
        }

        for (Error error : getErrors()) {
            for (ProblemPattern problemPattern : error.getProblemPatterns()) {
                if (problemPattern.getProblem().isHandled()) {
                    return Handling.HANDLED;
                }
            }
        }

        return Handling.UNHANDLED;
    }

    @Override
    public int compareTo(ExecutedScenario other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<ExecutedScenario> runIdComparator = comparing(e -> e.runId, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> featureFileComparator = comparing(ExecutedScenario::getFeatureFile, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> nameComparator = comparing(ExecutedScenario::getName, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> lineComparator = comparing(e -> Long.valueOf(e.getLine()), nullsFirst(naturalOrder()));
        return nullsFirst(runIdComparator
                .thenComparing(featureFileComparator)
                .thenComparing(nameComparator)
                .thenComparing(lineComparator)).compare(this, other);
    }

}
