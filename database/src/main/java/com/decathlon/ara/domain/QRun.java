package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QRun extends EntityPathBase<Run> {

    private static final long serialVersionUID = -1350971902L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRun run = new QRun("run");

    public final StringPath comment = createString("comment");

    public final QCountry country;

    public final StringPath countryTags = createString("countryTags");

    public final NumberPath<Long> duration = createNumber("duration", Long.class);

    public final NumberPath<Long> estimatedDuration = createNumber("estimatedDuration", Long.class);

    public final SetPath<ExecutedScenario, QExecutedScenario> executedScenarios = this.<ExecutedScenario, QExecutedScenario>createSet("executedScenarios", ExecutedScenario.class, QExecutedScenario.class, PathInits.DIRECT2);

    public final QExecution execution;

    public final NumberPath<Long> executionId = createNumber("executionId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath includeInThresholds = createBoolean("includeInThresholds");

    public final StringPath jobLink = createString("jobLink");

    public final StringPath jobUrl = createString("jobUrl");

    public final StringPath platform = createString("platform");

    public final StringPath severityTags = createString("severityTags");

    public final DateTimePath<java.util.Date> startDateTime = createDateTime("startDateTime", java.util.Date.class);

    public final EnumPath<com.decathlon.ara.domain.enumeration.JobStatus> status = createEnum("status", com.decathlon.ara.domain.enumeration.JobStatus.class);

    public final QType type;

    public QRun(String variable) {
        this(Run.class, forVariable(variable), INITS);
    }

    public QRun(Path<? extends Run> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRun(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRun(PathMetadata metadata, PathInits inits) {
        this(Run.class, metadata, inits);
    }

    public QRun(Class<? extends Run> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new QCountry(forProperty("country")) : null;
        this.execution = inits.isInitialized("execution") ? new QExecution(forProperty("execution"), inits.get("execution")) : null;
        this.type = inits.isInitialized("type") ? new QType(forProperty("type"), inits.get("type")) : null;
    }

}

