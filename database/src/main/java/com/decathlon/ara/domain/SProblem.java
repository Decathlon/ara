package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SProblem extends com.querydsl.sql.RelationalPathBase<SProblem> {

    private static final long serialVersionUID = 1838743957;

    public static final SProblem problem = new SProblem("problem");

    public final NumberPath<Long> blamedTeamId = createNumber("blamedTeamId", Long.class);

    public final DateTimePath<java.sql.Timestamp> closingDateTime = createDateTime("closingDateTime", java.sql.Timestamp.class);

    public final StringPath comment = createString("comment");

    public final DateTimePath<java.sql.Timestamp> creationDateTime = createDateTime("creationDateTime", java.sql.Timestamp.class);

    public final StringPath defectExistence = createString("defectExistence");

    public final StringPath defectId = createString("defectId");

    public final DateTimePath<java.sql.Timestamp> firstSeenDateTime = createDateTime("firstSeenDateTime", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.sql.Timestamp> lastSeenDateTime = createDateTime("lastSeenDateTime", java.sql.Timestamp.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final NumberPath<Long> rootCauseId = createNumber("rootCauseId", Long.class);

    public final StringPath status = createString("status");

    public final com.querydsl.sql.PrimaryKey<SProblem> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<STeam> problemBlamedteamidFk = createForeignKey(blamedTeamId, "id");

    public final com.querydsl.sql.ForeignKey<SRootCause> problemRootcauseidFk = createForeignKey(rootCauseId, "id");

    public final com.querydsl.sql.ForeignKey<SProblemPattern> _problempatternProblemidFk = createInvForeignKey(id, "problem_id");

    public SProblem(String variable) {
        super(SProblem.class, forVariable(variable), "null", "problem");
        addMetadata();
    }

    public SProblem(String variable, String schema, String table) {
        super(SProblem.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SProblem(String variable, String schema) {
        super(SProblem.class, forVariable(variable), schema, "problem");
        addMetadata();
    }

    public SProblem(Path<? extends SProblem> path) {
        super(path.getType(), path.getMetadata(), "null", "problem");
        addMetadata();
    }

    public SProblem(PathMetadata metadata) {
        super(SProblem.class, metadata, "null", "problem");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blamedTeamId, ColumnMetadata.named("blamed_team_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(closingDateTime, ColumnMetadata.named("closing_date_time").withIndex(10).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(comment, ColumnMetadata.named("comment").withIndex(3).ofType(Types.LONGVARCHAR).withSize(2147483647));
        addMetadata(creationDateTime, ColumnMetadata.named("creation_date_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(defectExistence, ColumnMetadata.named("defect_existence").withIndex(9).ofType(Types.VARCHAR).withSize(11));
        addMetadata(defectId, ColumnMetadata.named("defect_id").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(firstSeenDateTime, ColumnMetadata.named("first_seen_date_time").withIndex(12).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lastSeenDateTime, ColumnMetadata.named("last_seen_date_time").withIndex(13).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(rootCauseId, ColumnMetadata.named("root_cause_id").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(4).ofType(Types.VARCHAR).withSize(21).notNull());
    }

}

