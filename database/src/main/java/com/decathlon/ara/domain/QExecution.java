package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QExecution extends EntityPathBase<Execution> {

    private static final long serialVersionUID = -1579390641L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExecution execution = new QExecution("execution");

    public final EnumPath<com.decathlon.ara.domain.enumeration.ExecutionAcceptance> acceptance = createEnum("acceptance", com.decathlon.ara.domain.enumeration.ExecutionAcceptance.class);

    public final BooleanPath blockingValidation = createBoolean("blockingValidation");

    public final StringPath branch = createString("branch");

    public final DateTimePath<java.util.Date> buildDateTime = createDateTime("buildDateTime", java.util.Date.class);

    public final SetPath<CountryDeployment, QCountryDeployment> countryDeployments = this.<CountryDeployment, QCountryDeployment>createSet("countryDeployments", CountryDeployment.class, QCountryDeployment.class, PathInits.DIRECT2);

    public final QCycleDefinition cycleDefinition;

    public final StringPath discardReason = createString("discardReason");

    public final NumberPath<Long> duration = createNumber("duration", Long.class);

    public final NumberPath<Long> estimatedDuration = createNumber("estimatedDuration", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobLink = createString("jobLink");

    public final StringPath jobUrl = createString("jobUrl");

    public final StringPath name = createString("name");

    public final StringPath qualitySeverities = createString("qualitySeverities");

    public final EnumPath<com.decathlon.ara.domain.enumeration.QualityStatus> qualityStatus = createEnum("qualityStatus", com.decathlon.ara.domain.enumeration.QualityStatus.class);

    public final StringPath qualityThresholds = createString("qualityThresholds");

    public final StringPath release = createString("release");

    public final EnumPath<com.decathlon.ara.domain.enumeration.Result> result = createEnum("result", com.decathlon.ara.domain.enumeration.Result.class);

    public final SetPath<Run, QRun> runs = this.<Run, QRun>createSet("runs", Run.class, QRun.class, PathInits.DIRECT2);

    public final EnumPath<com.decathlon.ara.domain.enumeration.JobStatus> status = createEnum("status", com.decathlon.ara.domain.enumeration.JobStatus.class);

    public final DateTimePath<java.util.Date> testDateTime = createDateTime("testDateTime", java.util.Date.class);

    public final StringPath version = createString("version");

    public QExecution(String variable) {
        this(Execution.class, forVariable(variable), INITS);
    }

    public QExecution(Path<? extends Execution> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExecution(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExecution(PathMetadata metadata, PathInits inits) {
        this(Execution.class, metadata, inits);
    }

    public QExecution(Class<? extends Execution> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.cycleDefinition = inits.isInitialized("cycleDefinition") ? new QCycleDefinition(forProperty("cycleDefinition")) : null;
    }

}

