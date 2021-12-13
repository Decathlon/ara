package com.decathlon.ara.util.builder;

import java.util.Date;
import java.util.Set;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;

public class FunctionalityBuilder extends GenericTestDataBuilder<Functionality> {

    public FunctionalityBuilder withId(Long id) {
        builded.setId(id);
        return this;
    }

    public FunctionalityBuilder withProjectId(Long projectId) {
        builded.setProjectId(projectId);
        return this;
    }

    public FunctionalityBuilder withParentId(Long parentId) {
        builded.setParentId(parentId);
        return this;
    }

    public FunctionalityBuilder withOrder(Double order) {
        builded.setOrder(order);
        return this;
    }

    public FunctionalityBuilder withType(FunctionalityType type) {
        builded.setType(type);
        return this;
    }

    public FunctionalityBuilder withName(String name) {
        setField("name", name);
        return this;
    }

    public FunctionalityBuilder withCountryCodes(String countryCodes) {
        builded.setCountryCodes(countryCodes);
        return this;
    }

    public FunctionalityBuilder withTeamId(Long teamId) {
        builded.setTeamId(teamId);
        return this;
    }

    public FunctionalityBuilder withSeverity(FunctionalitySeverity severity) {
        setField("severity", severity);
        return this;
    }

    public FunctionalityBuilder withCreated(String created) {
        setField("created", created);
        return this;
    }

    public FunctionalityBuilder withStarted(Boolean started) {
        builded.setStarted(started);
        return this;
    }

    public FunctionalityBuilder withNotAutomatable(Boolean notAutomatable) {
        builded.setNotAutomatable(notAutomatable);
        return this;
    }

    public FunctionalityBuilder withCoveredScenarios(Integer coveredScenarios) {
        builded.setCoveredScenarios(coveredScenarios);
        return this;
    }

    public FunctionalityBuilder withCoveredCountryScenarios(String coveredCountryScenarios) {
        builded.setCoveredCountryScenarios(coveredCountryScenarios);
        return this;
    }

    public FunctionalityBuilder withIgnoredScenarios(Integer ignoredScenarios) {
        builded.setIgnoredScenarios(ignoredScenarios);
        return this;
    }

    public FunctionalityBuilder withIgnoredCountryScenarios(String ignoredCountryScenarios) {
        builded.setIgnoredCountryScenarios(ignoredCountryScenarios);
        return this;
    }

    public FunctionalityBuilder withComment(String comment) {
        setField("comment", comment);
        return this;
    }

    public FunctionalityBuilder withCreationDateTime(Date creationDateTime) {
        builded.setCreationDateTime(creationDateTime);
        return this;
    }

    public FunctionalityBuilder withUpdateDateTime(Date updateDateTime) {
        builded.setUpdateDateTime(updateDateTime);
        return this;
    }

    public FunctionalityBuilder withScenarios(Set<Scenario> scenarios) {
        setField("scenarios", scenarios);
        return this;
    }

}
