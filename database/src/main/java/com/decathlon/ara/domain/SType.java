package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SType extends com.querydsl.sql.RelationalPathBase<SType> {

    private static final long serialVersionUID = -1203919868;

    public static final SType type = new SType("type");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isBrowser = createBoolean("isBrowser");

    public final BooleanPath isMobile = createBoolean("isMobile");

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final NumberPath<Long> sourceId = createNumber("sourceId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SType> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> typeProjectidFk = createForeignKey(projectId, "id");

    public final com.querydsl.sql.ForeignKey<SSource> typeSourceidFk = createForeignKey(sourceId, "id");

    public final com.querydsl.sql.ForeignKey<SRun> _runTypeidFk = createInvForeignKey(id, "type_id");

    public final com.querydsl.sql.ForeignKey<SProblemPattern> _problempatternTypeidFk = createInvForeignKey(id, "type_id");

    public SType(String variable) {
        super(SType.class, forVariable(variable), "null", "type");
        addMetadata();
    }

    public SType(String variable, String schema, String table) {
        super(SType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SType(String variable, String schema) {
        super(SType.class, forVariable(variable), schema, "type");
        addMetadata();
    }

    public SType(Path<? extends SType> path) {
        super(path.getType(), path.getMetadata(), "null", "type");
        addMetadata();
    }

    public SType(PathMetadata metadata) {
        super(SType.class, metadata, "null", "type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(1).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(6).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(isBrowser, ColumnMetadata.named("is_browser").withIndex(3).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(isMobile, ColumnMetadata.named("is_mobile").withIndex(4).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(50).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(7).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(sourceId, ColumnMetadata.named("source_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

