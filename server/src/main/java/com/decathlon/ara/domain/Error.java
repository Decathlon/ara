package com.decathlon.ara.domain;

import com.querydsl.core.annotations.QueryInit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
@EqualsAndHashCode(of = { "executedScenarioId", "stepLine" })
public class Error implements Comparable<Error> {

    public static final String PROBLEM_PATTERNS_COLLECTION_CACHE = "com.decathlon.ara.domain.Error.problemPatterns";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    // 1/2 for @EqualsAndHashCode to work: used when an entity is fetched by JPA
    @Column(name = "executed_scenario_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long executedScenarioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "executed_scenario_id")
    @QueryInit("run.*.*") // Requires Q* class regeneration https://github.com/querydsl/querydsl/issues/255
    private ExecutedScenario executedScenario;

    private String step;

    private String stepDefinition;

    private int stepLine;

    @Lob
    private String exception;

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = PROBLEM_PATTERNS_COLLECTION_CACHE)
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "errors")
    private Set<ProblemPattern> problemPatterns = new HashSet<>();

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setExecutedScenario(ExecutedScenario executedScenario) {
        this.executedScenario = executedScenario;
        this.executedScenarioId = (executedScenario == null ? null : executedScenario.getId());
    }

    public void addProblemPattern(ProblemPattern problemPattern) {
        this.problemPatterns.add(problemPattern);
        problemPattern.getErrors().add(this);
    }

    public void removeProblemPattern(ProblemPattern problemPattern) {
        this.problemPatterns.remove(problemPattern);
        problemPattern.getErrors().remove(this);
    }

    @Override
    public int compareTo(Error other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Error> executedScenarioIdComparator = comparing(e -> e.executedScenarioId, nullsFirst(naturalOrder()));
        Comparator<Error> stepLineComparator = comparing(e -> Long.valueOf(e.getStepLine()), nullsFirst(naturalOrder()));
        return nullsFirst(executedScenarioIdComparator
                .thenComparing(stepLineComparator)).compare(this, other);
    }

}
