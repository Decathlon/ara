/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

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

