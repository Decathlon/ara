/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.scenario.cucumber.indexer;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.decathlon.ara.scenario.cucumber.bean.Feature;
import com.decathlon.ara.scenario.cucumber.service.ExecutedScenarioExtractorService;
import com.decathlon.ara.scenario.cucumber.settings.CucumberSettings;
import com.decathlon.ara.service.FileProcessorService;
import com.decathlon.ara.service.TechnologySettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CucumberScenariosIndexer implements ScenariosIndexer {

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final ExecutedScenarioExtractorService executedScenarioExtractorService;

    @NonNull
    private final TechnologySettingService technologySettingService;

    @NonNull
    private final FileProcessorService fileProcessorService;

    /**
     * Get the Cucumber executed scenarios
     * @param cucumberFolder the Cucumber report folder
     * @param run the run
     * @param projectId the project id
     * @return the Cucumber executed scenarios
     */
    @Override
    public List<ExecutedScenario> getExecutedScenarios(File cucumberFolder, Run run, Long projectId) {
        List<Feature> features = new ArrayList<>();
        List<String> stepDefinitions = new ArrayList<>();

        String reportFileName = technologySettingService.getSettingValue(projectId, CucumberSettings.REPORT_PATH).orElse("");
        Optional<File> cucumberReportFile = fileProcessorService.getMatchingSimpleFile(cucumberFolder, reportFileName);
        if (cucumberReportFile.isPresent()) {
            features = getCucumberFeaturesFromReport(cucumberReportFile.get());
        }

        String stepDefinitionsFileName = technologySettingService.getSettingValue(projectId, CucumberSettings.STEP_DEFINITIONS_PATH).orElse("");

        Optional<File> stepDefinitionsFile = fileProcessorService.getMatchingSimpleFile(cucumberFolder, stepDefinitionsFileName);
        if (stepDefinitionsFile.isPresent()) {
            stepDefinitions = getCucumberStepDefinitions(stepDefinitionsFile.get());
        }

        List<ExecutedScenario> executedScenarios = executedScenarioExtractorService.extractExecutedScenarios(
                features,
                stepDefinitions,
                run.getJobUrl()
        );

        return executedScenarios;
    }

    /**
     * Get the features from the Cucumber report file
     * @param cucumberReport the Cucumber report file
     * @return the Cucumber features
     */
    public List<Feature> getCucumberFeaturesFromReport(File cucumberReport) {
        try (InputStream input = new FileInputStream(cucumberReport)) {
            return objectMapper.readValue(input, objectMapper.getTypeFactory().constructCollectionType(List.class, Feature.class));
        } catch (IOException e) {
            log.info("Cannot download report file in {}", cucumberReport.getPath(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get Cucumber step definitions from the step definitions file
     * @param stepDefinitionsFile the file defining the Cucumber step definitions
     * @return all the extracted Cucumber step definitions
     */
    public List<String> getCucumberStepDefinitions(File stepDefinitionsFile) {
        try (InputStream input = new FileInputStream(stepDefinitionsFile)) {
            return objectMapper.readValue(input, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            log.info("Cannot download the step definitions file in {}", stepDefinitionsFile.getPath(), e);
            log.info("Please check your (technology) settings again");
            return new ArrayList<>();
        }
    }
}
