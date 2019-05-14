package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SExecutedScenario extends com.querydsl.sql.RelationalPathBase<SExecutedScenario> {

    private static final long serialVersionUID = -383449591;

    public static final SExecutedScenario executedScenario = new SExecutedScenario("executed_scenario");

    public final StringPath apiServer = createString("apiServer");

    public final StringPath content = createString("content");

    public final StringPath cucumberId = createString("cucumberId");

    public final StringPath cucumberReportUrl = createString("cucumberReportUrl");

    public final StringPath diffReportUrl = createString("diffReportUrl");

    public final StringPath featureFile = createString("featureFile");

    public final StringPath featureName = createString("featureName");

    public final StringPath featureTags = createString("featureTags");

    public final StringPath httpRequestsUrl = createString("httpRequestsUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath javaScriptErrorsUrl = createString("javaScriptErrorsUrl");

    public final NumberPath<Integer> line = createNumber("line", Integer.class);

    public final StringPath logsUrl = createString("logsUrl");

    public final StringPath name = createString("name");

    public final NumberPath<Long> runId = createNumber("runId", Long.class);

    public final StringPath screenshotUrl = createString("screenshotUrl");

    public final StringPath seleniumNode = createString("seleniumNode");

    public final StringPath severity = createString("severity");

    public final DateTimePath<java.sql.Timestamp> startDateTime = createDateTime("startDateTime", java.sql.Timestamp.class);

    public final StringPath tags = createString("tags");

    public final StringPath videoUrl = createString("videoUrl");

    public final com.querydsl.sql.PrimaryKey<SExecutedScenario> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SRun> executedscenarioRunidFk = createForeignKey(runId, "id");

    public final com.querydsl.sql.ForeignKey<SError> _errorExecutedscenarioidFk = createInvForeignKey(id, "executed_scenario_id");

    public SExecutedScenario(String variable) {
        super(SExecutedScenario.class, forVariable(variable), "null", "executed_scenario");
        addMetadata();
    }

    public SExecutedScenario(String variable, String schema, String table) {
        super(SExecutedScenario.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SExecutedScenario(String variable, String schema) {
        super(SExecutedScenario.class, forVariable(variable), schema, "executed_scenario");
        addMetadata();
    }

    public SExecutedScenario(Path<? extends SExecutedScenario> path) {
        super(path.getType(), path.getMetadata(), "null", "executed_scenario");
        addMetadata();
    }

    public SExecutedScenario(PathMetadata metadata) {
        super(SExecutedScenario.class, metadata, "null", "executed_scenario");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(apiServer, ColumnMetadata.named("api_server").withIndex(20).ofType(Types.VARCHAR).withSize(16));
        addMetadata(content, ColumnMetadata.named("content").withIndex(11).ofType(Types.LONGVARCHAR).withSize(2147483647).notNull());
        addMetadata(cucumberId, ColumnMetadata.named("cucumber_id").withIndex(10).ofType(Types.VARCHAR).withSize(640).notNull());
        addMetadata(cucumberReportUrl, ColumnMetadata.named("cucumber_report_url").withIndex(19).ofType(Types.VARCHAR).withSize(512));
        addMetadata(diffReportUrl, ColumnMetadata.named("diff_report_url").withIndex(18).ofType(Types.VARCHAR).withSize(512));
        addMetadata(featureFile, ColumnMetadata.named("feature_file").withIndex(3).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(featureName, ColumnMetadata.named("feature_name").withIndex(4).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(featureTags, ColumnMetadata.named("feature_tags").withIndex(5).ofType(Types.VARCHAR).withSize(256));
        addMetadata(httpRequestsUrl, ColumnMetadata.named("http_requests_url").withIndex(16).ofType(Types.VARCHAR).withSize(512));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(javaScriptErrorsUrl, ColumnMetadata.named("java_script_errors_url").withIndex(17).ofType(Types.VARCHAR).withSize(512));
        addMetadata(line, ColumnMetadata.named("line").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(logsUrl, ColumnMetadata.named("logs_url").withIndex(15).ofType(Types.VARCHAR).withSize(512));
        addMetadata(name, ColumnMetadata.named("name").withIndex(8).ofType(Types.VARCHAR).withSize(512).notNull());
        addMetadata(runId, ColumnMetadata.named("run_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(screenshotUrl, ColumnMetadata.named("screenshot_url").withIndex(13).ofType(Types.VARCHAR).withSize(512));
        addMetadata(seleniumNode, ColumnMetadata.named("selenium_node").withIndex(21).ofType(Types.VARCHAR).withSize(128));
        addMetadata(severity, ColumnMetadata.named("severity").withIndex(7).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(startDateTime, ColumnMetadata.named("start_date_time").withIndex(12).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(tags, ColumnMetadata.named("tags").withIndex(6).ofType(Types.VARCHAR).withSize(256));
        addMetadata(videoUrl, ColumnMetadata.named("video_url").withIndex(14).ofType(Types.VARCHAR).withSize(512));
    }

}

