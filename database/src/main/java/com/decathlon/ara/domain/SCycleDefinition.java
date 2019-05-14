package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SCycleDefinition extends com.querydsl.sql.RelationalPathBase<SCycleDefinition> {

    private static final long serialVersionUID = -1231934513;

    public static final SCycleDefinition cycleDefinition = new SCycleDefinition("cycle_definition");

    public final StringPath branch = createString("branch");

    public final NumberPath<Integer> branchPosition = createNumber("branchPosition", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SCycleDefinition> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> cycledefinitionProjectidFk = createForeignKey(projectId, "id");

    public final com.querydsl.sql.ForeignKey<SExecution> _executionCycledefinitionidFk = createInvForeignKey(id, "cycle_definition_id");

    public SCycleDefinition(String variable) {
        super(SCycleDefinition.class, forVariable(variable), "null", "cycle_definition");
        addMetadata();
    }

    public SCycleDefinition(String variable, String schema, String table) {
        super(SCycleDefinition.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SCycleDefinition(String variable, String schema) {
        super(SCycleDefinition.class, forVariable(variable), schema, "cycle_definition");
        addMetadata();
    }

    public SCycleDefinition(Path<? extends SCycleDefinition> path) {
        super(path.getType(), path.getMetadata(), "null", "cycle_definition");
        addMetadata();
    }

    public SCycleDefinition(PathMetadata metadata) {
        super(SCycleDefinition.class, metadata, "null", "cycle_definition");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("branch").withIndex(3).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(branchPosition, ColumnMetadata.named("branch_position").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

