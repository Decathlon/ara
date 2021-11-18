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
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class SProblemOccurrence extends RelationalPathBase<SProblemOccurrence> {

    private static final long serialVersionUID = 1804898630;

    public static final SProblemOccurrence problemOccurrence = new SProblemOccurrence("problem_occurrence");

    public final NumberPath<Long> errorId = createNumber("errorId", Long.class);

    public final NumberPath<Long> problemPatternId = createNumber("problemPatternId", Long.class);

    public final PrimaryKey<SProblemOccurrence> primary = createPrimaryKey(errorId, problemPatternId);

    public final ForeignKey<SProblemPattern> problemOccurrenceProblemPatternIdFk = createForeignKey(problemPatternId, "id");

    public final ForeignKey<SError> problemOccurrenceErrorIdFk = createForeignKey(errorId, "id");

    public SProblemOccurrence(String variable) {
        super(SProblemOccurrence.class, forVariable(variable), "null", "problem_occurrence");
        addMetadata();
    }

    public SProblemOccurrence(String variable, String schema, String table) {
        super(SProblemOccurrence.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SProblemOccurrence(String variable, String schema) {
        super(SProblemOccurrence.class, forVariable(variable), schema, "problem_occurrence");
        addMetadata();
    }

    public SProblemOccurrence(Path<? extends SProblemOccurrence> path) {
        super(path.getType(), path.getMetadata(), "null", "problem_occurrence");
        addMetadata();
    }

    public SProblemOccurrence(PathMetadata metadata) {
        super(SProblemOccurrence.class, metadata, "null", "problem_occurrence");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(errorId, ColumnMetadata.named("error_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(problemPatternId, ColumnMetadata.named("problem_pattern_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

