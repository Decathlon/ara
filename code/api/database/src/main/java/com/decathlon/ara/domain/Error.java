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
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(indexes = @Index(columnList = "executed_scenario_id"))
public class Error implements Comparable<Error> {

    public static final String PROBLEM_OCCURRENCES_COLLECTION_CACHE = "com.decathlon.ara.domain.Error.problemOccurrences";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_id")
    @SequenceGenerator(name = "error_id", sequenceName = "error_id", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "executed_scenario_id")
    private ExecutedScenario executedScenario;

    @Column(length = 2048)
    private String step;

    @Column(length = 2048)
    private String stepDefinition;

    private int stepLine;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String exception;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "error", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ProblemOccurrence> problemOccurrences = new HashSet<>();

    public void setExecutedScenario(ExecutedScenario executedScenario) {
        this.executedScenario = executedScenario;
    }

    @Override
    public int compareTo(Error other) {
        Comparator<Error> executedScenarioIdComparator = comparing(Error::getExecutedScenarioId, nullsFirst(naturalOrder()));
        Comparator<Error> stepLineComparator = comparing(e -> Long.valueOf(e.getStepLine()), nullsFirst(naturalOrder()));
        return nullsFirst(executedScenarioIdComparator
                .thenComparing(stepLineComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExecutedScenarioId(), stepLine);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Error)) {
            return false;
        }
        Error other = (Error) obj;
        return Objects.equals(getExecutedScenarioId(), other.getExecutedScenarioId()) && stepLine == other.stepLine;
    }

    public Long getId() {
        return id;
    }

    public Long getExecutedScenarioId() {
        return executedScenario == null ? null : executedScenario.getId();
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getStepDefinition() {
        return stepDefinition;
    }

    public void setStepDefinition(String stepDefinition) {
        this.stepDefinition = stepDefinition;
    }

    public int getStepLine() {
        return stepLine;
    }

    public void setStepLine(int stepLine) {
        this.stepLine = stepLine;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Set<ProblemOccurrence> getProblemOccurrences() {
        return problemOccurrences;
    }

    public ExecutedScenario getExecutedScenario() {
        return executedScenario;
    }

}
