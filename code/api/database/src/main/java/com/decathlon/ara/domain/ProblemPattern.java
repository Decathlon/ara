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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class ProblemPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "problem_pattern_id")
    @SequenceGenerator(name = "problem_pattern_id", sequenceName = "problem_pattern_id", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
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
    @Column(length = 512)
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
    @Column(length = 2048)
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
    @Column(length = 2048)
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
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String exception;

    /**
     * Matches {@link Execution#release}.
     */
    @Column(length = 32)
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
    @Column(length = 32)
    private String platform;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "problemPattern", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ProblemOccurrence> problemOccurrences = new HashSet<>();

    public ProblemPattern() {
        this.id = 0l;
    }

    public boolean equals(ProblemPattern other, Long problemId) {
        return Objects.equals(country, other.country) && Objects.equals(exception, other.exception)
                && Objects.equals(featureFile, other.featureFile) && Objects.equals(featureName, other.featureName)
                && Objects.equals(platform, other.platform) && Objects.equals(problemId, other.getProblemId())
                && Objects.equals(release, other.release) && Objects.equals(scenarioName, other.scenarioName)
                && scenarioNameStartsWith == other.scenarioNameStartsWith && Objects.equals(step, other.step)
                && Objects.equals(stepDefinition, other.stepDefinition)
                && stepDefinitionStartsWith == other.stepDefinitionStartsWith && Objects.equals(type, other.type)
                && Objects.equals(typeIsBrowser, other.typeIsBrowser)
                && Objects.equals(typeIsMobile, other.typeIsMobile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, exception, featureFile, featureName, platform, getProblemId(), release, scenarioName,
                scenarioNameStartsWith, step, stepDefinition, stepDefinitionStartsWith, type, typeIsBrowser,
                typeIsMobile);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProblemPattern)) {
            return false;
        }
        return equals((ProblemPattern) obj, this.getProblemId());
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return (problem == null ? null : problem.getId());
    }

    public Problem getProblem() {
        return problem;
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public boolean isScenarioNameStartsWith() {
        return scenarioNameStartsWith;
    }

    public String getStep() {
        return step;
    }

    public boolean isStepStartsWith() {
        return stepStartsWith;
    }

    public String getStepDefinition() {
        return stepDefinition;
    }

    public boolean isStepDefinitionStartsWith() {
        return stepDefinitionStartsWith;
    }

    public String getException() {
        return exception;
    }

    public String getRelease() {
        return release;
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

    public Boolean getTypeIsBrowser() {
        return typeIsBrowser;
    }

    public Boolean getTypeIsMobile() {
        return typeIsMobile;
    }

    public String getPlatform() {
        return platform;
    }

    public Set<ProblemOccurrence> getProblemOccurrences() {
        return problemOccurrences;
    }

}
