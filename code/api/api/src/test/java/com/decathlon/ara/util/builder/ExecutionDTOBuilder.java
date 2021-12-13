package com.decathlon.ara.util.builder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;

public class ExecutionDTOBuilder extends GenericTestDataBuilder<ExecutionDTO> {

    public ExecutionDTOBuilder withId(Long id) {
        setField("id", id);
        return this;
    }

    public ExecutionDTOBuilder withBranch(String branch) {
        setField("branch", branch);
        return this;
    }

    public ExecutionDTOBuilder withName(String name) {
        setField("name", name);
        return this;
    }

    public ExecutionDTOBuilder withRelease(String release) {
        setField("release", release);
        return this;
    }

    public ExecutionDTOBuilder withQualityStatus(QualityStatus qualityStatus) {
        setField("qualityStatus", qualityStatus);
        return this;
    }

    public ExecutionDTOBuilder withBuildDateTime(Date buildDateTime) {
        setField("buildDateTime", buildDateTime);
        return this;
    }

    public ExecutionDTOBuilder withTestDateTime(Date testDateTime) {
        setField("testDateTime", testDateTime);
        return this;
    }

    public ExecutionDTOBuilder withQualitySeverities(List<QualitySeverityDTO> qualitySeverities) {
        setField("qualitySeverities", qualitySeverities);
        return this;
    }

    public ExecutionDTOBuilder withBlockingValidation(boolean blockingValidation) {
        setField("blockingValidation", blockingValidation);
        return this;
    }

    public ExecutionDTOBuilder withQualityThresholds(Map<String, QualityThreshold> qualityThresholds) {
        setField("qualityThresholds", qualityThresholds);
        return this;
    }
}
