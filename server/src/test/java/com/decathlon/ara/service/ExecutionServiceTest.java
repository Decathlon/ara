/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
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

package com.decathlon.ara.service;

import com.decathlon.ara.ci.bean.PlannedIndexation;
import com.decathlon.ara.ci.service.ExecutionIndexerService;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.ExecutionCompletionRequest;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.repository.ExecutionCompletionRequestRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.mapper.ExecutionMapper;
import com.decathlon.ara.service.mapper.ExecutionWithHandlingCountsMapper;
import com.decathlon.ara.service.support.Settings;
import com.decathlon.ara.service.transformer.ExecutionTransformer;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private ExecutionCompletionRequestRepository executionCompletionRequestRepository;

    @Mock
    private FunctionalityRepository functionalityRepository;

    @Mock
    private ExecutionMapper executionMapper;

    @Mock
    private ExecutionWithHandlingCountsMapper executionWithHandlingCountsMapper;

    @Mock
    private ExecutionTransformer executionTransformer;

    @Mock
    private ExecutionHistoryService executionHistoryService;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private SettingService settingService;

    @Mock
    private ExecutionIndexerService executionIndexerService;

    @Mock
    private CycleDefinitionRepository cycleDefinitionRepository;

    @Mock
    private FeatureService featureService;

    @Mock
    private ProblemService problemService;

    @Spy
    @InjectMocks
    private ExecutionService cut;

    @Test
    public void requestCompletion_should_register_request_when_execution_not_crawled_yet() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(null);
        ArgumentCaptor<ExecutionCompletionRequest> argument = ArgumentCaptor.forClass(ExecutionCompletionRequest.class);
        when(executionCompletionRequestRepository.save(argument.capture())).thenReturn(null);

        // WHEN
        cut.requestCompletion(projectId, "url");

        // THEN
        assertThat(argument.getValue().getJobUrl()).isEqualTo("url");
    }

    @Test
    public void requestCompletion_should_register_request_when_execution_status_is_not_DONE_yet() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution());
        ArgumentCaptor<ExecutionCompletionRequest> argument = ArgumentCaptor.forClass(ExecutionCompletionRequest.class);
        when(executionCompletionRequestRepository.save(argument.capture())).thenReturn(null);

        // WHEN
        cut.requestCompletion(projectId, "url");

        // THEN
        assertThat(argument.getValue().getJobUrl()).isEqualTo("url");
    }

    @Test
    public void requestCompletion_should_not_register_request_when_execution_status_is_DONE() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution().withStatus(JobStatus.DONE));

        // WHEN
        cut.requestCompletion(projectId, "url");

        // THEN
        verify(executionCompletionRequestRepository, never()).save(any(ExecutionCompletionRequest.class));
    }

    @Test
    public void getQualityStatus_should_return_SILL_COMPUTING_when_execution_not_found() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(null);

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("STILL_COMPUTING");
    }

    @Test
    public void getQualityStatus_should_return_SILL_COMPUTING_when_running_execution_has_completion_request() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution().withStatus(JobStatus.RUNNING));
        when(executionCompletionRequestRepository.findById(eq("url"))).thenReturn(Optional.of(new ExecutionCompletionRequest()));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("STILL_COMPUTING");
    }

    @Test
    public void getQualityStatus_should_return_quality_status_when_running_execution_has_no_completion_request() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.RUNNING)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities("any")
                .withQualityThresholds("any")
                .withQualityStatus(QualityStatus.WARNING));
        when(executionCompletionRequestRepository.findById(eq("url"))).thenReturn(Optional.empty());

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("WARNING");
    }

    @Test
    public void getQualityStatus_should_return_quality_status_when_execution_is_DONE() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities("any")
                .withQualityThresholds("any")
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("WARNING");
    }

    @Test
    public void getQualityStatus_should_return_INCOMPLETE_when_execution_is_DONE_and_has_no_quality_status() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities("any")
                .withQualityThresholds("any")
                .withQualityStatus(null));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("INCOMPLETE");
    }

    @Test
    public void getQualityStatus_should_not_retrieve_completion_request_when_execution_is_DONE() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE));

        // WHEN
        cut.getQualityStatus(projectId, "url");

        // THEN
        verify(executionCompletionRequestRepository, never()).findById(anyString());
    }

    @Test
    public void getQualityStatus_should_return_CRASHED_when_DONE_execution_has_null_qualitySeverity() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities(null)
                .withQualityThresholds("any")
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("CRASHED");
    }

    @Test
    public void getQualityStatus_should_return_CRASHED_when_DONE_execution_has_empty_qualitySeverity() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities("")
                .withQualityThresholds("any")
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("CRASHED");
    }

    @Test
    public void getQualityStatus_should_return_CRASHED_when_DONE_execution_has_null_qualityThresholds() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities("any")
                .withQualityThresholds(null)
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("CRASHED");
    }

    @Test
    public void getQualityStatus_should_return_CRASHED_when_DONE_execution_has_empty_qualityThresholds() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities("any")
                .withQualityThresholds("")
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("CRASHED");
    }

    @Test
    public void getQualityStatus_should_return_NOT_BLOCKING_when_DONE_execution_is_not_blocking() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.FALSE)
                .withQualitySeverities("any")
                .withQualityThresholds("any")
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("NOT_BLOCKING");
    }

    @Test
    public void getQualityStatus_should_return_NOT_BLOCKING_when_DONE_execution_has_null_blocking() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(null)
                .withQualitySeverities("any")
                .withQualityThresholds("any")
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("NOT_BLOCKING");
    }

    @Test
    public void getQualityStatus_should_return_DISCARDED_when_execution_has_discard_reason() {
        // GIVEN
        long projectId = 1;
        when(executionRepository.findByProjectIdAndJobUrl(projectId, "url")).thenReturn(new Execution()
                .withStatus(JobStatus.DONE)
                .withBlockingValidation(Boolean.TRUE)
                .withQualitySeverities("any")
                .withQualityThresholds("any")
                .withDiscardReason("some reason")
                .withQualityStatus(QualityStatus.WARNING));

        // WHEN
        String status = cut.getQualityStatus(projectId, "url");

        // THEN
        assertThat(status).isEqualTo("DISCARDED");
    }

    @Test
    public void uploadExecutionReport_should_call_the_indexer() throws IOException {
        // Given
        long projectId = 23L;
        String projectCode = "prj";
        String branch = "master";
        String cycle = "day";
        MultipartFile zip = new MockMultipartFile("zip", "test.zip", "application/zip", new byte[0]);
        CycleDefinition cycleDefinition = new CycleDefinition(1L, projectId, branch, cycle, 1);
        File executionPath = new File("/opt/executions/123");
        PlannedIndexation plannedIndexation = new PlannedIndexation().withCycleDefinition(cycleDefinition).withExecutionFolder(executionPath);
        List<File> unzipMock = Collections.singletonList(executionPath);

        // When
        doReturn(unzipMock).when(cut).unzipExecutions(any(), any(), any());
        doReturn("/opt/data/{{project}}/{{branch}}/{{cycle}}").when(settingService).get(projectId, Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH);
        doReturn(Optional.of(cycleDefinition)).when(cycleDefinitionRepository).findByProjectIdAndBranchAndName(projectId, branch, cycle);
        doNothing().when(executionIndexerService).indexExecution(any());

        // Then
        cut.uploadExecutionReport(projectId, projectCode, branch, cycle, zip);
        verify(cut).launchExecutionDirectoriesProcessingThread(anyLong(), anyList(), any(CycleDefinition.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void uploadExecutionReport_should_throw_IllegalArgumentException_if_cycle_doesnt_exists() throws IOException {
        // Given
        long projectId = 23L;
        String projectCode = "prj";
        String branch = "master";
        String cycle = "day";
        MultipartFile zip = new MockMultipartFile("zip", "test.zip", "application/zip", new byte[0]);
        doReturn(Optional.empty()).when(cycleDefinitionRepository).findByProjectIdAndBranchAndName(projectId, branch, cycle);
        // When
        cut.uploadExecutionReport(projectId, projectCode, branch, cycle, zip);
    }

    @Test
    public void unzipExecutions_should_call_unzip_and_return_result() throws IOException {
        // GIVEN
        File target = new File(System.getProperty("java.io.tmpdir"),"ara-unzipExecutions-" +
                new Date().getTime());
        target.mkdir(); // Will log in warning level due to the fact that cut will try to recreate it.
        new File(target, "tmp").createNewFile(); // To make the directory not empty.
        MultipartFile file = new MockMultipartFile("zip", "test.zip", "application/zip", new byte[0]);
        doNothing().when(archiveService).unzip(file, target);
        List<File> executions = new ArrayList<>();
        executions.add(new File(target, "execution1"));
        doReturn(executions).when(cut).retrieveAllExecutionDirectories(target, "buildInformation.json");
        try {
            // WHEN
            List<File> files = this.cut.unzipExecutions(target, file, "buildInformation.json");
            // THEN
            verify(archiveService).unzip(file, target);
            assertThat(files).hasSize(1);
            assertThat(files.get(0)).isEqualTo(new File(target, "execution1"));
        } finally {
            FileUtils.deleteQuietly(target);
        }
    }

    @Test(expected = IOException.class)
    public void unzipExecutions_should_propagate_ioexception_from_archiveservice() throws IOException {
        // GIVEN
        File target = new File(System.getProperty("java.io.tmpdir"),"ara-unzipExecutions-" +
                new Date().getTime());
        MultipartFile file = new MockMultipartFile("zip", "test.zip", "application/zip", new byte[0]);
        doThrow(new IOException("Unable to write")).when(archiveService).unzip(file, target);
        try {
            // WHEN
            List<File> files = this.cut.unzipExecutions(target, file, "buildInformation.json");
            // Then is made by the annotation.
        } finally {
            FileUtils.deleteQuietly(target);
        }
    }

    @Test
    public void retrieveAllExecutionDirectories_should_return_the_current_directory() {
        // GIVEN
        File directory = new File(System.getProperty("java.io.tmpdir"), "ara-retrieveAllExeDir-" +
                new Date().getTime());
        directory.mkdir();
        doReturn(true).when(this.cut).isExecutionDirectory(directory, "buildInformation.json");
        File subdir = new File(directory, "subdir");
        subdir.mkdir();
        // WHEN
        List<File> paths = cut.retrieveAllExecutionDirectories(directory, "buildInformation.json");
        FileUtils.deleteQuietly(directory);
        // THEN
        assertThat(paths).hasSize(1);
        assertThat(paths.get(0)).isEqualTo(directory);
    }

    @Test
    public void retrieveAllExecutionDirectories_should_return_sub_directories() {
        // GIVEN
        File directory = new File(System.getProperty("java.io.tmpdir"), "ara-retrieveAllExeDirSubDir-" +
                new Date().getTime());
        directory.mkdir();
        doReturn(false).when(this.cut).isExecutionDirectory(directory, "buildInformation.json");
        File subdir1 = new File(directory, "subdir1");
        subdir1.mkdir();
        doReturn(true).when(this.cut).isExecutionDirectory(subdir1, "buildInformation.json");
        File subdir2 = new File(directory, "subdir2");
        subdir2.mkdir();
        doReturn(false).when(this.cut).isExecutionDirectory(subdir2, "buildInformation.json");
        File subdir3 = new File(directory, "subdir3");
        subdir3.mkdir();
        doReturn(true).when(this.cut).isExecutionDirectory(subdir3, "buildInformation.json");
        // WHEN
        List<File> paths = cut.retrieveAllExecutionDirectories(directory, "buildInformation.json");
        FileUtils.deleteQuietly(directory);
        // THEN
        assertThat(paths).hasSize(2);
        assertThat(paths).containsOnly(subdir1, subdir3);
    }

    @Test
    public void retrieveAllExecutionDirectories_should_return_empty_if_not_an_execution_directory() {
        // GIVEN
        File directory = new File(System.getProperty("java.io.tmpdir"), "ara-retrieveAllExeDir-" +
                new Date().getTime());
        directory.mkdir();
        doReturn(false).when(this.cut).isExecutionDirectory(directory, "buildInformation.json");
        File subdir1 = new File(directory, "subdir1");
        subdir1.mkdir();
        doReturn(false).when(this.cut).isExecutionDirectory(subdir1, "buildInformation.json");
        File subdir2 = new File(directory, "subdir2");
        subdir2.mkdir();
        doReturn(false).when(this.cut).isExecutionDirectory(subdir2, "buildInformation.json");
        // WHEN
        List<File> paths = cut.retrieveAllExecutionDirectories(directory, "buildInformation.json");
        FileUtils.deleteQuietly(directory);
        // THEN
        assertThat(paths).isEmpty();
    }

    @Test
    public void retrieveAllExecutionDirectories_should_return_empty_if_is_empty() {
        // GIVEN
        File directory = new File(System.getProperty("java.io.tmpdir"), "ara-retrieveAllExeDir-" +
                new Date().getTime());
        directory.mkdir();
        // WHEN
        List<File> paths = cut.retrieveAllExecutionDirectories(directory, "buildInformation.json");
        FileUtils.deleteQuietly(directory);
        // THEN
        assertThat(paths).isEmpty();
    }

    @Test
    public void isExecutionDirectory_should_return_true() throws IOException {
        // Given
        String timestamp = String.valueOf(new Date().getTime());
        File directory = new File(System.getProperty("java.io.tmpdir"), timestamp);
        directory.mkdirs();
        try {
            new File(directory, "buildInformation.json").createNewFile();
            // When
            boolean result = cut.isExecutionDirectory(directory, "buildInformation.json");
            // Then
            assertThat(result).isTrue();
        } finally {
            FileUtils.deleteQuietly(directory);
        }
    }

    @Test
    public void isExecutionDirectory_should_return_false_if_its_not_a_directory() throws IOException {
        File test = new File(System.getProperty("java.io.tmpdir"), "buildInformation.json");
        try {
            test.createNewFile();
            // When
            boolean result = cut.isExecutionDirectory(test, "buildInformation.json");
            // Then
            assertThat(result).isFalse();
        } finally {
            FileUtils.deleteQuietly(test);
        }
    }

    @Test
    public void isExecutionDirectory_should_return_false_if_name_is_not_a_timestamp() throws IOException {
        // Given
        File directory = new File(System.getProperty("java.io.tmpdir"), "not_a_timestamp");
        directory.mkdirs();
        try {
            new File(directory, "buildInformation.json").createNewFile();
            // When
            boolean result = cut.isExecutionDirectory(directory, "buildInformation.json");
            // Then
            assertThat(result).isFalse();
        } finally {
            FileUtils.deleteQuietly(directory);
        }
    }

    @Test
    public void isExecutionDirectory_should_return_false_if_dont_contains_buildInformation() {
        // Given
        String timestamp = String.valueOf(new Date().getTime());
        File directory = new File(System.getProperty("java.io.tmpdir"), timestamp);
        directory.mkdirs();
        try {
            // When
            boolean result = cut.isExecutionDirectory(directory, "buildInformation.json");
            // Then
            assertThat(result).isFalse();
        } finally {
            FileUtils.deleteQuietly(directory);
        }
    }
}
