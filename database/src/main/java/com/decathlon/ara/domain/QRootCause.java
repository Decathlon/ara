package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QRootCause extends EntityPathBase<RootCause> {

    private static final long serialVersionUID = -1381818658L;

    public static final QRootCause rootCause = new QRootCause("rootCause");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final ListPath<Problem, QProblem> problems = this.<Problem, QProblem>createList("problems", Problem.class, QProblem.class, PathInits.DIRECT2);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public QRootCause(String variable) {
        super(RootCause.class, forVariable(variable));
    }

    public QRootCause(Path<? extends RootCause> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRootCause(PathMetadata metadata) {
        super(RootCause.class, metadata);
    }

}

