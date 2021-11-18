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


import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.EffectiveProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.util.Comparator.*;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "projectId", "name" })
public class Problem implements Comparable<Problem>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "problem_id")
    @SequenceGenerator(name = "problem_id", sequenceName = "problem_id", allocationSize = 1)
    private Long id;

    private long projectId;

    private String name;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String comment;

    /**
     * The status saved in database, and managed by users (they can open or close a problem.
     *
     * @see #getEffectiveStatus() getEffectiveStatus() for the displayed status: REAPPEARED can hide the CLOSED status
     * if the problem reappeared after its closing date, until the problem is reopened
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 21)
    private ProblemStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blamed_team_id")
    private Team blamedTeam;

    @Column(length = 32)
    private String defectId;

    @Enumerated(EnumType.STRING)
    @Column(length = 11)
    private DefectExistence defectExistence;

    @Column(name = "closing_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date closingDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_cause_id")
    private RootCause rootCause;

    // Do not set orphanRemoval, because we want to be able to move patterns from one problem to another
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "problem")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ProblemPattern> patterns = new ArrayList<>();

    @Column(name = "creation_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDateTime;

    /**
     * This is a de-normalized field (to avoid a seven-tables join all over ARA source code)
     * holding the testDateTime of the first error occurrence for this problem,
     * or null if the problem never appeared.
     */
    @Column(name = "first_seen_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstSeenDateTime;

    /**
     * This is a de-normalized field (to avoid a seven-tables join all over ARA source code)
     * holding the testDateTime of the last error occurrence for this problem,
     * or null if the problem never appeared.
     */
    @Column(name = "last_seen_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSeenDateTime;

    /**
     * @return the status effectively displayed for the problem (REAPPEARED effectiveStatus can hide the CLOSED status
     * if the problem reappeared after its closing date)
     * @see #status status to know the real status saved in database
     */
    @Transient
    public EffectiveProblemStatus getEffectiveStatus() {
        if (status == ProblemStatus.CLOSED) {
            // This business logic is also present in another form in ProblemRepositoryImpl.computeStatusPredicate()
            if (getClosingDateTime() != null && getLastSeenDateTime() != null &&
                    getClosingDateTime().before(getLastSeenDateTime())) {
                return EffectiveProblemStatus.REAPPEARED;
            }
            return EffectiveProblemStatus.CLOSED;
        }
        return EffectiveProblemStatus.OPEN;
    }

    public void addPattern(ProblemPattern pattern) {
        pattern.setProblem(this);
        patterns.add(pattern);
    }

    @SuppressWarnings("squid:S2250") // Collection methods with O(n) performance should be used carefully
    public void removePattern(ProblemPattern pattern) {
        patterns.remove(pattern);
        pattern.setProblem(null);
    }

    @Override
    public int compareTo(Problem other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Problem> projectIdComparator = comparing(p -> Long.valueOf(p.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Problem> nameComparator = comparing(Problem::getName, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(nameComparator)).compare(this, other);
    }

    /**
     * @return true if the status is either open, or is closed and did not reappear after the closing date
     */
    boolean isHandled() {
        return getEffectiveStatus() != EffectiveProblemStatus.REAPPEARED;
    }

}
