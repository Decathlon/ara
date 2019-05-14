package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SDatabasechangelog extends com.querydsl.sql.RelationalPathBase<SDatabasechangelog> {

    private static final long serialVersionUID = 1914412527;

    public static final SDatabasechangelog databasechangelog = new SDatabasechangelog("DATABASECHANGELOG");

    public final StringPath author = createString("author");

    public final StringPath comments = createString("comments");

    public final StringPath contexts = createString("contexts");

    public final DateTimePath<java.sql.Timestamp> dateexecuted = createDateTime("dateexecuted", java.sql.Timestamp.class);

    public final StringPath deploymentId = createString("deploymentId");

    public final StringPath description = createString("description");

    public final StringPath exectype = createString("exectype");

    public final StringPath filename = createString("filename");

    public final StringPath id = createString("id");

    public final StringPath labels = createString("labels");

    public final StringPath liquibase = createString("liquibase");

    public final StringPath md5sum = createString("md5sum");

    public final NumberPath<Integer> orderexecuted = createNumber("orderexecuted", Integer.class);

    public final StringPath tag = createString("tag");

    public SDatabasechangelog(String variable) {
        super(SDatabasechangelog.class, forVariable(variable), "null", "DATABASECHANGELOG");
        addMetadata();
    }

    public SDatabasechangelog(String variable, String schema, String table) {
        super(SDatabasechangelog.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SDatabasechangelog(String variable, String schema) {
        super(SDatabasechangelog.class, forVariable(variable), schema, "DATABASECHANGELOG");
        addMetadata();
    }

    public SDatabasechangelog(Path<? extends SDatabasechangelog> path) {
        super(path.getType(), path.getMetadata(), "null", "DATABASECHANGELOG");
        addMetadata();
    }

    public SDatabasechangelog(PathMetadata metadata) {
        super(SDatabasechangelog.class, metadata, "null", "DATABASECHANGELOG");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(author, ColumnMetadata.named("AUTHOR").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(comments, ColumnMetadata.named("COMMENTS").withIndex(9).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contexts, ColumnMetadata.named("CONTEXTS").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(dateexecuted, ColumnMetadata.named("DATEEXECUTED").withIndex(4).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(deploymentId, ColumnMetadata.named("DEPLOYMENT_ID").withIndex(14).ofType(Types.VARCHAR).withSize(10));
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(8).ofType(Types.VARCHAR).withSize(255));
        addMetadata(exectype, ColumnMetadata.named("EXECTYPE").withIndex(6).ofType(Types.VARCHAR).withSize(10).notNull());
        addMetadata(filename, ColumnMetadata.named("FILENAME").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(labels, ColumnMetadata.named("LABELS").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(liquibase, ColumnMetadata.named("LIQUIBASE").withIndex(11).ofType(Types.VARCHAR).withSize(20));
        addMetadata(md5sum, ColumnMetadata.named("MD5SUM").withIndex(7).ofType(Types.VARCHAR).withSize(35));
        addMetadata(orderexecuted, ColumnMetadata.named("ORDEREXECUTED").withIndex(5).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(tag, ColumnMetadata.named("TAG").withIndex(10).ofType(Types.VARCHAR).withSize(255));
    }

}

