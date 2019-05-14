package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SCountryDeployment extends com.querydsl.sql.RelationalPathBase<SCountryDeployment> {

    private static final long serialVersionUID = 1100902001;

    public static final SCountryDeployment countryDeployment = new SCountryDeployment("country_deployment");

    public final NumberPath<Long> countryId = createNumber("countryId", Long.class);

    public final NumberPath<Long> duration = createNumber("duration", Long.class);

    public final NumberPath<Long> estimatedDuration = createNumber("estimatedDuration", Long.class);

    public final NumberPath<Long> executionId = createNumber("executionId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobLink = createString("jobLink");

    public final StringPath jobUrl = createString("jobUrl");

    public final StringPath platform = createString("platform");

    public final StringPath result = createString("result");

    public final DateTimePath<java.sql.Timestamp> startDateTime = createDateTime("startDateTime", java.sql.Timestamp.class);

    public final StringPath status = createString("status");

    public final com.querydsl.sql.PrimaryKey<SCountryDeployment> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SExecution> countrydeploymentExecutionidFk = createForeignKey(executionId, "id");

    public final com.querydsl.sql.ForeignKey<SCountry> countrydeploymentCountryidFk = createForeignKey(countryId, "id");

    public SCountryDeployment(String variable) {
        super(SCountryDeployment.class, forVariable(variable), "null", "country_deployment");
        addMetadata();
    }

    public SCountryDeployment(String variable, String schema, String table) {
        super(SCountryDeployment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SCountryDeployment(String variable, String schema) {
        super(SCountryDeployment.class, forVariable(variable), schema, "country_deployment");
        addMetadata();
    }

    public SCountryDeployment(Path<? extends SCountryDeployment> path) {
        super(path.getType(), path.getMetadata(), "null", "country_deployment");
        addMetadata();
    }

    public SCountryDeployment(PathMetadata metadata) {
        super(SCountryDeployment.class, metadata, "null", "country_deployment");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(countryId, ColumnMetadata.named("country_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(duration, ColumnMetadata.named("duration").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(estimatedDuration, ColumnMetadata.named("estimated_duration").withIndex(8).ofType(Types.BIGINT).withSize(19));
        addMetadata(executionId, ColumnMetadata.named("execution_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(jobLink, ColumnMetadata.named("job_link").withIndex(11).ofType(Types.VARCHAR).withSize(256));
        addMetadata(jobUrl, ColumnMetadata.named("job_url").withIndex(4).ofType(Types.VARCHAR).withSize(256));
        addMetadata(platform, ColumnMetadata.named("platform").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(result, ColumnMetadata.named("result").withIndex(6).ofType(Types.VARCHAR).withSize(16));
        addMetadata(startDateTime, ColumnMetadata.named("start_date_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(5).ofType(Types.VARCHAR).withSize(16).notNull());
    }

}

