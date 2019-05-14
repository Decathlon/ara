package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QType extends EntityPathBase<Type> {

    private static final long serialVersionUID = 1069607587L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QType type = new QType("type1");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isBrowser = createBoolean("isBrowser");

    public final BooleanPath isMobile = createBoolean("isMobile");

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final QSource source;

    public QType(String variable) {
        this(Type.class, forVariable(variable), INITS);
    }

    public QType(Path<? extends Type> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QType(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QType(PathMetadata metadata, PathInits inits) {
        this(Type.class, metadata, inits);
    }

    public QType(Class<? extends Type> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.source = inits.isInitialized("source") ? new QSource(forProperty("source")) : null;
    }

}

