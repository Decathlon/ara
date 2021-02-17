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
import com.decathlon.ara.scenario.generic.bean.GenericExecutedScenarioReport;
import com.decathlon.ara.scenario.generic.bean.description.GenericExecutedScenarioDescription;
import com.decathlon.ara.scenario.generic.bean.display.GenericExecutedScenarioResultsDisplay;
import com.decathlon.ara.scenario.generic.bean.error.GenericExecutedScenarioError;
import com.decathlon.ara.scenario.generic.bean.feature.GenericExecutedScenarioFeature;
import com.decathlon.ara.scenario.generic.bean.log.GenericExecutedScenarioLogs;
import com.decathlon.ara.scenario.generic.settings.GenericSettings;
import com.decathlon.ara.service.FileProcessorService;
import com.decathlon.ara.service.TechnologySettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenericScenariosIndexerTest {

    @Mock
    private TechnologySettingService technologySettingService;

    @Mock
    private FileProcessorService fileProcessorService;

    @InjectMocks
    private GenericScenariosIndexer genericScenariosIndexer;

    @Test
    void getExecutedScenarios_returnEmptyList_whenNoReportFound() {
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String reportsLocation = "/report/location";

        // When
        when(technologySettingService.getSettingValue(projectId, GenericSettings.REPORTS_LOCATION)).thenReturn(Optional.of(reportsLocation));
        when(fileProcessorService.getMappedObjectsFromDirectory(parentFolder, reportsLocation, GenericExecutedScenarioReport.class)).thenReturn(null);

        // Then
        List<ExecutedScenario> executedScenarios = genericScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getExecutedScenarios_returnExecutedScenarios_whenReportsFound() {
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String reportsLocation = "/report/location";

        GenericExecutedScenarioReport genericReport1 = mock(GenericExecutedScenarioReport.class);
        GenericExecutedScenarioDescription genericDescription1 = mock(GenericExecutedScenarioDescription.class);
        GenericExecutedScenarioResultsDisplay genericDisplay1 = mock(GenericExecutedScenarioResultsDisplay.class);
        GenericExecutedScenarioFeature genericFeature1 = mock(GenericExecutedScenarioFeature.class);
        GenericExecutedScenarioLogs genericLogs1 = mock(GenericExecutedScenarioLogs.class);

        GenericExecutedScenarioReport genericReport2 = mock(GenericExecutedScenarioReport.class);
        GenericExecutedScenarioDescription genericDescription2 = mock(GenericExecutedScenarioDescription.class);
        GenericExecutedScenarioResultsDisplay genericDisplay2 = mock(GenericExecutedScenarioResultsDisplay.class);
        GenericExecutedScenarioFeature genericFeature2 = mock(GenericExecutedScenarioFeature.class);
        GenericExecutedScenarioLogs genericLogs2 = mock(GenericExecutedScenarioLogs.class);

        GenericExecutedScenarioError genericError1 = mock(GenericExecutedScenarioError.class);
        GenericExecutedScenarioError genericError2 = mock(GenericExecutedScenarioError.class);
        GenericExecutedScenarioError genericError3 = mock(GenericExecutedScenarioError.class);

        Long runId = 10L;

        // When
        when(technologySettingService.getSettingValue(projectId, GenericSettings.REPORTS_LOCATION)).thenReturn(Optional.of(reportsLocation));
        when(fileProcessorService.getMappedObjectsFromDirectory(parentFolder, reportsLocation, GenericExecutedScenarioReport.class))
                .thenReturn(Arrays.asList(genericReport1, genericReport2));

        when(genericReport1.getCode()).thenReturn("feature_id1");
        when(genericReport1.getComment()).thenReturn("some comment");
        when(genericReport1.getName()).thenReturn("scenario_name1");
        when(genericReport1.getServerName()).thenReturn("server_name1");
        when(genericReport1.getSeverity()).thenReturn("sanity_check");
        when(genericReport1.getStartDate()).thenReturn(Date.from(LocalDateTime.of(2020, Month.MARCH, 3, 12, 5, 35, 10).atZone(ZoneId.systemDefault()).toInstant()));
        when(genericReport1.getTags()).thenReturn(Arrays.asList("tag11", "tag12", "tag13"));
        when(genericReport1.getDescription()).thenReturn(genericDescription1);
        when(genericReport1.getCartography()).thenReturn(Arrays.asList(35l, 56l, 28l));
        when(genericDescription1.getStepsContent()).thenReturn("content1");
        when(genericDescription1.getStartLineNumber()).thenReturn(12);
        when(genericReport1.getDisplay()).thenReturn(genericDisplay1);
        when(genericDisplay1.getScreenshotUrl()).thenReturn("http://your-company.com/screenshot1.png");
        when(genericDisplay1.getVideoUrl()).thenReturn("http://your-company.com/video1.mp4");
        when(genericDisplay1.getOtherResultsDisplayUrl()).thenReturn("http://your-company.com/result1.html");
        when(genericReport1.getFeature()).thenReturn(genericFeature1);
        when(genericFeature1.getName()).thenReturn("feature_name1");
        when(genericFeature1.getFileName()).thenReturn("feature_file1");
        when(genericFeature1.getTags()).thenReturn(Arrays.asList("feature_tag11", "feature_tag12", "feature_tag13"));
        when(genericReport1.getLogs()).thenReturn(genericLogs1);
        when(genericLogs1.getDiffReportUrl()).thenReturn("http://your-company.com/diff_report1");
        when(genericLogs1.getErrorStacktraceUrl()).thenReturn("http://your-company.com/stack-trace1");
        when(genericLogs1.getExecutedScenarioUrl()).thenReturn("http://your-company.com/scenario_log1");
        when(genericLogs1.getExecutionTraceUrl()).thenReturn("http://your-company.com/logs1");
        when(genericReport1.getErrors()).thenReturn(
                List.of(
                        genericError1,
                        genericError2,
                        genericError3
                )
        );
        when(genericError1.getLineNumber()).thenReturn(1L);
        when(genericError1.getStackTrace()).thenReturn("exception stack trace 1");
        when(genericError1.getRawLine()).thenReturn("^a line %s$");
        when(genericError1.getCompleteLine()).thenReturn("a line with some value");
        when(genericError2.getLineNumber()).thenReturn(2L);
        when(genericError2.getStackTrace()).thenReturn("exception stack trace 2");
        when(genericError2.getRawLine()).thenReturn("some value here: %d");
        when(genericError2.getCompleteLine()).thenReturn("another complete line <5>");
        when(genericError3.getLineNumber()).thenReturn(3L);
        when(genericError3.getStackTrace()).thenReturn("exception stack trace 3");
        when(genericError3.getRawLine()).thenReturn("^last line$");
        when(genericError3.getCompleteLine()).thenReturn("last line with some values: (, §, }");

        when(genericReport2.getCode()).thenReturn("feature_id2");
        when(genericReport2.getComment()).thenReturn("another comment");
        when(genericReport2.getName()).thenReturn("scenario_name2");
        when(genericReport2.getServerName()).thenReturn("server_name2");
        when(genericReport2.getSeverity()).thenReturn("high");
        when(genericReport2.getStartDate()).thenReturn(Date.from(LocalDateTime.of(2019, Month.JUNE, 5, 9, 8, 12, 12).atZone(ZoneId.systemDefault()).toInstant()));
        when(genericReport2.getTags()).thenReturn(Arrays.asList("tag21"));
        when(genericReport2.getDescription()).thenReturn(genericDescription2);
        when(genericReport2.getCartography()).thenReturn(null);
        when(genericDescription2.getStepsContent()).thenReturn("content2");
        when(genericDescription2.getStartLineNumber()).thenReturn(21);
        when(genericReport2.getDisplay()).thenReturn(genericDisplay2);
        when(genericDisplay2.getScreenshotUrl()).thenReturn("http://your-company.com/screenshot2.png");
        when(genericDisplay2.getVideoUrl()).thenReturn("http://your-company.com/video2.mp4");
        when(genericDisplay2.getOtherResultsDisplayUrl()).thenReturn("http://your-company.com/result2.html");
        when(genericReport2.getFeature()).thenReturn(genericFeature2);
        when(genericFeature2.getName()).thenReturn("feature_name2");
        when(genericFeature2.getFileName()).thenReturn("feature_file2");
        when(genericFeature2.getTags()).thenReturn(Arrays.asList("feature_tag21", "feature_tag22"));
        when(genericReport2.getLogs()).thenReturn(genericLogs2);
        when(genericLogs2.getDiffReportUrl()).thenReturn("http://your-company.com/diff_report2");
        when(genericLogs2.getErrorStacktraceUrl()).thenReturn("http://your-company.com/stack-trace2");
        when(genericLogs2.getExecutedScenarioUrl()).thenReturn("http://your-company.com/scenario_log2");
        when(genericLogs2.getExecutionTraceUrl()).thenReturn("http://your-company.com/logs2");
        when(genericReport2.getErrors()).thenReturn(null);

        when(run.getId()).thenReturn(runId);

        // Then
        List<ExecutedScenario> executedScenarios = genericScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .extracting(
                        "runId",
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "line",
                        "cucumberId",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                runId,
                                "feature_file1",
                                "feature_name1",
                                "@feature_tag11 @feature_tag12 @feature_tag13",
                                "@tag11 @tag12 @tag13",
                                "sanity_check",
                                "Functionality 35, 56, 28: scenario_name1",
                                12,
                                "feature_id1",
                                "content1",
                                Date.from(LocalDateTime.of(2020, Month.MARCH, 3, 12, 5, 35, 10).atZone(ZoneId.systemDefault()).toInstant()),
                                "http://your-company.com/screenshot1.png",
                                "http://your-company.com/video1.mp4",
                                "http://your-company.com/logs1",
                                "http://your-company.com/scenario_log1",
                                "http://your-company.com/stack-trace1",
                                "http://your-company.com/diff_report1",
                                "http://your-company.com/result1.html",
                                "server_name1",
                                "some comment"
                        ),
                        tuple(
                                runId,
                                "feature_file2",
                                "feature_name2",
                                "@feature_tag21 @feature_tag22",
                                "@tag21",
                                "high",
                                "scenario_name2",
                                21,
                                "feature_id2",
                                "content2",
                                Date.from(LocalDateTime.of(2019, Month.JUNE, 5, 9, 8, 12, 12).atZone(ZoneId.systemDefault()).toInstant()),
                                "http://your-company.com/screenshot2.png",
                                "http://your-company.com/video2.mp4",
                                "http://your-company.com/logs2",
                                "http://your-company.com/scenario_log2",
                                "http://your-company.com/stack-trace2",
                                "http://your-company.com/diff_report2",
                                "http://your-company.com/result2.html",
                                "server_name2",
                                "another comment"
                        )
                );
        Set<Error> scenario1Errors = executedScenarios.get(0).getErrors();
        assertThat(scenario1Errors)
                .hasSize(3)
                .extracting(
                        "step",
                        "stepDefinition",
                        "exception",
                        "stepLine"
                )
                .containsExactly(
                        tuple(
                                "a line with some value",
                                "^a line %s$",
                                "exception stack trace 1",
                                1
                        ),
                        tuple(
                                "another complete line <5>",
                                "some value here: %d",
                                "exception stack trace 2",
                                2
                        ),
                        tuple(
                                "last line with some values: (, §, }",
                                "^last line$",
                                "exception stack trace 3",
                                3
                        )
                );

        Set<Error> scenario2Errors = executedScenarios.get(1).getErrors();
        assertThat(scenario2Errors).isNotNull().isEmpty();
    }
}
