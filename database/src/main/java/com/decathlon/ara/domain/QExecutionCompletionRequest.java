package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

public class QExecutionCompletionRequest extends EntityPathBase<ExecutionCompletionRequest> {

    private static final long serialVersionUID = 832738692L;

    public static final QExecutionCompletionRequest executionCompletionRequest = new QExecutionCompletionRequest("executionCompletionRequest");

    public final StringPath jobUrl = createString("jobUrl");

    public QExecutionCompletionRequest(String variable) {
        super(ExecutionCompletionRequest.class, forVariable(variable));
    }

    public QExecutionCompletionRequest(Path<? extends ExecutionCompletionRequest> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExecutionCompletionRequest(PathMetadata metadata) {
        super(ExecutionCompletionRequest.class, metadata);
    }

}

