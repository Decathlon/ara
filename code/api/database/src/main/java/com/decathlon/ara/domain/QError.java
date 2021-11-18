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

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QError extends EntityPathBase<Error> {

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

}

