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

package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.PlannedIndexation;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.*;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.custom.util.TransactionAppenderUtil;
import com.decathlon.ara.service.ExecutionFilesProcessorService;
import com.decathlon.ara.service.ProblemDenormalizationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionIndexerServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private ErrorRepository errorRepository;

    @Mock
    private QualityEmailService qualityEmailService;

    @Mock
    private ProblemDenormalizationService problemDenormalizationService;

    @Mock
    private TransactionAppenderUtil transactionService;

    @Mock
    private ExecutionFilesProcessorService executionFilesProcessorService;

    @Spy
    @InjectMocks
    private ExecutionIndexerService cut;

    @Test
    public void indexExecution_doNothing_whenThePlannedIndexationIsNull() {
        // Given
        PlannedIndexation plannedIndexation = null;

        // When

        // Then
        cut.indexExecution(plannedIndexation);

        verify(executionFilesProcessorService, never()).getExecution(any(PlannedIndexation.class));
        verify(executionRepository, never()).save(any(Execution.class));
        verify(executionRepository, never()).findByProjectIdAndJobUrlOrJobLink(anyLong(), anyString(), anyString());
        verify(errorRepository, never()).autoAssignProblemsToNewErrors(anyLong(), anyList());
        verify(problemDenormalizationService, never()).updateFirstAndLastSeenDateTimes(anyCollection());
        verify(transactionService, never()).doAfterCommit(any(Runnable.class));
    }

    @Test
    public void indexExecution_doNothing_whenThePlannedIndexationHasNoExecutionFolder() {
        // Given
        PlannedIndexation plannedIndexation = mock(PlannedIndexation.class);

        // When
        when(plannedIndexation.getExecutionFolder()).thenReturn(null);

        // Then
        cut.indexExecution(plannedIndexation);

        verify(executionFilesProcessorService, never()).getExecution(any(PlannedIndexation.class));
        verify(executionRepository, never()).save(any(Execution.class));
        verify(executionRepository, never()).findByProjectIdAndJobUrlOrJobLink(anyLong(), anyString(), anyString());
        verify(errorRepository, never()).autoAssignProblemsToNewErrors(anyLong(), anyList());
        verify(problemDenormalizationService, never()).updateFirstAndLastSeenDateTimes(anyCollection());
        verify(transactionService, never()).doAfterCommit(any(Runnable.class));
    }

    @Test
    public void indexExecution_doNothing_whenThePlannedIndexationHasNoCycleDefinition() {
        // Given
        PlannedIndexation plannedIndexation = mock(PlannedIndexation.class);
        File executionFile = mock(File.class);

        // When
        when(plannedIndexation.getExecutionFolder()).thenReturn(executionFile);
        when(plannedIndexation.getCycleDefinition()).thenReturn(null);

        // Then
        cut.indexExecution(plannedIndexation);

        verify(executionFilesProcessorService, never()).getExecution(any(PlannedIndexation.class));
        verify(executionRepository, never()).save(any(Execution.class));
        verify(executionRepository, never()).findByProjectIdAndJobUrlOrJobLink(anyLong(), anyString(), anyString());
        verify(errorRepository, never()).autoAssignProblemsToNewErrors(anyLong(), anyList());
        verify(problemDenormalizationService, never()).updateFirstAndLastSeenDateTimes(anyCollection());
        verify(transactionService, never()).doAfterCommit(any(Runnable.class));
    }

    @Test
    public void indexExecution_doNothing_whenNoExecutionIndexed() {
        // Given
        PlannedIndexation plannedIndexation = mock(PlannedIndexation.class);
        File executionFile = mock(File.class);
        CycleDefinition cycleDefinition = mock(CycleDefinition.class);

        // When
        when(plannedIndexation.getExecutionFolder()).thenReturn(executionFile);
        when(plannedIndexation.getCycleDefinition()).thenReturn(cycleDefinition);
        when(executionFile.getAbsolutePath()).thenReturn("/execution/folder/location/in/disk");
        when(cycleDefinition.getProjectId()).thenReturn(1L);
        when(cycleDefinition.getBranch()).thenReturn("branch");
        when(cycleDefinition.getName()).thenReturn("cycle");
        when(executionFilesProcessorService.getExecution(plannedIndexation)).thenReturn(Optional.empty());

        // Then
        cut.indexExecution(plannedIndexation);

        verify(executionFilesProcessorService).getExecution(plannedIndexation);
        verify(executionRepository, never()).save(any(Execution.class));
        verify(executionRepository, never()).findByProjectIdAndJobUrlOrJobLink(anyLong(), anyString(), anyString());
        verify(errorRepository, never()).autoAssignProblemsToNewErrors(anyLong(), anyList());
        verify(problemDenormalizationService, never()).updateFirstAndLastSeenDateTimes(anyCollection());
        verify(transactionService, never()).doAfterCommit(any(Runnable.class));
    }

    @Test
    public void indexExecution_saveIndexedExecution_whenExecutionIndexed() {
        // Given
        PlannedIndexation plannedIndexation = mock(PlannedIndexation.class);
        File executionFile = mock(File.class);
        CycleDefinition cycleDefinition = mock(CycleDefinition.class);

        Execution indexedExecution = mock(Execution.class);
        Execution savedExecution = mock(Execution.class);

        // When
        when(plannedIndexation.getExecutionFolder()).thenReturn(executionFile);
        when(plannedIndexation.getCycleDefinition()).thenReturn(cycleDefinition);
        when(executionFile.getAbsolutePath()).thenReturn("/execution/folder/location/in/disk");
        when(cycleDefinition.getProjectId()).thenReturn(1L);
        when(cycleDefinition.getBranch()).thenReturn("branch");
        when(cycleDefinition.getName()).thenReturn("cycle");
        when(executionFilesProcessorService.getExecution(plannedIndexation)).thenReturn(Optional.of(indexedExecution));
        when(executionRepository.save(indexedExecution)).thenReturn(savedExecution);
        when(indexedExecution.getJobUrl()).thenReturn("http://execution-url.build.org");
        when(savedExecution.getStatus()).thenReturn(JobStatus.UNAVAILABLE);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(1L,"http://execution-url.build.org", "/execution/folder/location/in/disk" + File.separator)).thenReturn(Optional.empty());

        // Then
        cut.indexExecution(plannedIndexation);

        verify(executionFilesProcessorService).getExecution(plannedIndexation);
        verify(executionRepository).save(indexedExecution);
        verify(executionRepository).findByProjectIdAndJobUrlOrJobLink(1L,"http://execution-url.build.org", "/execution/folder/location/in/disk" + File.separator);
        verify(errorRepository, never()).autoAssignProblemsToNewErrors(anyLong(), anyList());
        verify(problemDenormalizationService, never()).updateFirstAndLastSeenDateTimes(anyCollection());
        verify(transactionService, never()).doAfterCommit(any(Runnable.class));
    }

    @Test
    public void indexExecution_manageErrors_whenErrorsFound() {
        // Given
        PlannedIndexation plannedIndexation = mock(PlannedIndexation.class);
        File executionFile = mock(File.class);
        CycleDefinition cycleDefinition = mock(CycleDefinition.class);

        Execution indexedExecution = mock(Execution.class);

        Execution savedExecution = mock(Execution.class);
        Run savedRun1 = mock(Run.class);
        ExecutedScenario savedScenario11 = mock(ExecutedScenario.class);
        Set<ExecutedScenario> savedScenarios1 = new TreeSet<>();
        savedScenarios1.addAll(Arrays.asList(savedScenario11));
        Error savedError111 = mock(Error.class);
        Error savedError112 = mock(Error.class);
        Error savedError113 = mock(Error.class);
        Set<Error> savedErrors11 = new TreeSet<>();
        savedErrors11.addAll(Arrays.asList(savedError111, savedError112, savedError113));
        Run savedRun2 = mock(Run.class);
        ExecutedScenario savedScenario21 = mock(ExecutedScenario.class);
        Error savedError211 = mock(Error.class);
        Error savedError212 = mock(Error.class);
        Set<Error> savedErrors21 = new TreeSet<>();
        savedErrors21.addAll(Arrays.asList(savedError211, savedError212));
        ExecutedScenario savedScenario22 = mock(ExecutedScenario.class);
        Error savedError221 = mock(Error.class);
        Error savedError222 = mock(Error.class);
        Set<Error> savedErrors22 = new TreeSet<>();
        savedErrors22.addAll(Arrays.asList(savedError221, savedError222));
        Set<ExecutedScenario> savedScenarios2 = new TreeSet<>();
        savedScenarios2.addAll(Arrays.asList(savedScenario21, savedScenario22));
        Set<Run> savedRuns = new TreeSet<>();
        savedRuns.addAll(Arrays.asList(savedRun1, savedRun2));

        Execution previousExecution = mock(Execution.class);
        Run previousRun = mock(Run.class);
        Set<Run> previousRuns = new TreeSet<>();
        previousRuns.add(previousRun);
        ExecutedScenario previousScenario = mock(ExecutedScenario.class);
        Set<ExecutedScenario> previousExecutedScenarios = new TreeSet<>();
        previousExecutedScenarios.add(previousScenario);
        Error previousError1 = mock(Error.class);
        Error previousError2 = mock(Error.class);
        Set<Error> previousErrors = new TreeSet<>();
        previousErrors.addAll(Arrays.asList(previousError1, previousError2));

        Problem problem1 = mock(Problem.class);
        Problem problem2 = mock(Problem.class);
        Problem problem3 = mock(Problem.class);
        Set<Problem> problems = new TreeSet<>();
        problems.addAll(Arrays.asList(problem1, problem2, problem3));

        // When
        when(plannedIndexation.getExecutionFolder()).thenReturn(executionFile);
        when(plannedIndexation.getCycleDefinition()).thenReturn(cycleDefinition);
        when(executionFile.getAbsolutePath()).thenReturn("/execution/folder/location/in/disk");
        when(cycleDefinition.getProjectId()).thenReturn(1L);
        when(cycleDefinition.getBranch()).thenReturn("branch");
        when(cycleDefinition.getName()).thenReturn("cycle");
        when(executionFilesProcessorService.getExecution(plannedIndexation)).thenReturn(Optional.of(indexedExecution));
        when(executionRepository.save(indexedExecution)).thenReturn(savedExecution);
        when(indexedExecution.getJobUrl()).thenReturn("http://execution-url.build.org");
        when(savedExecution.getStatus()).thenReturn(JobStatus.UNAVAILABLE);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(1L,"http://execution-url.build.org", "/execution/folder/location/in/disk" + File.separator)).thenReturn(Optional.of(previousExecution));

        when(previousExecution.getRuns()).thenReturn(previousRuns);
        when(previousRun.getExecutedScenarios()).thenReturn(previousExecutedScenarios);
        when(previousScenario.getErrors()).thenReturn(previousErrors);
        when(previousError1.getId()).thenReturn(112L);
        when(previousError2.getId()).thenReturn(212L);

        when(savedExecution.getRuns()).thenReturn(savedRuns);
        when(savedRun1.getExecutedScenarios()).thenReturn(savedScenarios1);
        when(savedScenario11.getErrors()).thenReturn(savedErrors11);
        when(savedError111.getId()).thenReturn(111L);
        when(savedError112.getId()).thenReturn(112L);
        when(savedError113.getId()).thenReturn(113L);
        when(savedRun2.getExecutedScenarios()).thenReturn(savedScenarios2);
        when(savedScenario21.getErrors()).thenReturn(savedErrors21);
        when(savedError211.getId()).thenReturn(211L);
        when(savedError212.getId()).thenReturn(212L);
        when(savedScenario22.getErrors()).thenReturn(savedErrors22);
        when(savedError221.getId()).thenReturn(221L);
        when(savedError222.getId()).thenReturn(222L);

        when(errorRepository.autoAssignProblemsToNewErrors(anyLong(), anyList())).thenReturn(problems);

        // Then
        cut.indexExecution(plannedIndexation);

        verify(executionFilesProcessorService).getExecution(plannedIndexation);
        verify(executionRepository).save(indexedExecution);
        verify(executionRepository).findByProjectIdAndJobUrlOrJobLink(1L,"http://execution-url.build.org", "/execution/folder/location/in/disk" + File.separator);
        verify(errorRepository).autoAssignProblemsToNewErrors(1L, Arrays.asList(111L, 113L, 211L, 221L, 222L));
        verify(problemDenormalizationService).updateFirstAndLastSeenDateTimes(problems);
        verify(transactionService, never()).doAfterCommit(any(Runnable.class));
    }

    @Test
    public void indexExecution_sendEmail_whenExecutionStatusIsDone() {
        // Given
        PlannedIndexation plannedIndexation = mock(PlannedIndexation.class);
        File executionFile = mock(File.class);
        CycleDefinition cycleDefinition = mock(CycleDefinition.class);

        Execution indexedExecution = mock(Execution.class);

        Execution savedExecution = mock(Execution.class);
        Run savedRun1 = mock(Run.class);
        ExecutedScenario savedScenario11 = mock(ExecutedScenario.class);
        Set<ExecutedScenario> savedScenarios1 = new TreeSet<>();
        savedScenarios1.addAll(Arrays.asList(savedScenario11));
        Error savedError111 = mock(Error.class);
        Error savedError112 = mock(Error.class);
        Error savedError113 = mock(Error.class);
        Set<Error> savedErrors11 = new TreeSet<>();
        savedErrors11.addAll(Arrays.asList(savedError111, savedError112, savedError113));
        Run savedRun2 = mock(Run.class);
        ExecutedScenario savedScenario21 = mock(ExecutedScenario.class);
        Error savedError211 = mock(Error.class);
        Error savedError212 = mock(Error.class);
        Set<Error> savedErrors21 = new TreeSet<>();
        savedErrors21.addAll(Arrays.asList(savedError211, savedError212));
        ExecutedScenario savedScenario22 = mock(ExecutedScenario.class);
        Error savedError221 = mock(Error.class);
        Error savedError222 = mock(Error.class);
        Set<Error> savedErrors22 = new TreeSet<>();
        savedErrors22.addAll(Arrays.asList(savedError221, savedError222));
        Set<ExecutedScenario> savedScenarios2 = new TreeSet<>();
        savedScenarios2.addAll(Arrays.asList(savedScenario21, savedScenario22));
        Set<Run> savedRuns = new TreeSet<>();
        savedRuns.addAll(Arrays.asList(savedRun1, savedRun2));

        Execution previousExecution = mock(Execution.class);
        Run previousRun = mock(Run.class);
        Set<Run> previousRuns = new TreeSet<>();
        previousRuns.add(previousRun);
        ExecutedScenario previousScenario = mock(ExecutedScenario.class);
        Set<ExecutedScenario> previousExecutedScenarios = new TreeSet<>();
        previousExecutedScenarios.add(previousScenario);
        Error previousError1 = mock(Error.class);
        Error previousError2 = mock(Error.class);
        Set<Error> previousErrors = new TreeSet<>();
        previousErrors.addAll(Arrays.asList(previousError1, previousError2));

        Problem problem1 = mock(Problem.class);
        Problem problem2 = mock(Problem.class);
        Problem problem3 = mock(Problem.class);
        Set<Problem> problems = new TreeSet<>();
        problems.addAll(Arrays.asList(problem1, problem2, problem3));

        // When
        when(plannedIndexation.getExecutionFolder()).thenReturn(executionFile);
        when(plannedIndexation.getCycleDefinition()).thenReturn(cycleDefinition);
        when(executionFile.getAbsolutePath()).thenReturn("/execution/folder/location/in/disk");
        when(cycleDefinition.getProjectId()).thenReturn(1L);
        when(cycleDefinition.getBranch()).thenReturn("branch");
        when(cycleDefinition.getName()).thenReturn("cycle");
        when(executionFilesProcessorService.getExecution(plannedIndexation)).thenReturn(Optional.of(indexedExecution));
        when(executionRepository.save(indexedExecution)).thenReturn(savedExecution);
        when(indexedExecution.getJobUrl()).thenReturn("http://execution-url.build.org");
        when(savedExecution.getStatus()).thenReturn(JobStatus.DONE);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(1L,"http://execution-url.build.org", "/execution/folder/location/in/disk" + File.separator)).thenReturn(Optional.of(previousExecution));

        when(previousExecution.getRuns()).thenReturn(previousRuns);
        when(previousRun.getExecutedScenarios()).thenReturn(previousExecutedScenarios);
        when(previousScenario.getErrors()).thenReturn(previousErrors);
        when(previousError1.getId()).thenReturn(112L);
        when(previousError2.getId()).thenReturn(212L);

        when(savedExecution.getRuns()).thenReturn(savedRuns);
        when(savedRun1.getExecutedScenarios()).thenReturn(savedScenarios1);
        when(savedScenario11.getErrors()).thenReturn(savedErrors11);
        when(savedError111.getId()).thenReturn(111L);
        when(savedError112.getId()).thenReturn(112L);
        when(savedError113.getId()).thenReturn(113L);
        when(savedRun2.getExecutedScenarios()).thenReturn(savedScenarios2);
        when(savedScenario21.getErrors()).thenReturn(savedErrors21);
        when(savedError211.getId()).thenReturn(211L);
        when(savedError212.getId()).thenReturn(212L);
        when(savedScenario22.getErrors()).thenReturn(savedErrors22);
        when(savedError221.getId()).thenReturn(221L);
        when(savedError222.getId()).thenReturn(222L);

        when(errorRepository.autoAssignProblemsToNewErrors(anyLong(), anyList())).thenReturn(problems);

        // Then
        cut.indexExecution(plannedIndexation);

        verify(executionFilesProcessorService).getExecution(plannedIndexation);
        verify(executionRepository).save(indexedExecution);
        verify(executionRepository).findByProjectIdAndJobUrlOrJobLink(1L,"http://execution-url.build.org", "/execution/folder/location/in/disk" + File.separator);
        verify(errorRepository).autoAssignProblemsToNewErrors(1L, Arrays.asList(111L, 113L, 211L, 221L, 222L));
        verify(problemDenormalizationService).updateFirstAndLastSeenDateTimes(problems);
        verify(transactionService).doAfterCommit(any(Runnable.class));
    }

}
