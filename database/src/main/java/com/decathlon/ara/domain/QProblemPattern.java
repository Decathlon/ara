package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QProblemPattern extends EntityPathBase<ProblemPattern> {

    private static final long serialVersionUID = -1841083366L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProblemPattern problemPattern = new QProblemPattern("problemPattern");

    public final QCountry country;

    public final SetPath<Error, QError> errors = this.<Error, QError>createSet("errors", Error.class, QError.class, PathInits.DIRECT2);

    public final StringPath exception = createString("exception");

    public final StringPath featureFile = createString("featureFile");

    public final StringPath featureName = createString("featureName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath platform = createString("platform");

    public final QProblem problem;

    public final NumberPath<Long> problemId = createNumber("problemId", Long.class);

    public final StringPath release = createString("release");

    public final StringPath scenarioName = createString("scenarioName");

    public final BooleanPath scenarioNameStartsWith = createBoolean("scenarioNameStartsWith");

    public final StringPath step = createString("step");

    public final StringPath stepDefinition = createString("stepDefinition");

    public final BooleanPath stepDefinitionStartsWith = createBoolean("stepDefinitionStartsWith");

    public final BooleanPath stepStartsWith = createBoolean("stepStartsWith");

    public final QType type;

    public final BooleanPath typeIsBrowser = createBoolean("typeIsBrowser");

    public final BooleanPath typeIsMobile = createBoolean("typeIsMobile");

    public QProblemPattern(String variable) {
        this(ProblemPattern.class, forVariable(variable), INITS);
    }

    public QProblemPattern(Path<? extends ProblemPattern> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProblemPattern(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProblemPattern(PathMetadata metadata, PathInits inits) {
        this(ProblemPattern.class, metadata, inits);
    }

    public QProblemPattern(Class<? extends ProblemPattern> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new QCountry(forProperty("country")) : null;
        this.problem = inits.isInitialized("problem") ? new QProblem(forProperty("problem"), inits.get("problem")) : null;
        this.type = inits.isInitialized("type") ? new QType(forProperty("type"), inits.get("type")) : null;
    }

}

