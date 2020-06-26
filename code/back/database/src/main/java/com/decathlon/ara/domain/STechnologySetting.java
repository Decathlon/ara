/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class STechnologySetting extends RelationalPathBase<STechnologySetting> {

    public static final STechnologySetting technologySetting = new STechnologySetting("technology_setting");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath value = createString("value");

    public final PrimaryKey<STechnologySetting> primary = createPrimaryKey(id);

    public final StringPath technology = createString("technology");

    public final ForeignKey<SProject> technologySettingProjectidFk = createForeignKey(projectId, "id");

    public STechnologySetting(String variable) {
        super(STechnologySetting.class, forVariable(variable), "null", "technology_setting");
        addMetadata();
    }

    public STechnologySetting(String variable, String schema, String table) {
        super(STechnologySetting.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public STechnologySetting(String variable, String schema) {
        super(STechnologySetting.class, forVariable(variable), schema, "technology_setting");
        addMetadata();
    }

    public STechnologySetting(Path<? extends STechnologySetting> path) {
        super(path.getType(), path.getMetadata(), "null", "technology_setting");
        addMetadata();
    }

    public STechnologySetting(PathMetadata metadata) {
        super(STechnologySetting.class, metadata, "null", "technology_setting");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(3).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(value, ColumnMetadata.named("value").withIndex(4).ofType(Types.VARCHAR).withSize(512));
        addMetadata(technology, ColumnMetadata.named("technology").withIndex(4).ofType(Types.VARCHAR).withSize(16).notNull());
    }
}
