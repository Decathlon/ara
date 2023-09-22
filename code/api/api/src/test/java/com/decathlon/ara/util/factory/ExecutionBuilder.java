package com.decathlon.ara.util.factory;

import java.util.Date;
import java.util.Set;

import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.util.builder.GenericTestDataBuilder;

public class ExecutionBuilder extends GenericTestDataBuilder<Execution> {

    public ExecutionBuilder withId(Long id) {
        builded.setId(id);
        return this;
    }

    public ExecutionBuilder withBranch(String branch) {
        builded.setBranch(branch);
        return this;
    }

    public ExecutionBuilder withName(String name) {
        builded.setName(name);
        return this;
    }

    public ExecutionBuilder withRelease(String release) {
        builded.setRelease(release);
        return this;
    }

    public ExecutionBuilder withVersion(String version) {
        builded.setVersion(version);
        return this;
    }

    public ExecutionBuilder withBuildDateTime(Date buildDateTime) {
        builded.setBuildDateTime(buildDateTime);
        return this;
    }

    public ExecutionBuilder withTestDateTime(Date testDateTime) {
        builded.setTestDateTime(testDateTime);
        return this;
    }

    public ExecutionBuilder withJobUrl(String jobUrl) {
        builded.setJobUrl(jobUrl);
        return this;
    }

    public ExecutionBuilder withJobLink(String jobLink) {
        builded.setJobLink(jobLink);
        return this;
    }

    public ExecutionBuilder withStatus(JobStatus status) {
        builded.setStatus(status);
        return this;
    }

    public ExecutionBuilder withResult(Result result) {
        builded.setResult(result);
        return this;
    }

    public ExecutionBuilder withAcceptance(ExecutionAcceptance acceptance) {
        builded.setAcceptance(acceptance);
        return this;
    }

    public ExecutionBuilder withDiscardReason(String discardReason) {
        builded.setDiscardReason(discardReason);
        return this;
    }

    public ExecutionBuilder withCycleDefinition(CycleDefinition cycleDefinition) {
        builded.setCycleDefinition(cycleDefinition);
        return this;
    }

    public ExecutionBuilder withBlockingValidation(Boolean blockingValidation) {
        builded.setBlockingValidation(blockingValidation);
        return this;
    }

    public ExecutionBuilder withQualityThresholds(String qualityThresholds) {
        builded.setQualityThresholds(qualityThresholds);
        return this;
    }

    public ExecutionBuilder withQualityStatus(QualityStatus qualityStatus) {
        builded.setQualityStatus(qualityStatus);
        return this;
    }

    public ExecutionBuilder withQualitySeverities(String qualitySeverities) {
        builded.setQualitySeverities(qualitySeverities);
        return this;
    }

    public ExecutionBuilder withDuration(Long duration) {
        builded.setDuration(duration);
        return this;
    }

    public ExecutionBuilder withEstimatedDuration(Long estimatedDuration) {
        builded.setEstimatedDuration(estimatedDuration);
        return this;
    }

    public ExecutionBuilder withRuns(Set<Run> runs) {
        setField("runs", runs);
        return this;
    }

    public ExecutionBuilder withCountryDeployments(Set<CountryDeployment> countryDeployments) {
        setField("countryDeployments", countryDeployments);
        return this;
    }

}
