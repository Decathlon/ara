package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SSource extends com.querydsl.sql.RelationalPathBase<SSource> {

    private static final long serialVersionUID = -1658490267;

    public static final SSource source = new SSource("source");

    public final StringPath code = createString("code");

    public final StringPath defaultBranch = createString("defaultBranch");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath letter = createString("letter");

    public final StringPath name = createString("name");

    public final BooleanPath postmanCountryRootFolders = createBoolean("postmanCountryRootFolders");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath technology = createString("technology");

    public final StringPath vcsUrl = createString("vcsUrl");

    public final com.querydsl.sql.PrimaryKey<SSource> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> sourceProjectidFk = createForeignKey(projectId, "id");

    public final com.querydsl.sql.ForeignKey<SScenario> _scenarioSourceidFk = createInvForeignKey(id, "source_id");

    public final com.querydsl.sql.ForeignKey<SType> _typeSourceidFk = createInvForeignKey(id, "source_id");

    public SSource(String variable) {
        super(SSource.class, forVariable(variable), "null", "source");
        addMetadata();
    }

    public SSource(String variable, String schema, String table) {
        super(SSource.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SSource(String variable, String schema) {
        super(SSource.class, forVariable(variable), schema, "source");
        addMetadata();
    }

    public SSource(Path<? extends SSource> path) {
        super(path.getType(), path.getMetadata(), "null", "source");
        addMetadata();
    }

    public SSource(PathMetadata metadata) {
        super(SSource.class, metadata, "null", "source");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(1).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(defaultBranch, ColumnMetadata.named("default_branch").withIndex(6).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(8).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(letter, ColumnMetadata.named("letter").withIndex(3).ofType(Types.CHAR).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(postmanCountryRootFolders, ColumnMetadata.named("postman_country_root_folders").withIndex(7).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(technology, ColumnMetadata.named("technology").withIndex(4).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(vcsUrl, ColumnMetadata.named("vcs_url").withIndex(5).ofType(Types.VARCHAR).withSize(256).notNull());
    }

}

