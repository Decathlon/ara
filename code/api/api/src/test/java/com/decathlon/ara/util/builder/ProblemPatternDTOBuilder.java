package com.decathlon.ara.util.builder;

import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;

public class ProblemPatternDTOBuilder extends GenericTestDataBuilder<ProblemPatternDTO> {

    public ProblemPatternDTOBuilder withId(Long id) {
        builded.setId(id);
        return this;
    }

    public ProblemPatternDTOBuilder withFeatureFile(String featureFile) {
        setField("featureFile", featureFile);
        return this;
    }

    public ProblemPatternDTOBuilder withFeatureName(String featureName) {
        setField("featureName", featureName);
        return this;
    }

    public ProblemPatternDTOBuilder withScenarioName(String scenarioName) {
        setField("scenarioName", scenarioName);
        return this;
    }

    public ProblemPatternDTOBuilder withScenarioNameStartsWith(boolean scenarioNameStartsWith) {
        setField("scenarioNameStartsWith", scenarioNameStartsWith);
        return this;
    }

    public ProblemPatternDTOBuilder withStep(String step) {
        setField("step", step);
        return this;
    }

    public ProblemPatternDTOBuilder withStepStartsWith(boolean stepStartsWith) {
        setField("stepStartsWith", stepStartsWith);
        return this;
    }

    public ProblemPatternDTOBuilder withStepDefinition(String stepDefinition) {
        setField("stepDefinition", stepDefinition);
        return this;
    }

    public ProblemPatternDTOBuilder withStepDefinitionStartsWith(boolean stepDefinitionStartsWith) {
        setField("stepDefinitionStartsWith", stepDefinitionStartsWith);
        return this;
    }

    public ProblemPatternDTOBuilder withException(String exception) {
        setField("exception", exception);
        return this;
    }

    public ProblemPatternDTOBuilder withRelease(String release) {
        setField("release", release);
        return this;
    }

    public ProblemPatternDTOBuilder withCountry(CountryDTO country) {
        setField("country", country);
        return this;
    }

    public ProblemPatternDTOBuilder withType(TypeWithSourceDTO type) {
        setField("type", type);
        return this;
    }

    public ProblemPatternDTOBuilder withTypeIsBrowser(Boolean typeIsBrowser) {
        setField("typeIsBrowser", typeIsBrowser);
        return this;
    }

    public ProblemPatternDTOBuilder withTypeIsMobile(Boolean typeIsMobile) {
        setField("typeIsMobile", typeIsMobile);
        return this;
    }

    public ProblemPatternDTOBuilder withPlatform(String platform) {
        setField("platform", platform);
        return this;
    }

}
