package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QProject extends EntityPathBase<Project> {

    private static final long serialVersionUID = 861129488L;

    public static final QProject project = new QProject("project");

    public final StringPath code = createString("code");

    public final ListPath<Communication, QCommunication> communications = this.<Communication, QCommunication>createList("communications", Communication.class, QCommunication.class, PathInits.DIRECT2);

    public final BooleanPath defaultAtStartup = createBoolean("defaultAtStartup");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QProject(String variable) {
        super(Project.class, forVariable(variable));
    }

    public QProject(Path<? extends Project> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProject(PathMetadata metadata) {
        super(Project.class, metadata);
    }

}

