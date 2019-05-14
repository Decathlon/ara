package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SCommunication extends com.querydsl.sql.RelationalPathBase<SCommunication> {

    private static final long serialVersionUID = -1595048372;

    public static final SCommunication communication = new SCommunication("communication");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath message = createString("message");

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<SCommunication> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> communicationProjectidFk = createForeignKey(projectId, "id");

    public SCommunication(String variable) {
        super(SCommunication.class, forVariable(variable), "null", "communication");
        addMetadata();
    }

    public SCommunication(String variable, String schema, String table) {
        super(SCommunication.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SCommunication(String variable, String schema) {
        super(SCommunication.class, forVariable(variable), schema, "communication");
        addMetadata();
    }

    public SCommunication(Path<? extends SCommunication> path) {
        super(path.getType(), path.getMetadata(), "null", "communication");
        addMetadata();
    }

    public SCommunication(PathMetadata metadata) {
        super(SCommunication.class, metadata, "null", "communication");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(message, ColumnMetadata.named("message").withIndex(4).ofType(Types.LONGVARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(6).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(type, ColumnMetadata.named("type").withIndex(3).ofType(Types.VARCHAR).withSize(4).notNull());
    }

}

