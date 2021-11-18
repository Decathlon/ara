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

import com.decathlon.ara.domain.enumeration.Handling;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Comparator.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "runId", "featureFile", "name", "line" })
@Table(indexes = @Index(columnList = "run_id"))
public class ExecutedScenario implements Comparable<ExecutedScenario>, Serializable {

    public static final int CUCUMBER_ID_MAX_SIZE = 640;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "executed_scenario_id")
    @SequenceGenerator(name = "executed_scenario_id", sequenceName = "executed_scenario_id", allocationSize = 1)
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

    @Column(length = 32)
    private String severity;

    @Column(length = 512)
    private String name;

    @Column(length = 640)
    private String cucumberId;

    private int line;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String content;

    @Column(name = "start_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;

    @Column(length = 512)
    private String screenshotUrl;

    @Column(length = 512)
    private String videoUrl;

    @Column(length = 512)
    private String logsUrl;

    @Column(length = 512)
    private String httpRequestsUrl;

    @Column(length = 512)
    private String javaScriptErrorsUrl;

    @Column(length = 512)
    private String diffReportUrl;

    @Column(length = 512)
    private String cucumberReportUrl;

    @Column(length = 16)
    private String apiServer;

    @Column(length = 128)
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
            for (ProblemOccurrence problemOccurrence : error.getProblemOccurrences()) {
                var problemPattern = problemOccurrence.getProblemPattern();
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
