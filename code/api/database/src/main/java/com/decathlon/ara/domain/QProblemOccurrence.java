package com.decathlon.ara.domain;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathInits;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QProblemOccurrence extends EntityPathBase<ProblemOccurrence> {

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProblemOccurrence problemOccurrence = new QProblemOccurrence("problemOccurrence");

    public final NumberPath<Long> errorId = createNumber("errorId", Long.class);

    public final NumberPath<Long> problemPatternId = createNumber("problemPatternId", Long.class);

    public final QError error;

    public final QProblemPattern problemPattern;

    public QProblemOccurrence(String variable) {
        this(ProblemOccurrence.class, forVariable(variable), INITS);
    }

    public QProblemOccurrence(Path<? extends ProblemOccurrence> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProblemOccurrence(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProblemOccurrence(PathMetadata metadata, PathInits inits) {
        this(ProblemOccurrence.class, metadata, inits);
    }

    public QProblemOccurrence(Class<? extends ProblemOccurrence> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.error = inits.isInitialized("problemOccurrenceId.error") ? new QError(forProperty("problemOccurrenceId.error")) : null;
        var problemOccurrenceIdProblemPattern = "problemOccurrenceId.problemPattern";
        this.problemPattern = inits.isInitialized(problemOccurrenceIdProblemPattern) ? new QProblemPattern(forProperty(problemOccurrenceIdProblemPattern), inits.get(problemOccurrenceIdProblemPattern)) : null;
    }
}
