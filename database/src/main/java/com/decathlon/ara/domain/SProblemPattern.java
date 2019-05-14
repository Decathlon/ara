package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SProblemPattern extends com.querydsl.sql.RelationalPathBase<SProblemPattern> {

    private static final long serialVersionUID = 1335132219;

    public static final SProblemPattern problemPattern = new SProblemPattern("problem_pattern");

    public final NumberPath<Long> countryId = createNumber("countryId", Long.class);

    public final StringPath exception = createString("exception");

    public final StringPath featureFile = createString("featureFile");

    public final StringPath featureName = createString("featureName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath platform = createString("platform");

    public final NumberPath<Long> problemId = createNumber("problemId", Long.class);

    public final StringPath release = createString("release");

    public final StringPath scenarioName = createString("scenarioName");

    public final BooleanPath scenarioNameStartsWith = createBoolean("scenarioNameStartsWith");

    public final StringPath step = createString("step");

    public final StringPath stepDefinition = createString("stepDefinition");

    public final BooleanPath stepDefinitionStartsWith = createBoolean("stepDefinitionStartsWith");

    public final BooleanPath stepStartsWith = createBoolean("stepStartsWith");

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final BooleanPath typeIsBrowser = createBoolean("typeIsBrowser");

    public final BooleanPath typeIsMobile = createBoolean("typeIsMobile");

    public final com.querydsl.sql.PrimaryKey<SProblemPattern> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SType> problempatternTypeidFk = createForeignKey(typeId, "id");

    public final com.querydsl.sql.ForeignKey<SCountry> problempatternCountryidFk = createForeignKey(countryId, "id");

    public final com.querydsl.sql.ForeignKey<SProblem> problempatternProblemidFk = createForeignKey(problemId, "id");

    public final com.querydsl.sql.ForeignKey<SProblemOccurrence> _problemoccurrenceProblempatternidFk = createInvForeignKey(id, "problem_pattern_id");

    public SProblemPattern(String variable) {
        super(SProblemPattern.class, forVariable(variable), "null", "problem_pattern");
        addMetadata();
    }

    public SProblemPattern(String variable, String schema, String table) {
        super(SProblemPattern.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SProblemPattern(String variable, String schema) {
        super(SProblemPattern.class, forVariable(variable), schema, "problem_pattern");
        addMetadata();
    }

    public SProblemPattern(Path<? extends SProblemPattern> path) {
        super(path.getType(), path.getMetadata(), "null", "problem_pattern");
        addMetadata();
    }

    public SProblemPattern(PathMetadata metadata) {
        super(SProblemPattern.class, metadata, "null", "problem_pattern");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(countryId, ColumnMetadata.named("country_id").withIndex(14).ofType(Types.BIGINT).withSize(19));
        addMetadata(exception, ColumnMetadata.named("exception").withIndex(8).ofType(Types.LONGVARCHAR).withSize(2147483647));
        addMetadata(featureFile, ColumnMetadata.named("feature_file").withIndex(3).ofType(Types.VARCHAR).withSize(256));
        addMetadata(featureName, ColumnMetadata.named("feature_name").withIndex(4).ofType(Types.VARCHAR).withSize(256));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(platform, ColumnMetadata.named("platform").withIndex(12).ofType(Types.VARCHAR).withSize(32));
        addMetadata(problemId, ColumnMetadata.named("problem_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(release, ColumnMetadata.named("release").withIndex(9).ofType(Types.VARCHAR).withSize(32));
        addMetadata(scenarioName, ColumnMetadata.named("scenario_name").withIndex(5).ofType(Types.VARCHAR).withSize(512));
        addMetadata(scenarioNameStartsWith, ColumnMetadata.named("scenario_name_starts_with").withIndex(15).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(step, ColumnMetadata.named("step").withIndex(6).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(stepDefinition, ColumnMetadata.named("step_definition").withIndex(7).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(stepDefinitionStartsWith, ColumnMetadata.named("step_definition_starts_with").withIndex(17).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(stepStartsWith, ColumnMetadata.named("step_starts_with").withIndex(16).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(typeId, ColumnMetadata.named("type_id").withIndex(13).ofType(Types.BIGINT).withSize(19));
        addMetadata(typeIsBrowser, ColumnMetadata.named("type_is_browser").withIndex(10).ofType(Types.BIT).withSize(1));
        addMetadata(typeIsMobile, ColumnMetadata.named("type_is_mobile").withIndex(11).ofType(Types.BIT).withSize(1));
    }

}

