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
import com.querydsl.core.types.dsl.PathInits;

public class QCommunication extends EntityPathBase<Communication> {

    private static final long serialVersionUID = -1420412275L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCommunication communication = new QCommunication("communication");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath message = createString("message");

    public final StringPath name = createString("name");

    public final QProject project;

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final EnumPath<com.decathlon.ara.domain.enumeration.CommunicationType> type = createEnum("type", com.decathlon.ara.domain.enumeration.CommunicationType.class);

    public QCommunication(String variable) {
        this(Communication.class, forVariable(variable), INITS);
    }

    public QCommunication(Path<? extends Communication> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCommunication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCommunication(PathMetadata metadata, PathInits inits) {
        this(Communication.class, metadata, inits);
    }

    public QCommunication(Class<? extends Communication> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.project = inits.isInitialized("project") ? new QProject(forProperty("project")) : null;
    }

}

