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

package com.decathlon.ara.scenario.generic.indexer;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.decathlon.ara.scenario.generic.bean.GenericExecutedScenarioReport;
import com.decathlon.ara.scenario.generic.bean.description.GenericExecutedScenarioDescription;
import com.decathlon.ara.scenario.generic.bean.display.GenericExecutedScenarioResultsDisplay;
import com.decathlon.ara.scenario.generic.bean.error.GenericExecutedScenarioError;
import com.decathlon.ara.scenario.generic.bean.feature.GenericExecutedScenarioFeature;
import com.decathlon.ara.scenario.generic.bean.log.GenericExecutedScenarioLogs;
import com.decathlon.ara.scenario.generic.settings.GenericSettings;
import com.decathlon.ara.service.FileProcessorService;
import com.decathlon.ara.service.TechnologySettingService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenericScenariosIndexer implements ScenariosIndexer {

    @NonNull
    private final TechnologySettingService technologySettingService;

    @NonNull
    private final FileProcessorService fileProcessorService;

    @Override
    public List<ExecutedScenario> getExecutedScenarios(File parentFolder, Run run, Long projectId) {
        String reportsFolderPath = technologySettingService.getSettingValue(projectId, GenericSettings.REPORTS_LOCATION).orElse("");
        List<GenericExecutedScenarioReport> reports = fileProcessorService.getMappedObjectsFromDirectory(parentFolder, reportsFolderPath, GenericExecutedScenarioReport.class);

        return CollectionUtils.isEmpty(reports) ?
                new ArrayList<>() :
                reports
                        .stream()
                        .map(report -> getExecutedScenarioFromGenericExecutedScenarioReport(report, run))
                        .collect(Collectors.toList());
    }

    /**
     * Convert a {@link GenericExecutedScenarioReport} into an {@link ExecutedScenario}
     * @param genericReport the generic report to convert
     * @param run the matching run
     * @return an executed scenario
     */
    private ExecutedScenario getExecutedScenarioFromGenericExecutedScenarioReport(GenericExecutedScenarioReport genericReport, Run run) {
        ExecutedScenario executedScenario = new ExecutedScenario();
        executedScenario.setRun(run);

        if (genericReport != null) {
            executedScenario.setCucumberId(genericReport.getCode());
            executedScenario.setApiServer(genericReport.getServerName());
            executedScenario.setSeverity(genericReport.getSeverity());
            executedScenario.setStartDateTime(genericReport.getStartDate());
            executedScenario.setSeleniumNode(genericReport.getComment());

            String functionalitiesName = getFunctionalitiesName(genericReport.getCartography(), genericReport.getName());
            executedScenario.setName(functionalitiesName);

            String tags = convertTagsToString(genericReport.getTags());
            executedScenario.setTags(tags);

            GenericExecutedScenarioDescription description = genericReport.getDescription();

            List<GenericExecutedScenarioError> genericErrors = genericReport.getErrors();
            if (!CollectionUtils.isEmpty(genericErrors)) {
                List<Error> errors = genericErrors.stream()
                        .map(this::getErrorFromGenericExecutedScenarioError)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                executedScenario.addErrors(errors);
            }

            if (description != null) {
                executedScenario.setContent(description.getStepsContent());
                executedScenario.setLine(description.getStartLineNumber());
            }

            GenericExecutedScenarioResultsDisplay display = genericReport.getDisplay();
            if (display != null) {
                executedScenario.setVideoUrl(display.getVideoUrl());
                executedScenario.setScreenshotUrl(display.getScreenshotUrl());
                executedScenario.setCucumberReportUrl(display.getOtherResultsDisplayUrl());
            }

            GenericExecutedScenarioFeature feature = genericReport.getFeature();
            if (feature != null) {
                String featureTags = convertTagsToString(feature.getTags());
                executedScenario.setFeatureName(feature.getName());
                executedScenario.setFeatureFile(feature.getFileName());
                executedScenario.setFeatureTags(featureTags);
            }

            GenericExecutedScenarioLogs logs = genericReport.getLogs();
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
     * Convert an {@link Error} into a {@link GenericExecutedScenarioError}
     * @param genericError the generic error
     * @return an error
     */
    private Optional<Error> getErrorFromGenericExecutedScenarioError(GenericExecutedScenarioError genericError) {
        Error error = null;
        if (genericError != null) {
            error = new Error();
            Integer lineNumber = 0;
            if (genericError.getLineNumber() != null) {
                lineNumber = genericError.getLineNumber().intValue();
            }
            error.setStepLine(lineNumber);
            error.setStep(genericError.getCompleteLine());
            error.setStepDefinition(genericError.getRawLine());
            error.setException(genericError.getStackTrace());
        }
        return Optional.ofNullable(error);
    }

    /**
     * Convert tags into a string. If no tags, then return an empty string
     * @param tags the tags to convert
     * @return the string representation of the tags
     */
    private String convertTagsToString(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return "";
        }
        return tags
                .stream()
                .map(tag -> String.format("@%s", tag))
                .collect(Collectors.joining(" "));
    }

    /**
     * Get functionalities name
     * @param cartography the functionality ids
     * @param name the name
     * @return the functionalities name
     */
    private String getFunctionalitiesName(List<Long> cartography, String name) {
        String cartographyAsString = "";
        if (!CollectionUtils.isEmpty(cartography)) {
            String cartographyIdsAsString = cartography
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            cartographyAsString = String.format("Functionality %s: ", cartographyIdsAsString);
        }
        return String.format("%s%s", cartographyAsString, name);
    }
}
