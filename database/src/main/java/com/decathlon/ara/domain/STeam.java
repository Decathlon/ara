package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class STeam extends com.querydsl.sql.RelationalPathBase<STeam> {

    private static final long serialVersionUID = -1203939545;

    public static final STeam team = new STeam("team");

    public final BooleanPath assignableToFunctionalities = createBoolean("assignableToFunctionalities");

    public final BooleanPath assignableToProblems = createBoolean("assignableToProblems");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final com.querydsl.sql.PrimaryKey<STeam> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> teamProjectidFk = createForeignKey(projectId, "id");

    public final com.querydsl.sql.ForeignKey<SProblem> _problemBlamedteamidFk = createInvForeignKey(id, "blamed_team_id");

    public final com.querydsl.sql.ForeignKey<SFunctionality> _functionalityTeamidFk = createInvForeignKey(id, "team_id");

    public STeam(String variable) {
        super(STeam.class, forVariable(variable), "null", "team");
        addMetadata();
    }

    public STeam(String variable, String schema, String table) {
        super(STeam.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public STeam(String variable, String schema) {
        super(STeam.class, forVariable(variable), schema, "team");
        addMetadata();
    }

    public STeam(Path<? extends STeam> path) {
        super(path.getType(), path.getMetadata(), "null", "team");
        addMetadata();
    }

    public STeam(PathMetadata metadata) {
        super(STeam.class, metadata, "null", "team");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(assignableToFunctionalities, ColumnMetadata.named("assignable_to_functionalities").withIndex(4).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(assignableToProblems, ColumnMetadata.named("assignable_to_problems").withIndex(3).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

