package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

public class QSetting extends EntityPathBase<Setting> {

    private static final long serialVersionUID = -1138586041L;

    public static final QSetting setting = new QSetting("setting");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath value = createString("value");

    public QSetting(String variable) {
        super(Setting.class, forVariable(variable));
    }

    public QSetting(Path<? extends Setting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSetting(PathMetadata metadata) {
        super(Setting.class, metadata);
    }

}

