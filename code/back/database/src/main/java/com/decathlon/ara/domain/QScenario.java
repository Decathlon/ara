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

public class QScenario extends EntityPathBase<Scenario> {

    private static final long serialVersionUID = 1148318329L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScenario scenario = new QScenario("scenario");

    public final StringPath content = createString("content");

    public final StringPath countryCodes = createString("countryCodes");

    public final StringPath featureFile = createString("featureFile");

    public final StringPath featureName = createString("featureName");

    public final StringPath featureTags = createString("featureTags");

    public final SetPath<Functionality, QFunctionality> functionalities = this.<Functionality, QFunctionality>createSet("functionalities", Functionality.class, QFunctionality.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath ignored = createBoolean("ignored");

    public final NumberPath<Integer> line = createNumber("line", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath severity = createString("severity");

    public final QSource source;

    public final StringPath tags = createString("tags");

    public final StringPath wrongCountryCodes = createString("wrongCountryCodes");

    public final StringPath wrongFunctionalityIds = createString("wrongFunctionalityIds");

    public final StringPath wrongSeverityCode = createString("wrongSeverityCode");

    public QScenario(String variable) {
        this(Scenario.class, forVariable(variable), INITS);
    }

    public QScenario(Path<? extends Scenario> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScenario(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScenario(PathMetadata metadata, PathInits inits) {
        this(Scenario.class, metadata, inits);
    }

    public QScenario(Class<? extends Scenario> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.source = inits.isInitialized("source") ? new QSource(forProperty("source")) : null;
    }

}

