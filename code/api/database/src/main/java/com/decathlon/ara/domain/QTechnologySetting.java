/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

import com.decathlon.ara.domain.enumeration.Technology;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QTechnologySetting extends EntityPathBase<TechnologySetting> {

    public static final QTechnologySetting technologySetting = new QTechnologySetting("technologySetting");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath value = createString("value");

    public final EnumPath<Technology> technology = createEnum("technology", Technology.class);

    public QTechnologySetting(String variable) {
        super(TechnologySetting.class, forVariable(variable));
    }

    public QTechnologySetting(Path<? extends TechnologySetting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTechnologySetting(PathMetadata metadata) {
        super(TechnologySetting.class, metadata);
    }
}
