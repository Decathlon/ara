package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SSetting extends com.querydsl.sql.RelationalPathBase<SSetting> {

    private static final long serialVersionUID = -160740026;

    public static final SSetting setting = new SSetting("setting");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath value = createString("value");

    public final com.querydsl.sql.PrimaryKey<SSetting> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> settingProjectidFk = createForeignKey(projectId, "id");

    public SSetting(String variable) {
        super(SSetting.class, forVariable(variable), "null", "setting");
        addMetadata();
    }

    public SSetting(String variable, String schema, String table) {
        super(SSetting.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SSetting(String variable, String schema) {
        super(SSetting.class, forVariable(variable), schema, "setting");
        addMetadata();
    }

    public SSetting(Path<? extends SSetting> path) {
        super(path.getType(), path.getMetadata(), "null", "setting");
        addMetadata();
    }

    public SSetting(PathMetadata metadata) {
        super(SSetting.class, metadata, "null", "setting");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(3).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(value, ColumnMetadata.named("value").withIndex(4).ofType(Types.VARCHAR).withSize(512));
    }

}

