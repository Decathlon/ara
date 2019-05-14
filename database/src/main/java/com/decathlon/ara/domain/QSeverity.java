package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

public class QSeverity extends EntityPathBase<Severity> {

    private static final long serialVersionUID = -892759578L;

    public static final QSeverity severity = new QSeverity("severity");

    public final StringPath code = createString("code");

    public final BooleanPath defaultOnMissing = createBoolean("defaultOnMissing");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath initials = createString("initials");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> position = createNumber("position", Integer.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath shortName = createString("shortName");

    public QSeverity(String variable) {
        super(Severity.class, forVariable(variable));
    }

    public QSeverity(Path<? extends Severity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSeverity(PathMetadata metadata) {
        super(Severity.class, metadata);
    }

}

