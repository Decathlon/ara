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

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;

import org.apache.commons.lang3.StringUtils;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.util.ArrayList;
import java.util.List;

public class QError extends EntityPathBase<Error> {

    private static final String LIKE_MARK = "%";

    private static final long serialVersionUID = -1215962177L;

    private static final PathInits INITS = new PathInits("*", "executedScenario.run.*.*");

    public static final QError error = new QError("error");

    public final StringPath exception = createString("exception");

    public final QExecutedScenario executedScenario;

    public final NumberPath<Long> executedScenarioId = createNumber("executedScenarioId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final SetPath<ProblemOccurrence, QProblemOccurrence> problemOccurrences = this.createSet("problemOccurrences", ProblemOccurrence.class, QProblemOccurrence.class, PathInits.DIRECT2);

    public final StringPath step = createString("step");

    public final StringPath stepDefinition = createString("stepDefinition");

    public final NumberPath<Integer> stepLine = createNumber("stepLine", Integer.class);

    public QError(String variable) {
        this(Error.class, forVariable(variable), INITS);
    }

    public QError(Path<? extends Error> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QError(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QError(PathMetadata metadata, PathInits inits) {
        this(Error.class, metadata, inits);
    }

    public QError(Class<? extends Error> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.executedScenario = inits.isInitialized("executedScenario") ? new QExecutedScenario(forProperty("executedScenario"), inits.get("executedScenario")) : null;
    }

    public Predicate toFilterPredicate(long projectId, ProblemPattern pattern) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(error.executedScenario.run.execution.cycleDefinition.projectId.eq(Long.valueOf(projectId)));

        appendPredicateFeatureFile(pattern, predicates);
        appendPredicateFeatureName(pattern, predicates);
        appendPredicateScenarioName(pattern, predicates);
        appendPredicateStep(pattern, predicates);
        appendPredicateStepDefinition(pattern, predicates);
        appendPredicateException(pattern, predicates);
        appendPredicateRelease(pattern, predicates);
        appendPredicateCountry(pattern, predicates);
        appendPredicatePlatform(pattern, predicates);
        appendPredicateType(pattern, predicates);

        return ExpressionUtils.allOf(predicates);
    }

    private void appendPredicateFeatureFile(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getFeatureFile())) {
            predicates.add(executedScenario.featureFile.eq(pattern.getFeatureFile()));
        }
    }

    private void appendPredicateFeatureName(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getFeatureName())) {
            predicates.add(executedScenario.featureName.eq(pattern.getFeatureName()));
        }
    }

    public void appendPredicateScenarioName(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getScenarioName())) {
            if (pattern.isScenarioNameStartsWith()) {
                predicates.add(executedScenario.name.like(pattern.getScenarioName() + LIKE_MARK));
            } else {
                predicates.add(executedScenario.name.eq(pattern.getScenarioName()));
            }
        }
    }

    public void appendPredicateStep(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getStep())) {
            if (pattern.isStepStartsWith()) {
                predicates.add(step.like(pattern.getStep() + LIKE_MARK));
            } else {
                predicates.add(step.eq(pattern.getStep()));
            }
        }
    }

    public void appendPredicateStepDefinition(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getStepDefinition())) {
            if (pattern.isStepDefinitionStartsWith()) {
                predicates.add(stepDefinition.like(pattern.getStepDefinition() + LIKE_MARK));
            } else {
                predicates.add(stepDefinition.eq(pattern.getStepDefinition()));
            }
        }
    }

    private void appendPredicateException(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getException())) {
            predicates.add(exception.like(pattern.getException() + LIKE_MARK));
        }
    }

    private void appendPredicateRelease(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getRelease())) {
            predicates.add(executedScenario.run.execution.release.eq(pattern.getRelease()));
        }
    }

    private void appendPredicateCountry(ProblemPattern pattern, List<Predicate> predicates) {
        if (pattern.getCountry() != null && StringUtils.isNotEmpty(pattern.getCountry().getCode())) {
            predicates.add(executedScenario.run.country.code.eq(pattern.getCountry().getCode()));
        }
    }

    private void appendPredicatePlatform(ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getPlatform())) {
            predicates.add(executedScenario.run.platform.eq(pattern.getPlatform()));
        }
    }

    private void appendPredicateType(ProblemPattern pattern, List<Predicate> predicates) {
        if (pattern.getType() != null && StringUtils.isNotEmpty(pattern.getType().getCode())) {
            predicates.add(executedScenario.run.type.code.eq(pattern.getType().getCode()));
        }
        if (pattern.getTypeIsBrowser() != null) {
            predicates.add(executedScenario.run.type.isBrowser.eq(pattern.getTypeIsBrowser()));
        }
        if (pattern.getTypeIsMobile() != null) {
            predicates.add(executedScenario.run.type.isMobile.eq(pattern.getTypeIsMobile()));
        }
    }

}

