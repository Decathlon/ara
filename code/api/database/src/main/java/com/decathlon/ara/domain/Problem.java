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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.EffectiveProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatus;

@Entity
public class Problem implements Comparable<Problem> {

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

    @Override
    public int hashCode() {
        return Objects.hash(name, projectId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Problem)) {
            return false;
        }
        Problem other = (Problem) obj;
        return Objects.equals(name, other.name) && projectId == other.projectId;
    }

    /**
     * @return true if the status is either open, or is closed and did not reappear after the closing date
     */
    boolean isHandled() {
        return getEffectiveStatus() != EffectiveProblemStatus.REAPPEARED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ProblemStatus getStatus() {
        return status;
    }

    public void setStatus(ProblemStatus status) {
        this.status = status;
    }

    public Team getBlamedTeam() {
        return blamedTeam;
    }

    public void setBlamedTeam(Team blamedTeam) {
        this.blamedTeam = blamedTeam;
    }

    public String getDefectId() {
        return defectId;
    }

    public void setDefectId(String defectId) {
        this.defectId = defectId;
    }

    public DefectExistence getDefectExistence() {
        return defectExistence;
    }

    public void setDefectExistence(DefectExistence defectExistence) {
        this.defectExistence = defectExistence;
    }

    public Date getClosingDateTime() {
        return closingDateTime;
    }

    public void setClosingDateTime(Date closingDateTime) {
        this.closingDateTime = closingDateTime;
    }

    public RootCause getRootCause() {
        return rootCause;
    }

    public void setRootCause(RootCause rootCause) {
        this.rootCause = rootCause;
    }

    public List<ProblemPattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<ProblemPattern> patterns) {
        this.patterns = patterns;
    }

    public Date getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Date creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Date getFirstSeenDateTime() {
        return firstSeenDateTime;
    }

    public void setFirstSeenDateTime(Date firstSeenDateTime) {
        this.firstSeenDateTime = firstSeenDateTime;
    }

    public Date getLastSeenDateTime() {
        return lastSeenDateTime;
    }

    public void setLastSeenDateTime(Date lastSeenDateTime) {
        this.lastSeenDateTime = lastSeenDateTime;
    }

}
