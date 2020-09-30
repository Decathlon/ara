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

public class SExecution extends com.querydsl.sql.RelationalPathBase<SExecution> {

    private static final long serialVersionUID = 1827759246;

    public static final SExecution execution = new SExecution("execution");

    public final StringPath acceptance = createString("acceptance");

    public final BooleanPath blockingValidation = createBoolean("blockingValidation");

    public final StringPath branch = createString("branch");

    public final DateTimePath<java.sql.Timestamp> buildDateTime = createDateTime("buildDateTime", java.sql.Timestamp.class);

    public final NumberPath<Long> cycleDefinitionId = createNumber("cycleDefinitionId", Long.class);

    public final StringPath discardReason = createString("discardReason");

    public final NumberPath<Long> duration = createNumber("duration", Long.class);

    public final NumberPath<Long> estimatedDuration = createNumber("estimatedDuration", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobLink = createString("jobLink");

    public final StringPath jobUrl = createString("jobUrl");

    public final StringPath name = createString("name");

    public final StringPath qualitySeverities = createString("qualitySeverities");

    public final StringPath qualityStatus = createString("qualityStatus");

    public final StringPath qualityThresholds = createString("qualityThresholds");

    public final StringPath release = createString("release");

    public final StringPath result = createString("result");

    public final StringPath status = createString("status");

    public final DateTimePath<java.sql.Timestamp> testDateTime = createDateTime("testDateTime", java.sql.Timestamp.class);

    public final StringPath version = createString("version");

    public final com.querydsl.sql.PrimaryKey<SExecution> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SCycleDefinition> executionCycledefinitionidFk = createForeignKey(cycleDefinitionId, "id");

    public final com.querydsl.sql.ForeignKey<SRun> _runExecutionidFk = createInvForeignKey(id, "execution_id");

    public final com.querydsl.sql.ForeignKey<SCountryDeployment> _countrydeploymentExecutionidFk = createInvForeignKey(id, "execution_id");

    public SExecution(String variable) {
        super(SExecution.class, forVariable(variable), "null", "execution");
        addMetadata();
    }

    public SExecution(String variable, String schema, String table) {
        super(SExecution.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SExecution(String variable, String schema) {
        super(SExecution.class, forVariable(variable), schema, "execution");
        addMetadata();
    }

    public SExecution(Path<? extends SExecution> path) {
        super(path.getType(), path.getMetadata(), "null", "execution");
        addMetadata();
    }

    public SExecution(PathMetadata metadata) {
        super(SExecution.class, metadata, "null", "execution");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(acceptance, ColumnMetadata.named("acceptance").withIndex(12).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(blockingValidation, ColumnMetadata.named("blocking_validation").withIndex(14).ofType(Types.BIT).withSize(1));
        addMetadata(branch, ColumnMetadata.named("branch").withIndex(3).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(buildDateTime, ColumnMetadata.named("build_date_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(cycleDefinitionId, ColumnMetadata.named("cycle_definition_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(discardReason, ColumnMetadata.named("discard_reason").withIndex(10).ofType(Types.VARCHAR).withSize(512));
        addMetadata(duration, ColumnMetadata.named("duration").withIndex(16).ofType(Types.BIGINT).withSize(19));
        addMetadata(estimatedDuration, ColumnMetadata.named("estimated_duration").withIndex(17).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(jobLink, ColumnMetadata.named("job_link").withIndex(20).ofType(Types.VARCHAR).withSize(256));
        addMetadata(jobUrl, ColumnMetadata.named("job_url").withIndex(8).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(qualitySeverities, ColumnMetadata.named("quality_severities").withIndex(19).ofType(Types.VARCHAR).withSize(4096));
        addMetadata(qualityStatus, ColumnMetadata.named("quality_status").withIndex(18).ofType(Types.VARCHAR).withSize(10));
        addMetadata(qualityThresholds, ColumnMetadata.named("quality_thresholds").withIndex(15).ofType(Types.VARCHAR).withSize(256));
        addMetadata(release, ColumnMetadata.named("release").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(result, ColumnMetadata.named("result").withIndex(11).ofType(Types.VARCHAR).withSize(16));
        addMetadata(status, ColumnMetadata.named("status").withIndex(9).ofType(Types.VARCHAR).withSize(16));
        addMetadata(testDateTime, ColumnMetadata.named("test_date_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(version, ColumnMetadata.named("version").withIndex(5).ofType(Types.VARCHAR).withSize(64));
    }

}

