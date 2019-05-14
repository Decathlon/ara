package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SExecutionCompletionRequest extends com.querydsl.sql.RelationalPathBase<SExecutionCompletionRequest> {

    private static final long serialVersionUID = -643194843;

    public static final SExecutionCompletionRequest executionCompletionRequest = new SExecutionCompletionRequest("execution_completion_request");

    public final StringPath jobUrl = createString("jobUrl");

    public final com.querydsl.sql.PrimaryKey<SExecutionCompletionRequest> primary = createPrimaryKey(jobUrl);

    public SExecutionCompletionRequest(String variable) {
        super(SExecutionCompletionRequest.class, forVariable(variable), "null", "execution_completion_request");
        addMetadata();
    }

    public SExecutionCompletionRequest(String variable, String schema, String table) {
        super(SExecutionCompletionRequest.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SExecutionCompletionRequest(String variable, String schema) {
        super(SExecutionCompletionRequest.class, forVariable(variable), schema, "execution_completion_request");
        addMetadata();
    }

    public SExecutionCompletionRequest(Path<? extends SExecutionCompletionRequest> path) {
        super(path.getType(), path.getMetadata(), "null", "execution_completion_request");
        addMetadata();
    }

    public SExecutionCompletionRequest(PathMetadata metadata) {
        super(SExecutionCompletionRequest.class, metadata, "null", "execution_completion_request");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(jobUrl, ColumnMetadata.named("job_url").withIndex(1).ofType(Types.VARCHAR).withSize(256).notNull());
    }

}

