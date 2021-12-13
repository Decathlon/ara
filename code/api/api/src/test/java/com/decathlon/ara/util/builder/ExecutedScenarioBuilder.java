package com.decathlon.ara.util.builder;

import java.util.Date;
import java.util.Set;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;

public class ExecutedScenarioBuilder extends GenericTestDataBuilder<ExecutedScenario> {

    public ExecutedScenarioBuilder withId(Long id) {
        setField("id", id);
        return this;
    }

    public ExecutedScenarioBuilder withRunId(Long runId) {
        setField("runId", runId);
        return this;
    }

    public ExecutedScenarioBuilder withFeatureFile(String featureFile) {
        builded.setFeatureFile(featureFile);
        return this;
    }

    public ExecutedScenarioBuilder withFeatureName(String featureName) {
        builded.setFeatureName(featureName);
        return this;
    }

    public ExecutedScenarioBuilder withFeatureTags(String featureTags) {
        builded.setFeatureTags(featureTags);
        return this;
    }

    public ExecutedScenarioBuilder withTags(String tags) {
        builded.setTags(tags);
        return this;
    }

    public ExecutedScenarioBuilder withSeverity(String severity) {
        builded.setSeverity(severity);
        return this;
    }

    public ExecutedScenarioBuilder withName(String name) {
        builded.setName(name);
        return this;
    }

    public ExecutedScenarioBuilder withCucumberId(String cucumberId) {
        builded.setCucumberId(cucumberId);
        return this;
    }

    public ExecutedScenarioBuilder withLine(int line) {
        builded.setLine(line);
        return this;
    }

    public ExecutedScenarioBuilder withContent(String content) {
        builded.setContent(content);
        return this;
    }

    public ExecutedScenarioBuilder withStartDateTime(Date startDateTime) {
        builded.setStartDateTime(startDateTime);
        return this;
    }

    public ExecutedScenarioBuilder withScreenshotUrl(String screenshotUrl) {
        builded.setScreenshotUrl(screenshotUrl);
        return this;
    }

    public ExecutedScenarioBuilder withVideoUrl(String videoUrl) {
        builded.setVideoUrl(videoUrl);
        return this;
    }

    public ExecutedScenarioBuilder withLogsUrl(String logsUrl) {
        builded.setLogsUrl(logsUrl);
        return this;
    }

    public ExecutedScenarioBuilder withHttpRequestsUrl(String httpRequestsUrl) {
        builded.setHttpRequestsUrl(httpRequestsUrl);
        return this;
    }

    public ExecutedScenarioBuilder withJavaScriptErrorsUrl(String javaScriptErrorsUrl) {
        builded.setJavaScriptErrorsUrl(javaScriptErrorsUrl);
        return this;
    }

    public ExecutedScenarioBuilder withDiffReportUrl(String diffReportUrl) {
        builded.setDiffReportUrl(diffReportUrl);
        return this;
    }

    public ExecutedScenarioBuilder withCucumberReportUrl(String cucumberReportUrl) {
        builded.setCucumberReportUrl(cucumberReportUrl);
        return this;
    }

    public ExecutedScenarioBuilder withApiServer(String apiServer) {
        builded.setApiServer(apiServer);
        return this;
    }

    public ExecutedScenarioBuilder withSeleniumNode(String seleniumNode) {
        builded.setSeleniumNode(seleniumNode);
        return this;
    }

    public ExecutedScenarioBuilder withErrors(Set<Error> errors) {
        setField("errors", errors);
        return this;
    }

}
