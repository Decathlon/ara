package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QProblem extends EntityPathBase<Problem> {

    private static final long serialVersionUID = 860897942L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProblem problem = new QProblem("problem");

    public final QTeam blamedTeam;

    public final DateTimePath<java.util.Date> closingDateTime = createDateTime("closingDateTime", java.util.Date.class);

    public final StringPath comment = createString("comment");

    public final DateTimePath<java.util.Date> creationDateTime = createDateTime("creationDateTime", java.util.Date.class);

    public final EnumPath<com.decathlon.ara.domain.enumeration.DefectExistence> defectExistence = createEnum("defectExistence", com.decathlon.ara.domain.enumeration.DefectExistence.class);

    public final StringPath defectId = createString("defectId");

    public final DateTimePath<java.util.Date> firstSeenDateTime = createDateTime("firstSeenDateTime", java.util.Date.class);

    public final BooleanPath handled = createBoolean("handled");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.util.Date> lastSeenDateTime = createDateTime("lastSeenDateTime", java.util.Date.class);

    public final StringPath name = createString("name");

    public final ListPath<ProblemPattern, QProblemPattern> patterns = this.<ProblemPattern, QProblemPattern>createList("patterns", ProblemPattern.class, QProblemPattern.class, PathInits.DIRECT2);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final QRootCause rootCause;

    public final EnumPath<com.decathlon.ara.domain.enumeration.ProblemStatus> status = createEnum("status", com.decathlon.ara.domain.enumeration.ProblemStatus.class);

    public QProblem(String variable) {
        this(Problem.class, forVariable(variable), INITS);
    }

    public QProblem(Path<? extends Problem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProblem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProblem(PathMetadata metadata, PathInits inits) {
        this(Problem.class, metadata, inits);
    }

    public QProblem(Class<? extends Problem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.blamedTeam = inits.isInitialized("blamedTeam") ? new QTeam(forProperty("blamedTeam")) : null;
        this.rootCause = inits.isInitialized("rootCause") ? new QRootCause(forProperty("rootCause")) : null;
    }

}

