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

public class SRun extends com.querydsl.sql.RelationalPathBase<SRun> {

    private static final long serialVersionUID = 1069540481;

    public static final SRun run = new SRun("run");

    public final StringPath comment = createString("comment");

    public final NumberPath<Long> countryId = createNumber("countryId", Long.class);

    public final StringPath countryTags = createString("countryTags");

    public final NumberPath<Long> duration = createNumber("duration", Long.class);

    public final NumberPath<Long> estimatedDuration = createNumber("estimatedDuration", Long.class);

    public final NumberPath<Long> executionId = createNumber("executionId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath includeInThresholds = createBoolean("includeInThresholds");

    public final StringPath jobLink = createString("jobLink");

    public final StringPath jobUrl = createString("jobUrl");

    public final StringPath platform = createString("platform");

    public final StringPath severityTags = createString("severityTags");

    public final DateTimePath<java.sql.Timestamp> startDateTime = createDateTime("startDateTime", java.sql.Timestamp.class);

    public final StringPath status = createString("status");

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SRun> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SType> runTypeidFk = createForeignKey(typeId, "id");

    public final com.querydsl.sql.ForeignKey<SExecution> runExecutionidFk = createForeignKey(executionId, "id");

    public final com.querydsl.sql.ForeignKey<SCountry> runCountryidFk = createForeignKey(countryId, "id");

    public final com.querydsl.sql.ForeignKey<SExecutedScenario> _executedscenarioRunidFk = createInvForeignKey(id, "run_id");

    public SRun(String variable) {
        super(SRun.class, forVariable(variable), "null", "run");
        addMetadata();
    }

    public SRun(String variable, String schema, String table) {
        super(SRun.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SRun(String variable, String schema) {
        super(SRun.class, forVariable(variable), schema, "run");
        addMetadata();
    }

    public SRun(Path<? extends SRun> path) {
        super(path.getType(), path.getMetadata(), "null", "run");
        addMetadata();
    }

    public SRun(PathMetadata metadata) {
        super(SRun.class, metadata, "null", "run");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(comment, ColumnMetadata.named("comment").withIndex(15).ofType(Types.VARCHAR).withSize(256));
        addMetadata(countryId, ColumnMetadata.named("country_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(countryTags, ColumnMetadata.named("country_tags").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(duration, ColumnMetadata.named("duration").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(estimatedDuration, ColumnMetadata.named("estimated_duration").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(executionId, ColumnMetadata.named("execution_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(includeInThresholds, ColumnMetadata.named("include_in_thresholds").withIndex(8).ofType(Types.BIT).withSize(1));
        addMetadata(jobLink, ColumnMetadata.named("job_link").withIndex(14).ofType(Types.VARCHAR).withSize(256));
        addMetadata(jobUrl, ColumnMetadata.named("job_url").withIndex(4).ofType(Types.VARCHAR).withSize(256));
        addMetadata(platform, ColumnMetadata.named("platform").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(severityTags, ColumnMetadata.named("severity_tags").withIndex(7).ofType(Types.VARCHAR).withSize(64));
        addMetadata(startDateTime, ColumnMetadata.named("start_date_time").withIndex(9).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(5).ofType(Types.VARCHAR).withSize(16));
        addMetadata(typeId, ColumnMetadata.named("type_id").withIndex(12).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

