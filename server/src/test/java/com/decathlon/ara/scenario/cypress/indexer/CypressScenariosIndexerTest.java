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

package com.decathlon.ara.scenario.cypress.indexer;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.scenario.cucumber.bean.Feature;
import com.decathlon.ara.scenario.cucumber.indexer.CucumberScenariosIndexer;
import com.decathlon.ara.scenario.cucumber.service.ExecutedScenarioExtractorService;
import com.decathlon.ara.scenario.cypress.settings.CypressSettings;
import com.decathlon.ara.service.FileProcessorService;
import com.decathlon.ara.service.TechnologySettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CypressScenariosIndexerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TechnologySettingService technologySettingService;

    @Mock
    private FileProcessorService fileProcessorService;

    @Mock
    private CucumberScenariosIndexer cucumberScenariosIndexer;

    @Mock
    private ExecutedScenarioExtractorService executedScenarioExtractorService;

    @InjectMocks
    private CypressScenariosIndexer cypressScenariosIndexer;

    @Test
    public void getExecutedScenarios_returnEmptyList_whenNoCucumberFolderFound(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.empty());

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios).isEmpty();
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(1)).getMatchingDirectory(any(File.class), anyString());
        verify(cucumberScenariosIndexer, never()).getCucumberFeaturesFromReport(any(File.class));
        verify(cucumberScenariosIndexer, never()).getCucumberStepDefinitions(any(File.class));
        verify(executedScenarioExtractorService, never()).extractExecutedScenarios(anyList(), anyList(), anyString());
    }

    @Test
    public void getExecutedScenarios_returnEmptyList_whenCucumberFolderIsEmpty(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        File cucumberFolder = mock(File.class);

        String cucumberSuffix = "cucumber-suffix";

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.of(cucumberFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(cucumberSuffix));
        when(cucumberFolder.listFiles()).thenReturn(new File[0]);

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios).isEmpty();
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(1)).getMatchingDirectory(any(File.class), anyString());
        verify(cucumberScenariosIndexer, never()).getCucumberFeaturesFromReport(any(File.class));
        verify(cucumberScenariosIndexer, never()).getCucumberStepDefinitions(any(File.class));
        verify(executedScenarioExtractorService, never()).extractExecutedScenarios(anyList(), anyList(), anyString());
    }

    @Test
    public void getExecutedScenarios_returnEmptyList_whenCucumberFolderHasNoReportMatchingCucumberSettingSuffix(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        File cucumberFolder = mock(File.class);

        String cucumberSuffix = "cucumber-suffix";

        File cucumberReportFile1 = mock(File.class);
        String cucumberReportFileName1 = "report1.not-a-cucumber-suffix.json";
        File cucumberReportFile2 = mock(File.class);
        String cucumberReportFileName2 = "report2.not-a-cucumber-suffix.json";

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.of(cucumberFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(cucumberSuffix));
        when(cucumberFolder.listFiles()).thenReturn(new File[] {cucumberReportFile1, cucumberReportFile2});
        when(cucumberReportFile1.isFile()).thenReturn(true);
        when(cucumberReportFile1.getName()).thenReturn(cucumberReportFileName1);
        when(cucumberReportFile2.isFile()).thenReturn(true);
        when(cucumberReportFile2.getName()).thenReturn(cucumberReportFileName2);

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios).isEmpty();
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(1)).getMatchingDirectory(any(File.class), anyString());
        verify(cucumberScenariosIndexer, never()).getCucumberFeaturesFromReport(any(File.class));
        verify(cucumberScenariosIndexer, never()).getCucumberStepDefinitions(any(File.class));
        verify(executedScenarioExtractorService, never()).extractExecutedScenarios(anyList(), anyList(), anyString());
    }

    @Test
    public void getExecutedScenarios_returnExecutedScenarios_whenCucumberReportsButStepDefinitionsFolderEmpty(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        File cucumberFolder = mock(File.class);

        String cucumberSuffix = "cucumber-suffix";

        File cucumberReportFile1 = mock(File.class);
        String cucumberReportFileName1 = "report1.cucumber-suffix.json";
        File cucumberReportFile2 = mock(File.class);
        String cucumberReportFileName2 = "report2.cucumber-suffix.json";

        String stepDefinitionsFolderPath = "/stepDefinitionsFolder";

        Feature feature11 = mock(Feature.class);
        Feature feature12 = mock(Feature.class);
        Feature feature13 = mock(Feature.class);
        List<Feature> features1 = Arrays.asList(feature11, feature12, feature13);
        List<String> steps1 = Arrays.asList();
        Feature feature21 = mock(Feature.class);
        Feature feature22 = mock(Feature.class);
        List<Feature> features2 = Arrays.asList(feature21, feature22);
        List<String> steps2 = Arrays.asList();

        String runJobUrl = "run-job-url";

        ExecutedScenario executedScenario11 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario12 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario21 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario22 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario23 = mock(ExecutedScenario.class);

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.of(cucumberFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(cucumberSuffix));
        when(cucumberFolder.listFiles()).thenReturn(new File[] {cucumberReportFile1, cucumberReportFile2});
        when(cucumberReportFile1.isFile()).thenReturn(true);
        when(cucumberReportFile1.getName()).thenReturn(cucumberReportFileName1);
        when(cucumberReportFile2.isFile()).thenReturn(true);
        when(cucumberReportFile2.getName()).thenReturn(cucumberReportFileName2);

        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH)).thenReturn(Optional.of(stepDefinitionsFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, stepDefinitionsFolderPath)).thenReturn(Optional.empty());

        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile1)).thenReturn(features1);
        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile2)).thenReturn(features2);

        when(run.getJobUrl()).thenReturn(runJobUrl);

        when(executedScenarioExtractorService.extractExecutedScenarios(features1, steps1, runJobUrl)).thenReturn(Arrays.asList(executedScenario11, executedScenario12));
        when(executedScenarioExtractorService.extractExecutedScenarios(features2, steps2, runJobUrl)).thenReturn(Arrays.asList(executedScenario21, executedScenario22, executedScenario23));

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios)
                .hasSize(5)
                .containsOnly(
                        executedScenario11,
                        executedScenario12,
                        executedScenario21,
                        executedScenario22,
                        executedScenario23
                );
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService, never()).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(2)).getMatchingDirectory(any(File.class), anyString());
        verify(fileProcessorService).getMatchingDirectory(parentFolder, cucumberFolderPath);
        verify(fileProcessorService).getMatchingDirectory(parentFolder, stepDefinitionsFolderPath);
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile1);
        verify(cucumberScenariosIndexer, never()).getCucumberStepDefinitions(any(File.class));
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile2);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features1, steps1, runJobUrl);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features2, steps2, runJobUrl);
    }

    @Test
    public void getExecutedScenarios_returnExecutedScenarios_whenStepDefinitionsFolderHasNoFileMatchingStepDefinitionsSettingSuffix(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        File cucumberFolder = mock(File.class);

        String cucumberSuffix = "cucumber-suffix";

        File cucumberReportFile1 = mock(File.class);
        String cucumberReportFileName1 = "report1.cucumber-suffix.json";
        File cucumberReportFile2 = mock(File.class);
        String cucumberReportFileName2 = "report2.cucumber-suffix.json";

        String stepDefinitionsFolderPath = "/stepDefinitionsFolder";

        File stepDefinitionFolder = mock(File.class);

        String stepDefinitionsSuffix = "step-definitions-suffix";
        File stepDefinitionsFile1 = mock(File.class);
        String stepDefinitionsFileName1 = "report1.not-a-step-definitions-suffix.json";
        File stepDefinitionsFile2 = mock(File.class);
        String stepDefinitionsFileName2 = "report2.not-a-step-definitions-suffix.json";

        Feature feature11 = mock(Feature.class);
        Feature feature12 = mock(Feature.class);
        Feature feature13 = mock(Feature.class);
        List<Feature> features1 = Arrays.asList(feature11, feature12, feature13);
        List<String> steps1 = Arrays.asList();
        Feature feature21 = mock(Feature.class);
        Feature feature22 = mock(Feature.class);
        List<Feature> features2 = Arrays.asList(feature21, feature22);
        List<String> steps2 = Arrays.asList();

        String runJobUrl = "run-job-url";

        ExecutedScenario executedScenario11 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario12 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario21 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario22 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario23 = mock(ExecutedScenario.class);

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.of(cucumberFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(cucumberSuffix));
        when(cucumberFolder.listFiles()).thenReturn(new File[] {cucumberReportFile1, cucumberReportFile2});
        when(cucumberReportFile1.isFile()).thenReturn(true);
        when(cucumberReportFile1.getName()).thenReturn(cucumberReportFileName1);
        when(cucumberReportFile2.isFile()).thenReturn(true);
        when(cucumberReportFile2.getName()).thenReturn(cucumberReportFileName2);

        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH)).thenReturn(Optional.of(stepDefinitionsFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, stepDefinitionsFolderPath)).thenReturn(Optional.of(stepDefinitionFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(stepDefinitionsSuffix));
        when(stepDefinitionFolder.listFiles()).thenReturn(new File[]{stepDefinitionsFile1, stepDefinitionsFile2});
        when(stepDefinitionsFile1.isFile()).thenReturn(true);
        when(stepDefinitionsFile1.getName()).thenReturn(stepDefinitionsFileName1);
        when(stepDefinitionsFile2.isFile()).thenReturn(true);
        when(stepDefinitionsFile2.getName()).thenReturn(stepDefinitionsFileName2);

        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile1)).thenReturn(features1);
        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile2)).thenReturn(features2);

        when(run.getJobUrl()).thenReturn(runJobUrl);

        when(executedScenarioExtractorService.extractExecutedScenarios(features1, steps1, runJobUrl)).thenReturn(Arrays.asList(executedScenario11, executedScenario12));
        when(executedScenarioExtractorService.extractExecutedScenarios(features2, steps2, runJobUrl)).thenReturn(Arrays.asList(executedScenario21, executedScenario22, executedScenario23));

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios)
                .hasSize(5)
                .containsOnly(
                        executedScenario11,
                        executedScenario12,
                        executedScenario21,
                        executedScenario22,
                        executedScenario23
                );
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(2)).getMatchingDirectory(any(File.class), anyString());
        verify(fileProcessorService).getMatchingDirectory(parentFolder, cucumberFolderPath);
        verify(fileProcessorService).getMatchingDirectory(parentFolder, stepDefinitionsFolderPath);
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile1);
        verify(cucumberScenariosIndexer, never()).getCucumberStepDefinitions(any(File.class));
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile2);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features1, steps1, runJobUrl);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features2, steps2, runJobUrl);
    }

    @Test
    public void getExecutedScenarios_returnExecutedScenarios_whenAStepDefinitionsFileIsMissing(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        File cucumberFolder = mock(File.class);

        String cucumberSuffix = "cucumber-suffix";

        File cucumberReportFile1 = mock(File.class);
        String cucumberReportFileName1 = "report1.cucumber-suffix.json";
        File cucumberReportFile2 = mock(File.class);
        String cucumberReportFileName2 = "report2.cucumber-suffix.json";

        String stepDefinitionsFolderPath = "/stepDefinitionsFolder";

        File stepDefinitionFolder = mock(File.class);

        String stepDefinitionsSuffix = "step-definitions-suffix";
        File stepDefinitionsFile1 = mock(File.class);
        String stepDefinitionsFileName1 = "report1.step-definitions-suffix.json";

        Feature feature11 = mock(Feature.class);
        Feature feature12 = mock(Feature.class);
        Feature feature13 = mock(Feature.class);
        List<Feature> features1 = Arrays.asList(feature11, feature12, feature13);
        String step11 = "step11";
        String step12 = "step12";
        String step13 = "step13";
        String step14 = "step14";
        String step15 = "step15";
        List<String> steps1 = Arrays.asList(step11, step12, step13, step14, step15);
        Feature feature21 = mock(Feature.class);
        Feature feature22 = mock(Feature.class);
        List<Feature> features2 = Arrays.asList(feature21, feature22);
        List<String> steps2 = Arrays.asList();

        String runJobUrl = "run-job-url";

        ExecutedScenario executedScenario11 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario12 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario21 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario22 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario23 = mock(ExecutedScenario.class);

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.of(cucumberFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(cucumberSuffix));
        when(cucumberFolder.listFiles()).thenReturn(new File[] {cucumberReportFile1, cucumberReportFile2});
        when(cucumberReportFile1.isFile()).thenReturn(true);
        when(cucumberReportFile1.getName()).thenReturn(cucumberReportFileName1);
        when(cucumberReportFile2.isFile()).thenReturn(true);
        when(cucumberReportFile2.getName()).thenReturn(cucumberReportFileName2);

        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH)).thenReturn(Optional.of(stepDefinitionsFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, stepDefinitionsFolderPath)).thenReturn(Optional.of(stepDefinitionFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(stepDefinitionsSuffix));
        when(stepDefinitionFolder.listFiles()).thenReturn(new File[]{stepDefinitionsFile1});
        when(stepDefinitionsFile1.isFile()).thenReturn(true);
        when(stepDefinitionsFile1.getName()).thenReturn(stepDefinitionsFileName1);

        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile1)).thenReturn(features1);
        when(cucumberScenariosIndexer.getCucumberStepDefinitions(stepDefinitionsFile1)).thenReturn(steps1);
        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile2)).thenReturn(features2);

        when(run.getJobUrl()).thenReturn(runJobUrl);

        when(executedScenarioExtractorService.extractExecutedScenarios(features1, steps1, runJobUrl)).thenReturn(Arrays.asList(executedScenario11, executedScenario12));
        when(executedScenarioExtractorService.extractExecutedScenarios(features2, steps2, runJobUrl)).thenReturn(Arrays.asList(executedScenario21, executedScenario22, executedScenario23));

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios)
                .hasSize(5)
                .containsOnly(
                        executedScenario11,
                        executedScenario12,
                        executedScenario21,
                        executedScenario22,
                        executedScenario23
                );
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(2)).getMatchingDirectory(any(File.class), anyString());
        verify(fileProcessorService).getMatchingDirectory(parentFolder, cucumberFolderPath);
        verify(fileProcessorService).getMatchingDirectory(parentFolder, stepDefinitionsFolderPath);
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile1);
        verify(cucumberScenariosIndexer).getCucumberStepDefinitions(stepDefinitionsFile1);
        verify(cucumberScenariosIndexer, times(1)).getCucumberStepDefinitions(any(File.class));
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile2);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features1, steps1, runJobUrl);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features2, steps2, runJobUrl);
    }

    @Test
    public void getExecutedScenarios_returnExecutedScenarios_whenThereIsAStepDefinitionsFileTooMany(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        File cucumberFolder = mock(File.class);

        String cucumberSuffix = "cucumber-suffix";

        File cucumberReportFile1 = mock(File.class);
        String cucumberReportFileName1 = "report1.cucumber-suffix.json";
        File cucumberReportFile2 = mock(File.class);
        String cucumberReportFileName2 = "report2.cucumber-suffix.json";

        String stepDefinitionsFolderPath = "/stepDefinitionsFolder";

        File stepDefinitionFolder = mock(File.class);

        String stepDefinitionsSuffix = "step-definitions-suffix";
        File stepDefinitionsFile1 = mock(File.class);
        String stepDefinitionsFileName1 = "report1.step-definitions-suffix.json";
        File stepDefinitionsFile2 = mock(File.class);
        String stepDefinitionsFileName2 = "report2.step-definitions-suffix.json";
        File stepDefinitionsFile3 = mock(File.class);
        String stepDefinitionsFileName3 = "report3.step-definitions-suffix.json";

        Feature feature11 = mock(Feature.class);
        Feature feature12 = mock(Feature.class);
        Feature feature13 = mock(Feature.class);
        List<Feature> features1 = Arrays.asList(feature11, feature12, feature13);
        String step11 = "step11";
        String step12 = "step12";
        String step13 = "step13";
        String step14 = "step14";
        String step15 = "step15";
        List<String> steps1 = Arrays.asList(step11, step12, step13, step14, step15);
        Feature feature21 = mock(Feature.class);
        Feature feature22 = mock(Feature.class);
        List<Feature> features2 = Arrays.asList(feature21, feature22);
        String step21 = "step21";
        String step22 = "step22";
        String step23 = "step23";
        List<String> steps2 = Arrays.asList(step21, step22, step23);

        String runJobUrl = "run-job-url";

        ExecutedScenario executedScenario11 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario12 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario21 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario22 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario23 = mock(ExecutedScenario.class);

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.of(cucumberFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(cucumberSuffix));
        when(cucumberFolder.listFiles()).thenReturn(new File[] {cucumberReportFile1, cucumberReportFile2});
        when(cucumberReportFile1.isFile()).thenReturn(true);
        when(cucumberReportFile1.getName()).thenReturn(cucumberReportFileName1);
        when(cucumberReportFile2.isFile()).thenReturn(true);
        when(cucumberReportFile2.getName()).thenReturn(cucumberReportFileName2);

        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH)).thenReturn(Optional.of(stepDefinitionsFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, stepDefinitionsFolderPath)).thenReturn(Optional.of(stepDefinitionFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(stepDefinitionsSuffix));
        when(stepDefinitionFolder.listFiles()).thenReturn(new File[]{stepDefinitionsFile1, stepDefinitionsFile2, stepDefinitionsFile3});
        when(stepDefinitionsFile1.isFile()).thenReturn(true);
        when(stepDefinitionsFile1.getName()).thenReturn(stepDefinitionsFileName1);
        when(stepDefinitionsFile2.isFile()).thenReturn(true);
        when(stepDefinitionsFile2.getName()).thenReturn(stepDefinitionsFileName2);
        when(stepDefinitionsFile3.isFile()).thenReturn(true);
        when(stepDefinitionsFile3.getName()).thenReturn(stepDefinitionsFileName3);

        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile1)).thenReturn(features1);
        when(cucumberScenariosIndexer.getCucumberStepDefinitions(stepDefinitionsFile1)).thenReturn(steps1);
        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile2)).thenReturn(features2);
        when(cucumberScenariosIndexer.getCucumberStepDefinitions(stepDefinitionsFile2)).thenReturn(steps2);

        when(run.getJobUrl()).thenReturn(runJobUrl);

        when(executedScenarioExtractorService.extractExecutedScenarios(features1, steps1, runJobUrl)).thenReturn(Arrays.asList(executedScenario11, executedScenario12));
        when(executedScenarioExtractorService.extractExecutedScenarios(features2, steps2, runJobUrl)).thenReturn(Arrays.asList(executedScenario21, executedScenario22, executedScenario23));

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios)
                .hasSize(5)
                .containsOnly(
                        executedScenario11,
                        executedScenario12,
                        executedScenario21,
                        executedScenario22,
                        executedScenario23
                );
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(2)).getMatchingDirectory(any(File.class), anyString());
        verify(fileProcessorService).getMatchingDirectory(parentFolder, cucumberFolderPath);
        verify(fileProcessorService).getMatchingDirectory(parentFolder, stepDefinitionsFolderPath);
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile1);
        verify(cucumberScenariosIndexer).getCucumberStepDefinitions(stepDefinitionsFile1);
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile2);
        verify(cucumberScenariosIndexer).getCucumberStepDefinitions(stepDefinitionsFile2);
        verifyNoMoreInteractions(cucumberScenariosIndexer);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features1, steps1, runJobUrl);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features2, steps2, runJobUrl);
        verifyNoMoreInteractions(executedScenarioExtractorService);
    }

    @Test
    public void getExecutedScenarios_returnExecutedScenarios_whenAllCucumberReportsMatchStepDefinitions(){
        // Given
        File parentFolder = mock(File.class);
        Run run = mock(Run.class);
        Long projectId = 1L;

        String cucumberFolderPath = "/cucumberFolder";

        File cucumberFolder = mock(File.class);

        String cucumberSuffix = "cucumber-suffix";

        File cucumberReportFile1 = mock(File.class);
        String cucumberReportFileName1 = "report1.cucumber-suffix.json";
        File cucumberReportFile2 = mock(File.class);
        String cucumberReportFileName2 = "report2.cucumber-suffix.json";

        String stepDefinitionsFolderPath = "/stepDefinitionsFolder";

        File stepDefinitionFolder = mock(File.class);

        String stepDefinitionsSuffix = "step-definitions-suffix";
        File stepDefinitionsFile1 = mock(File.class);
        String stepDefinitionsFileName1 = "report1.step-definitions-suffix.json";
        File stepDefinitionsFile2 = mock(File.class);
        String stepDefinitionsFileName2 = "report2.step-definitions-suffix.json";

        Feature feature11 = mock(Feature.class);
        Feature feature12 = mock(Feature.class);
        Feature feature13 = mock(Feature.class);
        List<Feature> features1 = Arrays.asList(feature11, feature12, feature13);
        String step11 = "step11";
        String step12 = "step12";
        String step13 = "step13";
        String step14 = "step14";
        String step15 = "step15";
        List<String> steps1 = Arrays.asList(step11, step12, step13, step14, step15);
        Feature feature21 = mock(Feature.class);
        Feature feature22 = mock(Feature.class);
        List<Feature> features2 = Arrays.asList(feature21, feature22);
        String step21 = "step21";
        String step22 = "step22";
        String step23 = "step23";
        List<String> steps2 = Arrays.asList(step21, step22, step23);

        String runJobUrl = "run-job-url";

        ExecutedScenario executedScenario11 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario12 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario21 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario22 = mock(ExecutedScenario.class);
        ExecutedScenario executedScenario23 = mock(ExecutedScenario.class);

        // When
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS)).thenReturn(Optional.of(cucumberFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, cucumberFolderPath)).thenReturn(Optional.of(cucumberFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(cucumberSuffix));
        when(cucumberFolder.listFiles()).thenReturn(new File[] {cucumberReportFile1, cucumberReportFile2});
        when(cucumberReportFile1.isFile()).thenReturn(true);
        when(cucumberReportFile1.getName()).thenReturn(cucumberReportFileName1);
        when(cucumberReportFile2.isFile()).thenReturn(true);
        when(cucumberReportFile2.getName()).thenReturn(cucumberReportFileName2);

        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH)).thenReturn(Optional.of(stepDefinitionsFolderPath));
        when(fileProcessorService.getMatchingDirectory(parentFolder, stepDefinitionsFolderPath)).thenReturn(Optional.of(stepDefinitionFolder));
        when(technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE)).thenReturn(Optional.of(stepDefinitionsSuffix));
        when(stepDefinitionFolder.listFiles()).thenReturn(new File[]{stepDefinitionsFile1, stepDefinitionsFile2});
        when(stepDefinitionsFile1.isFile()).thenReturn(true);
        when(stepDefinitionsFile1.getName()).thenReturn(stepDefinitionsFileName1);
        when(stepDefinitionsFile2.isFile()).thenReturn(true);
        when(stepDefinitionsFile2.getName()).thenReturn(stepDefinitionsFileName2);

        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile1)).thenReturn(features1);
        when(cucumberScenariosIndexer.getCucumberStepDefinitions(stepDefinitionsFile1)).thenReturn(steps1);
        when(cucumberScenariosIndexer.getCucumberFeaturesFromReport(cucumberReportFile2)).thenReturn(features2);
        when(cucumberScenariosIndexer.getCucumberStepDefinitions(stepDefinitionsFile2)).thenReturn(steps2);

        when(run.getJobUrl()).thenReturn(runJobUrl);

        when(executedScenarioExtractorService.extractExecutedScenarios(features1, steps1, runJobUrl)).thenReturn(Arrays.asList(executedScenario11, executedScenario12));
        when(executedScenarioExtractorService.extractExecutedScenarios(features2, steps2, runJobUrl)).thenReturn(Arrays.asList(executedScenario21, executedScenario22, executedScenario23));

        // Then
        List<ExecutedScenario> executedScenarios = cypressScenariosIndexer.getExecutedScenarios(parentFolder, run, projectId);
        assertThat(executedScenarios)
                .hasSize(5)
                .containsOnly(
                        executedScenario11,
                        executedScenario12,
                        executedScenario21,
                        executedScenario22,
                        executedScenario23
                );
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH);
        verify(technologySettingService).getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE);
        verify(fileProcessorService, times(2)).getMatchingDirectory(any(File.class), anyString());
        verify(fileProcessorService).getMatchingDirectory(parentFolder, cucumberFolderPath);
        verify(fileProcessorService).getMatchingDirectory(parentFolder, stepDefinitionsFolderPath);
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile1);
        verify(cucumberScenariosIndexer).getCucumberStepDefinitions(stepDefinitionsFile1);
        verify(cucumberScenariosIndexer).getCucumberFeaturesFromReport(cucumberReportFile2);
        verify(cucumberScenariosIndexer).getCucumberStepDefinitions(stepDefinitionsFile2);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features1, steps1, runJobUrl);
        verify(executedScenarioExtractorService).extractExecutedScenarios(features2, steps2, runJobUrl);
    }
}
