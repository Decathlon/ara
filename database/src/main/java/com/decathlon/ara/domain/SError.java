package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SError extends com.querydsl.sql.RelationalPathBase<SError> {

    private static final long serialVersionUID = 1319130750;

    public static final SError error = new SError("error");

    public final StringPath exception = createString("exception");

    public final NumberPath<Long> executedScenarioId = createNumber("executedScenarioId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath step = createString("step");

    public final StringPath stepDefinition = createString("stepDefinition");

    public final NumberPath<Integer> stepLine = createNumber("stepLine", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SError> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SExecutedScenario> errorExecutedscenarioidFk = createForeignKey(executedScenarioId, "id");

    public final com.querydsl.sql.ForeignKey<SProblemOccurrence> _problemoccurrenceErroridFk = createInvForeignKey(id, "error_id");

    public SError(String variable) {
        super(SError.class, forVariable(variable), "null", "error");
        addMetadata();
    }

    public SError(String variable, String schema, String table) {
        super(SError.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SError(String variable, String schema) {
        super(SError.class, forVariable(variable), schema, "error");
        addMetadata();
    }

    public SError(Path<? extends SError> path) {
        super(path.getType(), path.getMetadata(), "null", "error");
        addMetadata();
    }

    public SError(PathMetadata metadata) {
        super(SError.class, metadata, "null", "error");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(exception, ColumnMetadata.named("exception").withIndex(4).ofType(Types.LONGVARCHAR).withSize(2147483647).notNull());
        addMetadata(executedScenarioId, ColumnMetadata.named("executed_scenario_id").withIndex(6).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(step, ColumnMetadata.named("step").withIndex(2).ofType(Types.VARCHAR).withSize(2048).notNull());
        addMetadata(stepDefinition, ColumnMetadata.named("step_definition").withIndex(3).ofType(Types.VARCHAR).withSize(2048).notNull());
        addMetadata(stepLine, ColumnMetadata.named("step_line").withIndex(5).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

