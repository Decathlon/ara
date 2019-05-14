package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SCountry extends com.querydsl.sql.RelationalPathBase<SCountry> {

    private static final long serialVersionUID = -1188850036;

    public static final SCountry country = new SCountry("country");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SCountry> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SProject> countryProjectidFk = createForeignKey(projectId, "id");

    public final com.querydsl.sql.ForeignKey<SCountryDeployment> _countrydeploymentCountryidFk = createInvForeignKey(id, "country_id");

    public final com.querydsl.sql.ForeignKey<SProblemPattern> _problempatternCountryidFk = createInvForeignKey(id, "country_id");

    public final com.querydsl.sql.ForeignKey<SRun> _runCountryidFk = createInvForeignKey(id, "country_id");

    public SCountry(String variable) {
        super(SCountry.class, forVariable(variable), "null", "country");
        addMetadata();
    }

    public SCountry(String variable, String schema, String table) {
        super(SCountry.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SCountry(String variable, String schema) {
        super(SCountry.class, forVariable(variable), schema, "country");
        addMetadata();
    }

    public SCountry(Path<? extends SCountry> path) {
        super(path.getType(), path.getMetadata(), "null", "country");
        addMetadata();
    }

    public SCountry(PathMetadata metadata) {
        super(SCountry.class, metadata, "null", "country");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(1).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(50).notNull());
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

