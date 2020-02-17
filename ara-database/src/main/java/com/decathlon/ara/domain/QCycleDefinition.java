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

public class QCycleDefinition extends EntityPathBase<CycleDefinition> {

    private static final long serialVersionUID = -910369840L;

    public static final QCycleDefinition cycleDefinition = new QCycleDefinition("cycleDefinition");

    public final StringPath branch = createString("branch");

    public final NumberPath<Integer> branchPosition = createNumber("branchPosition", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public QCycleDefinition(String variable) {
        super(CycleDefinition.class, forVariable(variable));
    }

    public QCycleDefinition(Path<? extends CycleDefinition> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCycleDefinition(PathMetadata metadata) {
        super(CycleDefinition.class, metadata);
    }

}

