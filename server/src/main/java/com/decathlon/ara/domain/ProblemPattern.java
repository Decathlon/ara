package com.decathlon.ara.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
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
import org.hibernate.annotations.GenericGenerator;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Wither
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "problemId", "featureFile", "featureName", "scenarioName", "scenarioNameStartsWith", "step",
        "stepStartsWith", "stepDefinition", "stepDefinitionStartsWith", "exception", "release", "country", "type",
        "typeIsBrowser", "typeIsMobile", "platform" })
public class ProblemPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    // 1/2 for @EqualsAndHashCode to work: used when an entity is fetched by JPA
    @Column(name = "problem_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long problemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    /**
     * Matches {@link ExecutedScenario#featureFile}.
     */
    private String featureFile;

    /**
     * Matches {@link ExecutedScenario#featureName}.
     */
    private String featureName;

    /**
     * Matches {@link ExecutedScenario#name}
     * (exactly matching this text; or match the beginning of this text, optionally containing '%' for LIKE-matching,
     * if {@link #scenarioNameStartsWith} is true).
     */
    private String scenarioName;

    /**
     * If false, {@link #scenarioName} will be matched exactly to {@link ExecutedScenario#name}.<br>
     * If true, {@link #scenarioName} will be matched to the beginning of {@link ExecutedScenario#name},
     * and '%' will be used for LIKE-matching.
     */
    private boolean scenarioNameStartsWith;

    /**
     * Matches {@link Error#step}
     * (exactly matching this text; or match the beginning of this text, optionally containing '%' for LIKE-matching,
     * if {@link #stepStartsWith} is true).
     */
    private String step;

    /**
     * If false, {@link #step} will be matched exactly to {@link Error#step}.<br>
     * If true, {@link #step} will be matched to the beginning of {@link Error#step},
     * and '%' will be used for LIKE-matching.
     */
    private boolean stepStartsWith;

    /**
     * Matches {@link Error#stepDefinition}
     * (exactly matching this text; or match the beginning of this text, optionally containing '%' for LIKE-matching,
     * if {@link #stepStartsWith} is true).
     */
    private String stepDefinition;

    /**
     * If false, {@link #stepDefinition} will be matched exactly to {@link Error#stepDefinition}.<br>
     * If true, {@link #stepDefinition} will be matched to the beginning of {@link Error#stepDefinition},
     * and '%' will be used for LIKE-matching.
     */
    private boolean stepDefinitionStartsWith;

    /**
     * Matches the beginning of {@link Error#exception}, optionally containing '%' for LIKE-matching.
     */
    @Lob
    private String exception;

    /**
     * Matches {@link Execution#release}.
     */
    private String release;

    /**
     * Matches {@link Run#country}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    /**
     * Matches {@link Run#type}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private Type type;

    /**
     * Matches {@link Run#type}.{@link Type#isBrowser isBrowser}.
     */
    private Boolean typeIsBrowser;

    /**
     * Matches {@link Run#type}.{@link Type#isMobile isMobile}.
     */
    private Boolean typeIsMobile;

    /**
     * Matches {@link Run#platform}.
     */
    private String platform;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "problem_occurrence",
            joinColumns = @JoinColumn(name = "problem_pattern_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "error_id", referencedColumnName = "id")
    )
    private Set<Error> errors = new HashSet<>();

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setProblem(Problem problem) {
        this.problem = problem;
        this.problemId = (problem == null ? null : problem.getId());
    }

    public void addError(Error error) {
        this.errors.add(error);
        error.getProblemPatterns().add(this);
    }

    public void removeError(Error error) {
        this.errors.remove(error);
        error.getProblemPatterns().remove(this);
    }

}
