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

public class SRootCause extends com.querydsl.sql.RelationalPathBase<SRootCause> {

    private static final long serialVersionUID = 2025331229;

    public static final SRootCause rootCause = new SRootCause("root_cause");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SRootCause> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> rootcauseProjectidFk = createForeignKey(projectId, "id");

    public final com.querydsl.sql.ForeignKey<SProblem> _problemRootcauseidFk = createInvForeignKey(id, "root_cause_id");

    public SRootCause(String variable) {
        super(SRootCause.class, forVariable(variable), "null", "root_cause");
        addMetadata();
    }

    public SRootCause(String variable, String schema, String table) {
        super(SRootCause.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SRootCause(String variable, String schema) {
        super(SRootCause.class, forVariable(variable), schema, "root_cause");
        addMetadata();
    }

    public SRootCause(Path<? extends SRootCause> path) {
        super(path.getType(), path.getMetadata(), "null", "root_cause");
        addMetadata();
    }

    public SRootCause(PathMetadata metadata) {
        super(SRootCause.class, metadata, "null", "root_cause");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

