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

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SFunctionalityCoverage extends com.querydsl.sql.RelationalPathBase<SFunctionalityCoverage> {

    private static final long serialVersionUID = -1219722423;

    public static final SFunctionalityCoverage functionalityCoverage = new SFunctionalityCoverage("functionality_coverage");

    public final NumberPath<Long> functionalityId = createNumber("functionalityId", Long.class);

    public final NumberPath<Long> scenarioId = createNumber("scenarioId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SFunctionalityCoverage> primary = createPrimaryKey(functionalityId, scenarioId);

    public final com.querydsl.sql.ForeignKey<SScenario> functionalitycoverageScenarioidFk = createForeignKey(scenarioId, "id");

    public final com.querydsl.sql.ForeignKey<SFunctionality> functionalitycoverageFunctionalityidFk = createForeignKey(functionalityId, "id");

    public SFunctionalityCoverage(String variable) {
        super(SFunctionalityCoverage.class, forVariable(variable), "null", "functionality_coverage");
        addMetadata();
    }

    public SFunctionalityCoverage(String variable, String schema, String table) {
        super(SFunctionalityCoverage.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SFunctionalityCoverage(String variable, String schema) {
        super(SFunctionalityCoverage.class, forVariable(variable), schema, "functionality_coverage");
        addMetadata();
    }

    public SFunctionalityCoverage(Path<? extends SFunctionalityCoverage> path) {
        super(path.getType(), path.getMetadata(), "null", "functionality_coverage");
        addMetadata();
    }

    public SFunctionalityCoverage(PathMetadata metadata) {
        super(SFunctionalityCoverage.class, metadata, "null", "functionality_coverage");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(functionalityId, ColumnMetadata.named("functionality_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(scenarioId, ColumnMetadata.named("scenario_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

