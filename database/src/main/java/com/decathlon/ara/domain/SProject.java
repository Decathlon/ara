package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SProject extends com.querydsl.sql.RelationalPathBase<SProject> {

    private static final long serialVersionUID = 1838975503;

    public static final SProject project = new SProject("project");

    public final StringPath code = createString("code");

    public final BooleanPath defaultAtStartup = createBoolean("defaultAtStartup");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SProject> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SSeverity> _severityProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<STeam> _teamProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SSource> _sourceProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SType> _typeProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SCommunication> _communicationProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SCountry> _countryProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SSetting> _settingProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SFunctionality> _functionalityProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SCycleDefinition> _cycledefinitionProjectidFk = createInvForeignKey(id, "project_id");

    public final com.querydsl.sql.ForeignKey<SRootCause> _rootcauseProjectidFk = createInvForeignKey(id, "project_id");

    public SProject(String variable) {
        super(SProject.class, forVariable(variable), "null", "project");
        addMetadata();
    }

    public SProject(String variable, String schema, String table) {
        super(SProject.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SProject(String variable, String schema) {
        super(SProject.class, forVariable(variable), schema, "project");
        addMetadata();
    }

    public SProject(Path<? extends SProject> path) {
        super(path.getType(), path.getMetadata(), "null", "project");
        addMetadata();
    }

    public SProject(PathMetadata metadata) {
        super(SProject.class, metadata, "null", "project");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(defaultAtStartup, ColumnMetadata.named("default_at_startup").withIndex(4).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(64).notNull());
    }

}

