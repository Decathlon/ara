package com.decathlon.ara.util.builder;

import java.util.Date;
import java.util.Set;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.domain.enumeration.JobStatus;

public class RunBuilder extends GenericTestDataBuilder<Run> {

    public RunBuilder withId(Long id) {
        builded.setId(id);
        return this;
    }

    public RunBuilder withExecution(Execution execution) {
        builded.setExecution(execution);
        return this;
    }

    public RunBuilder withCountry(Country country) {
        builded.setCountry(country);
        return this;
    }

    public RunBuilder withType(Type type) {
        builded.setType(type);
        return this;
    }

    public RunBuilder withComment(String comment) {
        builded.setComment(comment);
        return this;
    }

    public RunBuilder withPlatform(String platform) {
        builded.setPlatform(platform);
        return this;
    }

    public RunBuilder withJobUrl(String jobUrl) {
        builded.setJobUrl(jobUrl);
        return this;
    }

    public RunBuilder withJobLink(String jobLink) {
        builded.setJobLink(jobLink);
        return this;
    }

    public RunBuilder withStatus(JobStatus status) {
        builded.setStatus(status);
        return this;
    }

    public RunBuilder withCountryTags(String countryTags) {
        builded.setCountryTags(countryTags);
        return this;
    }

    public RunBuilder withStartDateTime(Date startDateTime) {
        builded.setStartDateTime(startDateTime);
        return this;
    }

    public RunBuilder withEstimatedDuration(Long estimatedDuration) {
        builded.setEstimatedDuration(estimatedDuration);
        return this;
    }

    public RunBuilder withDuration(Long duration) {
        builded.setDuration(duration);
        return this;
    }

    public RunBuilder withSeverityTags(String severityTags) {
        builded.setSeverityTags(severityTags);
        return this;
    }

    public RunBuilder withIncludeInThresholds(Boolean includeInThresholds) {
        builded.setIncludeInThresholds(includeInThresholds);
        return this;
    }

    public RunBuilder withExecutedScenarios(Set<ExecutedScenario> executedScenarios) {
        setField("executedScenarios", executedScenarios);
        return this;
    }

}
