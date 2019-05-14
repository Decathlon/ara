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

