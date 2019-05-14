package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QCountryDeployment extends EntityPathBase<CountryDeployment> {

    private static final long serialVersionUID = 886907442L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCountryDeployment countryDeployment = new QCountryDeployment("countryDeployment");

    public final QCountry country;

    public final NumberPath<Long> duration = createNumber("duration", Long.class);

    public final NumberPath<Long> estimatedDuration = createNumber("estimatedDuration", Long.class);

    public final QExecution execution;

    public final NumberPath<Long> executionId = createNumber("executionId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobLink = createString("jobLink");

    public final StringPath jobUrl = createString("jobUrl");

    public final StringPath platform = createString("platform");

    public final EnumPath<com.decathlon.ara.domain.enumeration.Result> result = createEnum("result", com.decathlon.ara.domain.enumeration.Result.class);

    public final DateTimePath<java.util.Date> startDateTime = createDateTime("startDateTime", java.util.Date.class);

    public final EnumPath<com.decathlon.ara.domain.enumeration.JobStatus> status = createEnum("status", com.decathlon.ara.domain.enumeration.JobStatus.class);

    public QCountryDeployment(String variable) {
        this(CountryDeployment.class, forVariable(variable), INITS);
    }

    public QCountryDeployment(Path<? extends CountryDeployment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCountryDeployment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCountryDeployment(PathMetadata metadata, PathInits inits) {
        this(CountryDeployment.class, metadata, inits);
    }

    public QCountryDeployment(Class<? extends CountryDeployment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new QCountry(forProperty("country")) : null;
        this.execution = inits.isInitialized("execution") ? new QExecution(forProperty("execution"), inits.get("execution")) : null;
    }

}

