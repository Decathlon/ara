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

package com.decathlon.ara.scenario.karate.indexer;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.decathlon.ara.scenario.karate.bean.KarateExecutedScenarioReport;
import com.decathlon.ara.scenario.karate.bean.description.KarateExecutedScenarioDescription;
import com.decathlon.ara.scenario.karate.bean.display.KarateExecutedScenarioResultsDisplay;
import com.decathlon.ara.scenario.karate.bean.error.KarateExecutedScenarioError;
import com.decathlon.ara.scenario.karate.bean.feature.KarateExecutedScenarioFeature;
import com.decathlon.ara.scenario.karate.bean.log.KarateExecutedScenarioLogs;
import com.decathlon.ara.scenario.karate.settings.KarateSettings;
import com.decathlon.ara.service.FileProcessorService;
import com.decathlon.ara.service.TechnologySettingService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class KarateScenariosIndexer implements ScenariosIndexer {

    private final TechnologySettingService technologySettingService;

    private final FileProcessorService fileProcessorService;

    public karateScenariosIndexer(TechnologySettingService technologySettingService,
                                   FileProcessorService fileProcessorService) {
        this.technologySettingService = technologySettingService;
        this.fileProcessorService = fileProcessorService;
    }

    @Override
    public List<ExecutedScenario> getExecutedScenarios(File parentFolder, Run run, Long projectId) {
        String reportsFolderPath = technologySettingService.getSettingValue(projectId, KarateSettings.REPORTS_LOCATION).orElse("");
        List<KarateExecutedScenarioReport> reports = fileProcessorService.getMappedObjectsFromDirectory(parentFolder, reportsFolderPath, karateExecutedScenarioReport.class);

        return CollectionUtils.isEmpty(reports) ? new ArrayList<>() : reports
                .stream()
                .map(report -> getExecutedScenarioFromkarateExecutedScenarioReport(report, run))
                .toList();
    }

    /**
     * Convert a {@link karateExecutedScenarioReport} into an {@link ExecutedScenario}
     * @param karateReport the karate report to convert
     * @param run the matching run
     * @return an executed scenario
     */
    private ExecutedScenario getExecutedScenarioFromkarateExecutedScenarioReport(karateExecutedScenarioReport karateReport, Run run) {
        ExecutedScenario executedScenario = new ExecutedScenario();
        executedScenario.setRun(run);

        if (karateReport != null) {
            executedScenario.setCucumberId(karateReport.getCode());
            executedScenario.setApiServer(karateReport.getServerName());
            executedScenario.setSeverity(karateReport.getSeverity());
            executedScenario.setStartDateTime(karateReport.getStartDate());
            executedScenario.setSeleniumNode(karateReport.getComment());
            executedScenario.setName(karateReport.getFunctionalitiesName());
            executedScenario.setTags(karateReport.getTagsAsString());

            List<karateExecutedScenarioError> karateErrors = karateReport.getErrors();
            if (!CollectionUtils.isEmpty(karateErrors)) {
                List<Error> errors = karateErrors.stream()
                        .map(this::getErrorFromkarateExecutedScenarioError)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
                executedScenario.addErrors(errors);
            }

            karateExecutedScenarioDescription description = karateReport.getDescription();
            if (description != null) {
                executedScenario.setContent(description.getStepsContent());
                executedScenario.setLine(description.getStartLineNumber());
            }

            karateExecutedScenarioResultsDisplay display = karateReport.getDisplay();
            if (display != null) {
                executedScenario.setVideoUrl(display.getVideoUrl());
                executedScenario.setScreenshotUrl(display.getScreenshotUrl());
                executedScenario.setCucumberReportUrl(display.getOtherResultsDisplayUrl());
            }

            karateExecutedScenarioFeature feature = karateReport.getFeature();
            if (feature != null) {
                executedScenario.setFeatureName(feature.getName());
                executedScenario.setFeatureFile(feature.getFileName());
                executedScenario.setFeatureTags(feature.getTagsAsString());
            }

            karateExecutedScenarioLogs logs = karateReport.getLogs();
            if (logs != null) {
                executedScenario.setLogsUrl(logs.getExecutionTraceUrl());
                executedScenario.setJavaScriptErrorsUrl(logs.getErrorStacktraceUrl());
                executedScenario.setHttpRequestsUrl(logs.getExecutedScenarioUrl());
                executedScenario.setDiffReportUrl(logs.getDiffReportUrl());
            }
        }

        return executedScenario;
    }

    /**
     * Convert an {@link Error} into a {@link karateExecutedScenarioError}
     * @param karateError the karate error
     * @return an error
     */
    private Optional<Error> getErrorFromkarateExecutedScenarioError(karateExecutedScenarioError karateError) {
        Error error = null;
        if (karateError != null) {
            error = new Error();
            Integer lineNumber = 0;
            if (karateError.getLineNumber() != null) {
                lineNumber = karateError.getLineNumber().intValue();
            }
            error.setStepLine(lineNumber);
            error.setStep(karateError.getCompleteLine());
            error.setStepDefinition(karateError.getRawLine());
            error.setException(karateError.getStackTrace());
        }
        return Optional.ofNullable(error);
    }
}
