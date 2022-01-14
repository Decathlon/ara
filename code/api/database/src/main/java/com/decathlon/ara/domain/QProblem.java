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

import static com.querydsl.core.types.PathMetadataFactory.*;

import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Predicate;

import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.domain.filter.ProblemFilter;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

import org.apache.commons.lang3.StringUtils;
public class QProblem extends EntityPathBase<Problem> {
    
    private static final long serialVersionUID = 860897942L;
    
    private static final PathInits INITS = PathInits.DIRECT2;
    
    public static final QProblem problem = new QProblem("problem");

    public final QTeam blamedTeam;

    public final DateTimePath<java.util.Date> closingDateTime = createDateTime("closingDateTime", java.util.Date.class);

    public final StringPath comment = createString("comment");

    public final DateTimePath<java.util.Date> creationDateTime = createDateTime("creationDateTime", java.util.Date.class);

    public final EnumPath<com.decathlon.ara.domain.enumeration.DefectExistence> defectExistence = createEnum("defectExistence", com.decathlon.ara.domain.enumeration.DefectExistence.class);

    public final StringPath defectId = createString("defectId");

    public final DateTimePath<java.util.Date> firstSeenDateTime = createDateTime("firstSeenDateTime", java.util.Date.class);

    public final BooleanPath handled = createBoolean("handled");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.util.Date> lastSeenDateTime = createDateTime("lastSeenDateTime", java.util.Date.class);

    public final StringPath name = createString("name");

    public final ListPath<ProblemPattern, QProblemPattern> patterns = this.<ProblemPattern, QProblemPattern>createList("patterns", ProblemPattern.class, QProblemPattern.class, PathInits.DIRECT2);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final QRootCause rootCause;

    public final EnumPath<com.decathlon.ara.domain.enumeration.ProblemStatus> status = createEnum("status", com.decathlon.ara.domain.enumeration.ProblemStatus.class);

    public QProblem(String variable) {
        this(Problem.class, forVariable(variable), INITS);
    }

    public QProblem(Path<? extends Problem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProblem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProblem(PathMetadata metadata, PathInits inits) {
        this(Problem.class, metadata, inits);
    }

    public QProblem(Class<? extends Problem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.blamedTeam = inits.isInitialized("blamedTeam") ? new QTeam(forProperty("blamedTeam")) : null;
        this.rootCause = inits.isInitialized("rootCause") ? new QRootCause(forProperty("rootCause")) : null;
    }

    public Predicate toFilterPredicate(ProblemFilter filter) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(projectId.eq(Long.valueOf(filter.getProjectId())));

        if (StringUtils.isNotEmpty(filter.getName())) {
            predicates.add(name.likeIgnoreCase("%" + filter.getName() + "%"));
        }

        if (filter.getStatus() != null) {
            predicates.add(computeStatusPredicate(filter.getStatus()));
        }

        if (filter.getBlamedTeamId() != null) {
            predicates.add(blamedTeam.id.eq(filter.getBlamedTeamId()));
        }

        if (StringUtils.isNotEmpty(filter.getDefectId())) {
            if ("none".equalsIgnoreCase(filter.getDefectId())) {
                predicates.add(defectId.isNull().or(defectId.isEmpty()));
            } else {
                predicates.add(defectId.likeIgnoreCase("%" + filter.getDefectId() + "%"));
            }
        }

        if (filter.getDefectExistence() != null) {
            predicates.add(defectExistence.eq(filter.getDefectExistence()));
        }

        if (filter.getRootCauseId() != null) {
            predicates.add(rootCause.id.eq(filter.getRootCauseId()));
        }

        return ExpressionUtils.allOf(predicates);
    }

    /**
     * Compute a predicate to filter problems by their status, depending the filtering choice picked from the list of
     * filtering options. Filter is performed through the {@link Problem#getEffectiveStatus()} and some filter options
     * are a combination of several statuses.
     *
     * @param statusFilter the choosen filter for problem statuses
     * @return a predicate to use in a QueryDsl request
     */
    private Predicate computeStatusPredicate(ProblemStatusFilter statusFilter) {
        final BooleanExpression open = status.eq(ProblemStatus.OPEN);
        final BooleanExpression closed = status.eq(ProblemStatus.CLOSED);

        // This business logic is also present in another form in Problem.getEffectiveStatus()
        final BooleanExpression reappeared = problem.status.eq(ProblemStatus.CLOSED)
                .and(closingDateTime.isNotNull())
                .and(lastSeenDateTime.isNotNull())
                .and(closingDateTime.before(lastSeenDateTime));

        switch (statusFilter) {
            case OPEN:
                return open;
            case CLOSED:
                return closed.and(reappeared.not());
            case REAPPEARED:
                return reappeared;
            case OPEN_OR_REAPPEARED:
                return open.or(reappeared);
            default :
                return closed; // CLOSED status includes the REAPPEARED effectiveStatus
        }
    }

}

