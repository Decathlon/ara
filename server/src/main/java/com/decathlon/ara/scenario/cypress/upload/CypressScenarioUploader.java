package com.decathlon.ara.scenario.cypress.upload;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.upload.ScenarioUploader;
import com.decathlon.ara.scenario.cucumber.bean.Feature;
import com.decathlon.ara.scenario.cucumber.util.ScenarioExtractorUtil;
import com.decathlon.ara.service.exception.BadRequestException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CypressScenarioUploader {

    @NonNull
    private final ScenarioUploader uploader;

    public void uploadScenarios(long projectId, String sourceCode, List<Feature> features) throws BadRequestException {
        uploader.processUploadedContent(
                projectId,
                sourceCode,
                Technology.CYPRESS,
                source -> ScenarioExtractorUtil.extractScenarios(source, features)
        );
    }
}
