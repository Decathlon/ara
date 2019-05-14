package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SScenario extends com.querydsl.sql.RelationalPathBase<SScenario> {

    private static final long serialVersionUID = 1396773722;

    public static final SScenario scenario = new SScenario("scenario");

    public final StringPath content = createString("content");

    public final StringPath countryCodes = createString("countryCodes");

    public final StringPath featureFile = createString("featureFile");

    public final StringPath featureName = createString("featureName");

    public final StringPath featureTags = createString("featureTags");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath ignored = createBoolean("ignored");

    public final NumberPath<Integer> line = createNumber("line", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath severity = createString("severity");

    public final NumberPath<Long> sourceId = createNumber("sourceId", Long.class);

    public final StringPath tags = createString("tags");

    public final StringPath wrongCountryCodes = createString("wrongCountryCodes");

    public final StringPath wrongFunctionalityIds = createString("wrongFunctionalityIds");

    public final StringPath wrongSeverityCode = createString("wrongSeverityCode");

    public final com.querydsl.sql.PrimaryKey<SScenario> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SSource> scenarioSourceidFk = createForeignKey(sourceId, "id");

    public final com.querydsl.sql.ForeignKey<SFunctionalityCoverage> _functionalitycoverageScenarioidFk = createInvForeignKey(id, "scenario_id");

    public SScenario(String variable) {
        super(SScenario.class, forVariable(variable), "null", "scenario");
        addMetadata();
    }

    public SScenario(String variable, String schema, String table) {
        super(SScenario.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SScenario(String variable, String schema) {
        super(SScenario.class, forVariable(variable), schema, "scenario");
        addMetadata();
    }

    public SScenario(Path<? extends SScenario> path) {
        super(path.getType(), path.getMetadata(), "null", "scenario");
        addMetadata();
    }

    public SScenario(PathMetadata metadata) {
        super(SScenario.class, metadata, "null", "scenario");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(content, ColumnMetadata.named("content").withIndex(11).ofType(Types.LONGVARCHAR).withSize(2147483647).notNull());
        addMetadata(countryCodes, ColumnMetadata.named("country_codes").withIndex(7).ofType(Types.VARCHAR).withSize(128));
        addMetadata(featureFile, ColumnMetadata.named("feature_file").withIndex(2).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(featureName, ColumnMetadata.named("feature_name").withIndex(3).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(featureTags, ColumnMetadata.named("feature_tags").withIndex(4).ofType(Types.VARCHAR).withSize(256));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(ignored, ColumnMetadata.named("ignored").withIndex(6).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(line, ColumnMetadata.named("line").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(9).ofType(Types.VARCHAR).withSize(512).notNull());
        addMetadata(severity, ColumnMetadata.named("severity").withIndex(8).ofType(Types.VARCHAR).withSize(32));
        addMetadata(sourceId, ColumnMetadata.named("source_id").withIndex(15).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(tags, ColumnMetadata.named("tags").withIndex(5).ofType(Types.VARCHAR).withSize(256));
        addMetadata(wrongCountryCodes, ColumnMetadata.named("wrong_country_codes").withIndex(13).ofType(Types.VARCHAR).withSize(128));
        addMetadata(wrongFunctionalityIds, ColumnMetadata.named("wrong_functionality_ids").withIndex(12).ofType(Types.VARCHAR).withSize(256));
        addMetadata(wrongSeverityCode, ColumnMetadata.named("wrong_severity_code").withIndex(14).ofType(Types.VARCHAR).withSize(32));
    }

}

