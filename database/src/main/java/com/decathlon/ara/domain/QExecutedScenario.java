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

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QExecutedScenario extends EntityPathBase<ExecutedScenario> {

    private static final long serialVersionUID = 995120680L;

    private static final PathInits INITS = new PathInits("*", "run.*.*");

    public static final QExecutedScenario executedScenario = new QExecutedScenario("executedScenario");

    public final StringPath apiServer = createString("apiServer");

    public final StringPath content = createString("content");

    public final StringPath cucumberId = createString("cucumberId");

    public final StringPath cucumberReportUrl = createString("cucumberReportUrl");

    public final StringPath diffReportUrl = createString("diffReportUrl");

    public final SetPath<Error, QError> errors = this.<Error, QError>createSet("errors", Error.class, QError.class, PathInits.DIRECT2);

    public final StringPath featureFile = createString("featureFile");

    public final StringPath featureName = createString("featureName");

    public final StringPath featureTags = createString("featureTags");

    public final StringPath httpRequestsUrl = createString("httpRequestsUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath javaScriptErrorsUrl = createString("javaScriptErrorsUrl");

    public final NumberPath<Integer> line = createNumber("line", Integer.class);

    public final StringPath logsUrl = createString("logsUrl");

    public final StringPath name = createString("name");

    public final QRun run;

    public final NumberPath<Long> runId = createNumber("runId", Long.class);

    public final StringPath screenshotUrl = createString("screenshotUrl");

    public final StringPath seleniumNode = createString("seleniumNode");

    public final StringPath severity = createString("severity");

    public final DateTimePath<java.util.Date> startDateTime = createDateTime("startDateTime", java.util.Date.class);

    public final StringPath tags = createString("tags");

    public final StringPath videoUrl = createString("videoUrl");

    public QExecutedScenario(String variable) {
        this(ExecutedScenario.class, forVariable(variable), INITS);
    }

    public QExecutedScenario(Path<? extends ExecutedScenario> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExecutedScenario(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExecutedScenario(PathMetadata metadata, PathInits inits) {
        this(ExecutedScenario.class, metadata, inits);
    }

    public QExecutedScenario(Class<? extends ExecutedScenario> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.run = inits.isInitialized("run") ? new QRun(forProperty("run"), inits.get("run")) : null;
    }

}

