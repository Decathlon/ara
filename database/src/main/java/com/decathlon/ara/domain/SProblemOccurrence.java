package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SProblemOccurrence extends com.querydsl.sql.RelationalPathBase<SProblemOccurrence> {

    private static final long serialVersionUID = 1804898630;

    public static final SProblemOccurrence problemOccurrence = new SProblemOccurrence("problem_occurrence");

    public final NumberPath<Long> errorId = createNumber("errorId", Long.class);

    public final NumberPath<Long> problemPatternId = createNumber("problemPatternId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SProblemOccurrence> primary = createPrimaryKey(errorId, problemPatternId);

    public final com.querydsl.sql.ForeignKey<SProblemPattern> problemoccurrenceProblempatternidFk = createForeignKey(problemPatternId, "id");

    public final com.querydsl.sql.ForeignKey<SError> problemoccurrenceErroridFk = createForeignKey(errorId, "id");

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

