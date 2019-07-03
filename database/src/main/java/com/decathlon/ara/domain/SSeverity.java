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

public class SSeverity extends com.querydsl.sql.RelationalPathBase<SSeverity> {

    private static final long serialVersionUID = -644304185;

    public static final SSeverity severity = new SSeverity("severity");

    public final StringPath code = createString("code");

    public final BooleanPath defaultOnMissing = createBoolean("defaultOnMissing");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath initials = createString("initials");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> position = createNumber("position", Integer.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath shortName = createString("shortName");

    public final com.querydsl.sql.PrimaryKey<SSeverity> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> severityProjectidFk = createForeignKey(projectId, "id");

    public SSeverity(String variable) {
        super(SSeverity.class, forVariable(variable), "null", "severity");
        addMetadata();
    }

    public SSeverity(String variable, String schema, String table) {
        super(SSeverity.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SSeverity(String variable, String schema) {
        super(SSeverity.class, forVariable(variable), schema, "severity");
        addMetadata();
    }

    public SSeverity(Path<? extends SSeverity> path) {
        super(path.getType(), path.getMetadata(), "null", "severity");
        addMetadata();
    }

    public SSeverity(PathMetadata metadata) {
        super(SSeverity.class, metadata, "null", "severity");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(defaultOnMissing, ColumnMetadata.named("default_on_missing").withIndex(5).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(7).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(initials, ColumnMetadata.named("initials").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(position, ColumnMetadata.named("position").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(8).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(shortName, ColumnMetadata.named("short_name").withIndex(4).ofType(Types.VARCHAR).withSize(16).notNull());
    }

}

