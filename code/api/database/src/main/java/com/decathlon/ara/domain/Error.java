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

import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import lombok.With;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static java.util.Comparator.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "executedScenarioId", "stepLine" })
@Table(indexes = @Index(columnList = "executed_scenario_id"))
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

    @Column(length = 2048)
    private String step;

    @Column(length = 2048)
    private String stepDefinition;

    private int stepLine;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
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
