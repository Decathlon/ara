package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

public class QTeam extends EntityPathBase<Team> {

    private static final long serialVersionUID = 1069587910L;

    public static final QTeam team = new QTeam("team");

    public final BooleanPath assignableToFunctionalities = createBoolean("assignableToFunctionalities");

    public final BooleanPath assignableToProblems = createBoolean("assignableToProblems");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final ListPath<Problem, QProblem> problems = this.<Problem, QProblem>createList("problems", Problem.class, QProblem.class, PathInits.DIRECT2);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public QTeam(String variable) {
        super(Team.class, forVariable(variable));
    }

    public QTeam(Path<? extends Team> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTeam(PathMetadata metadata) {
        super(Team.class, metadata);
    }

}

