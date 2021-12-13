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
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.SortNatural;

import com.decathlon.ara.domain.enumeration.CoverageLevel;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;

@Entity
@NamedEntityGraph(name = "Functionality.scenarios", attributeNodes = @NamedAttributeNode("scenarios"))
public class Functionality implements Comparable<Functionality> {

    public static final String COUNTRY_CODES_SEPARATOR = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "functionality_id")
    @SequenceGenerator(name = "functionality_id", sequenceName = "functionality_id", allocationSize = 1)
    private Long id;

    private Long projectId;

    private Long parentId;

    @Column(name = "\"order\"")
    private Double order;

    @Enumerated(EnumType.STRING)
    @Column(length = 13)
    private FunctionalityType type;

    @Column(length = 512)
    private String name;

    /**
     * The {@link Country#code codes of all countries} where this functionality is configured to be supported,
     * separated by commas.<br>
     * Eg. "fr,us"...
     *
     * @see #COUNTRY_CODES_SEPARATOR the separator used to join country-codes together
     */
    @Column(length = 128)
    private String countryCodes;

    private Long teamId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private FunctionalitySeverity severity;

    @Column(length = 10)
    private String created;

    private Boolean started;

    private Boolean notAutomatable;

    private Integer coveredScenarios;

    @Column(length = 512)
    private String coveredCountryScenarios;

    private Integer ignoredScenarios;

    @Column(length = 512)
    private String ignoredCountryScenarios;

    @Transient
    private CoverageLevel lazyLoadedCoverageLevel;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String comment;

    @Column(name = "creation_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDateTime;

    @Column(name = "update_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDateTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "functionality_coverage",
            joinColumns = @JoinColumn(name = "functionality_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "scenario_id", referencedColumnName = "id"))
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
        Comparator<Functionality> projectIdComparator = comparing(Functionality::getProjectId, nullsFirst(naturalOrder()));
        Comparator<Functionality> parentIdComparator = comparing(Functionality::getParentId, nullsFirst(naturalOrder()));
        Comparator<Functionality> nameComparator = comparing(Functionality::getName, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(parentIdComparator)
                .thenComparing(nameComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parentId, projectId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Functionality)) {
            return false;
        }
        Functionality other = (Functionality) obj;
        return Objects.equals(name, other.name) && Objects.equals(parentId, other.parentId)
                && Objects.equals(projectId, other.projectId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Double getOrder() {
        return order;
    }

    public void setOrder(Double order) {
        this.order = order;
    }

    public FunctionalityType getType() {
        return type;
    }

    public void setType(FunctionalityType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getCountryCodes() {
        return countryCodes;
    }

    public void setCountryCodes(String countryCodes) {
        this.countryCodes = countryCodes;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public FunctionalitySeverity getSeverity() {
        return severity;
    }

    public String getCreated() {
        return created;
    }

    public Integer getCoveredScenarios() {
        return coveredScenarios;
    }

    public void setCoveredScenarios(Integer coveredScenarios) {
        this.coveredScenarios = coveredScenarios;
    }

    public String getCoveredCountryScenarios() {
        return coveredCountryScenarios;
    }

    public void setCoveredCountryScenarios(String coveredCountryScenarios) {
        this.coveredCountryScenarios = coveredCountryScenarios;
    }

    public Integer getIgnoredScenarios() {
        return ignoredScenarios;
    }

    public void setIgnoredScenarios(Integer ignoredScenarios) {
        this.ignoredScenarios = ignoredScenarios;
    }

    public String getIgnoredCountryScenarios() {
        return ignoredCountryScenarios;
    }

    public void setIgnoredCountryScenarios(String ignoredCountryScenarios) {
        this.ignoredCountryScenarios = ignoredCountryScenarios;
    }

    public CoverageLevel getLazyLoadedCoverageLevel() {
        return lazyLoadedCoverageLevel;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Date creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public Set<Scenario> getScenarios() {
        return scenarios;
    }

}
