package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QError extends EntityPathBase<Error> {

    private static final long serialVersionUID = -1215962177L;

    private static final PathInits INITS = new PathInits("*", "executedScenario.run.*.*");

    public static final QError error = new QError("error");

    public final StringPath exception = createString("exception");

    public final QExecutedScenario executedScenario;

    public final NumberPath<Long> executedScenarioId = createNumber("executedScenarioId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final SetPath<ProblemPattern, QProblemPattern> problemPatterns = this.<ProblemPattern, QProblemPattern>createSet("problemPatterns", ProblemPattern.class, QProblemPattern.class, PathInits.DIRECT2);

    public final StringPath step = createString("step");

    public final StringPath stepDefinition = createString("stepDefinition");

    public final NumberPath<Integer> stepLine = createNumber("stepLine", Integer.class);

    public QError(String variable) {
        this(Error.class, forVariable(variable), INITS);
    }

    public QError(Path<? extends Error> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QError(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QError(PathMetadata metadata, PathInits inits) {
        this(Error.class, metadata, inits);
    }

    public QError(Class<? extends Error> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.executedScenario = inits.isInitialized("executedScenario") ? new QExecutedScenario(forProperty("executedScenario"), inits.get("executedScenario")) : null;
    }

}

