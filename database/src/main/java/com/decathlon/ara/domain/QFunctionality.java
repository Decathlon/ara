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

public class QFunctionality extends EntityPathBase<Functionality> {

    private static final long serialVersionUID = 1670133378L;

    public static final QFunctionality functionality = new QFunctionality("functionality");

    public final StringPath comment = createString("comment");

    public final StringPath countryCodes = createString("countryCodes");

    public final StringPath coveredCountryScenarios = createString("coveredCountryScenarios");

    public final NumberPath<Integer> coveredScenarios = createNumber("coveredScenarios", Integer.class);

    public final StringPath created = createString("created");

    public final DateTimePath<java.util.Date> creationDateTime = createDateTime("creationDateTime", java.util.Date.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ignoredCountryScenarios = createString("ignoredCountryScenarios");

    public final NumberPath<Integer> ignoredScenarios = createNumber("ignoredScenarios", Integer.class);

    public final StringPath name = createString("name");

    public final BooleanPath notAutomatable = createBoolean("notAutomatable");

    public final NumberPath<Double> order = createNumber("order", Double.class);

    public final NumberPath<Long> parentId = createNumber("parentId", Long.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final SetPath<Scenario, QScenario> scenarios = this.<Scenario, QScenario>createSet("scenarios", Scenario.class, QScenario.class, PathInits.DIRECT2);

    public final EnumPath<com.decathlon.ara.domain.enumeration.FunctionalitySeverity> severity = createEnum("severity", com.decathlon.ara.domain.enumeration.FunctionalitySeverity.class);

    public final BooleanPath started = createBoolean("started");

    public final NumberPath<Long> teamId = createNumber("teamId", Long.class);

    public final EnumPath<com.decathlon.ara.domain.enumeration.FunctionalityType> type = createEnum("type", com.decathlon.ara.domain.enumeration.FunctionalityType.class);

    public final DateTimePath<java.util.Date> updateDateTime = createDateTime("updateDateTime", java.util.Date.class);

    public QFunctionality(String variable) {
        super(Functionality.class, forVariable(variable));
    }

    public QFunctionality(Path<? extends Functionality> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFunctionality(PathMetadata metadata) {
        super(Functionality.class, metadata);
    }

}

