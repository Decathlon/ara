package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SFunctionality extends com.querydsl.sql.RelationalPathBase<SFunctionality> {

    private static final long serialVersionUID = 1495497281;

    public static final SFunctionality functionality = new SFunctionality("functionality");

    public final StringPath comment = createString("comment");

    public final StringPath countryCodes = createString("countryCodes");

    public final StringPath coveredCountryScenarios = createString("coveredCountryScenarios");

    public final NumberPath<Integer> coveredScenarios = createNumber("coveredScenarios", Integer.class);

    public final StringPath created = createString("created");

    public final DateTimePath<java.sql.Timestamp> creationDateTime = createDateTime("creationDateTime", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ignoredCountryScenarios = createString("ignoredCountryScenarios");

    public final NumberPath<Integer> ignoredScenarios = createNumber("ignoredScenarios", Integer.class);

    public final StringPath name = createString("name");

    public final BooleanPath notAutomatable = createBoolean("notAutomatable");

    public final NumberPath<Double> order = createNumber("order", Double.class);

    public final NumberPath<Long> parentId = createNumber("parentId", Long.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath severity = createString("severity");

    public final BooleanPath started = createBoolean("started");

    public final NumberPath<Long> teamId = createNumber("teamId", Long.class);

    public final StringPath type = createString("type");

    public final DateTimePath<java.sql.Timestamp> updateDateTime = createDateTime("updateDateTime", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SFunctionality> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SFunctionality> functionalityParentidFk = createForeignKey(parentId, "id");

    public final com.querydsl.sql.ForeignKey<STeam> functionalityTeamidFk = createForeignKey(teamId, "id");

    public final com.querydsl.sql.ForeignKey<SProject> functionalityProjectidFk = createForeignKey(projectId, "id");

    public final com.querydsl.sql.ForeignKey<SFunctionalityCoverage> _functionalitycoverageFunctionalityidFk = createInvForeignKey(id, "functionality_id");

    public final com.querydsl.sql.ForeignKey<SFunctionality> _functionalityParentidFk = createInvForeignKey(id, "parent_id");

    public SFunctionality(String variable) {
        super(SFunctionality.class, forVariable(variable), "null", "functionality");
        addMetadata();
    }

    public SFunctionality(String variable, String schema, String table) {
        super(SFunctionality.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SFunctionality(String variable, String schema) {
        super(SFunctionality.class, forVariable(variable), schema, "functionality");
        addMetadata();
    }

    public SFunctionality(Path<? extends SFunctionality> path) {
        super(path.getType(), path.getMetadata(), "null", "functionality");
        addMetadata();
    }

    public SFunctionality(PathMetadata metadata) {
        super(SFunctionality.class, metadata, "null", "functionality");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(comment, ColumnMetadata.named("comment").withIndex(13).ofType(Types.LONGVARCHAR).withSize(2147483647));
        addMetadata(countryCodes, ColumnMetadata.named("country_codes").withIndex(6).ofType(Types.VARCHAR).withSize(128));
        addMetadata(coveredCountryScenarios, ColumnMetadata.named("covered_country_scenarios").withIndex(14).ofType(Types.VARCHAR).withSize(512));
        addMetadata(coveredScenarios, ColumnMetadata.named("covered_scenarios").withIndex(11).ofType(Types.INTEGER).withSize(10));
        addMetadata(created, ColumnMetadata.named("created").withIndex(9).ofType(Types.VARCHAR).withSize(10));
        addMetadata(creationDateTime, ColumnMetadata.named("creation_date_time").withIndex(18).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(ignoredCountryScenarios, ColumnMetadata.named("ignored_country_scenarios").withIndex(15).ofType(Types.VARCHAR).withSize(512));
        addMetadata(ignoredScenarios, ColumnMetadata.named("ignored_scenarios").withIndex(12).ofType(Types.INTEGER).withSize(10));
        addMetadata(name, ColumnMetadata.named("name").withIndex(5).ofType(Types.VARCHAR).withSize(512).notNull());
        addMetadata(notAutomatable, ColumnMetadata.named("not_automatable").withIndex(16).ofType(Types.BIT).withSize(1));
        addMetadata(order, ColumnMetadata.named("order").withIndex(3).ofType(Types.DOUBLE).withSize(22).notNull());
        addMetadata(parentId, ColumnMetadata.named("parent_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(17).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(severity, ColumnMetadata.named("severity").withIndex(8).ofType(Types.VARCHAR).withSize(32));
        addMetadata(started, ColumnMetadata.named("started").withIndex(10).ofType(Types.BIT).withSize(1));
        addMetadata(teamId, ColumnMetadata.named("team_id").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(type, ColumnMetadata.named("type").withIndex(4).ofType(Types.VARCHAR).withSize(13).notNull());
        addMetadata(updateDateTime, ColumnMetadata.named("update_date_time").withIndex(19).ofType(Types.TIMESTAMP).withSize(19).notNull());
    }

}

