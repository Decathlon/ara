package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.CoverageLevel;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortNatural;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@Entity
@NamedEntityGraph(name = "Functionality.scenarios", attributeNodes = @NamedAttributeNode("scenarios"))
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "projectId", "parentId", "name" })
public class Functionality implements Comparable<Functionality> {

    public static final String COUNTRY_CODES_SEPARATOR = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private long projectId;

    private Long parentId;

    private double order;

    @Enumerated(EnumType.STRING)
    private FunctionalityType type;

    private String name;

    /**
     * The {@link Country#code codes of all countries} where this functionality is configured to be supported,
     * separated by commas.<br>
     * Eg. "fr,us"...
     *
     * @see #COUNTRY_CODES_SEPARATOR the separator used to join country-codes together
     */
    private String countryCodes;

    private Long teamId;

    @Enumerated(EnumType.STRING)
    private FunctionalitySeverity severity;

    private String created;

    private Boolean started;

    private Boolean notAutomatable;

    private Integer coveredScenarios;

    private String coveredCountryScenarios;

    private Integer ignoredScenarios;

    private String ignoredCountryScenarios;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Transient
    private CoverageLevel lazyLoadedCoverageLevel;

    @Lob
    private String comment;

    @Column(name = "creation_date_time", columnDefinition = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDateTime;

    @Column(name = "update_date_time", columnDefinition = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDateTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "functionality_coverage",
            joinColumns = @JoinColumn(name = "functionality_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "scenario_id", referencedColumnName = "id")
    )
    @SortNatural
    private Set<Scenario> scenarios = new TreeSet<>();

    public void addScenario(Scenario scenario) {
        this.scenarios.add(scenario);
        lazyLoadedCoverageLevel = null;
    }

    public void removeScenario(Scenario scenario) {
        this.scenarios.remove(scenario);
        lazyLoadedCoverageLevel = null;
    }

    private boolean hasScenarios() {
        return !getScenarios().isEmpty();
    }

    public CoverageLevel getCoverageLevel() {
        if (lazyLoadedCoverageLevel == null) {
            if (hasScenarios()) {
                if (getScenarios().stream().noneMatch(Scenario::isIgnored)) {
                    lazyLoadedCoverageLevel = CoverageLevel.COVERED;
                } else if (getScenarios().stream().anyMatch(s -> !s.isIgnored())) {
                    lazyLoadedCoverageLevel = CoverageLevel.PARTIALLY_COVERED;
                } else {
                    lazyLoadedCoverageLevel = CoverageLevel.IGNORED_COVERAGE;
                }
            } else if (isStarted()) {
                lazyLoadedCoverageLevel = CoverageLevel.STARTED;
            } else if (isNotAutomatable()) {
                lazyLoadedCoverageLevel = CoverageLevel.NOT_AUTOMATABLE;
            } else {
                lazyLoadedCoverageLevel = CoverageLevel.NOT_COVERED;
            }
        }
        return lazyLoadedCoverageLevel;
    }

    public Boolean getStarted() {
        // We redefine setStarted, so SonarQube thinks started is never used outside this class if we do not redefine getStarted
        return started;
    }

    private boolean isStarted() {
        return getStarted() != null && getStarted().booleanValue();
    }

    public void setStarted(Boolean started) {
        this.started = started;
        lazyLoadedCoverageLevel = null;
    }

    public Boolean getNotAutomatable() {
        // We redefine setNotAutomatable, so SonarQube thinks notAutomatable is never used outside this class if we do not redefine getNotAutomatable
        return notAutomatable;
    }

    private boolean isNotAutomatable() {
        return getNotAutomatable() != null && getNotAutomatable().booleanValue();
    }

    public void setNotAutomatable(Boolean notAutomatable) {
        this.notAutomatable = notAutomatable;
        lazyLoadedCoverageLevel = null;
    }

    @Override
    public int compareTo(Functionality other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Functionality> projectIdComparator = comparing(f -> Long.valueOf(f.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Functionality> parentIdComparator = comparing(Functionality::getParentId, nullsFirst(naturalOrder()));
        Comparator<Functionality> nameComparator = comparing(Functionality::getName, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(parentIdComparator)
                .thenComparing(nameComparator)).compare(this, other);
    }

}
