package com.decathlon.ara.scheduler.purge;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.purge.service.PurgeService;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import com.decathlon.ara.service.util.DateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurgeServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private SettingService settingService;

    @Mock
    private DateService dateService;

    @InjectMocks
    private PurgeService purgeService;

    @Test
    void purgeExecutionsByProjectCode_doNothing_whenProjectCodeUnknown() throws NotFoundException {
        // Given
        var projectCode = "unknown-project-code";

        // When
        when(projectService.toId(projectCode)).thenThrow(new NotFoundException(String.format("project '%s' not found", projectCode), "test"));

        // Then
        purgeService.purgeExecutionsByProjectCode(projectCode);
        verify(executionRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    void purgeExecutionsByProjectCode_doNothing_whenValueSettingNotFound() throws NotFoundException {
        // Given
        var projectCode = "project-code";
        var projectId = 1L;
        String value = null;

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value);

        // Then
        purgeService.purgeExecutionsByProjectCode(projectCode);
        verify(executionRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    void purgeExecutionsByProjectCode_doNothing_whenValueSettingIsNotANumber() throws NotFoundException {
        // Given
        var projectCode = "project-code";
        var projectId = 1L;
        var value = "not-a-number";

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value);

        // Then
        purgeService.purgeExecutionsByProjectCode(projectCode);
        verify(executionRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    void purgeExecutionsByProjectCode_doNothing_whenValueSettingIsNegative() throws NotFoundException {
        // Given
        var projectCode = "project-code";
        var projectId = 1L;
        var value = "-1";

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value);

        // Then
        purgeService.purgeExecutionsByProjectCode(projectCode);
        verify(executionRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    void purgeExecutionsByProjectCode_doNothing_whenTypeSettingIsNotFound() throws NotFoundException {
        // Given
        var projectCode = "project-code";
        var projectId = 1L;
        var value = "3";
        String type = null;

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_TYPE)).thenReturn(type);

        // Then
        purgeService.purgeExecutionsByProjectCode(projectCode);
        verify(executionRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    void purgeExecutionsByProjectCode_doNothing_whenValueOrTypeSettingIsIncorrect() throws NotFoundException {
        // Given
        var projectCode = "project-code";
        var projectId = 1L;
        var value = "10";
        var type = "incorrectDurationType";

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_TYPE)).thenReturn(type);
        when(dateService.getTodayDateMinusPeriod(10, type)).thenReturn(Optional.empty());

        // Then
        purgeService.purgeExecutionsByProjectCode(projectCode);
        verify(executionRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    void purgeExecutionsByProjectCode_purgeOlderExecutions_whenValueAndTypeSettingsFoundAndCorrect() throws NotFoundException {
        // Given
        var projectCode = "project-code";
        var projectId = 1L;
        var value = "3";
        var type = "anyDurationType";
        var startDate = mock(Date.class);

        Execution execution1 = mock(Execution.class);
        Long executionId1 = 1L;
        Execution execution2 = mock(Execution.class);
        Long executionId2 = 2L;
        Execution execution3 = mock(Execution.class);
        Long executionId3 = 3L;
        List<Execution> executionsToDelete = List.of(execution1, execution2, execution3);

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value);
        when(settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_TYPE)).thenReturn(type);
        when(dateService.getTodayDateMinusPeriod(3, type)).thenReturn(Optional.of(startDate));
        when(executionRepository.findByCycleDefinitionProjectIdAndTestDateTimeBefore(projectId, startDate)).thenReturn(executionsToDelete);
        when(execution1.getId()).thenReturn(executionId1);
        when(execution2.getId()).thenReturn(executionId2);
        when(execution3.getId()).thenReturn(executionId3);

        // Then
        purgeService.purgeExecutionsByProjectCode(projectCode);
        var executionIdsToDeleteArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executionRepository).deleteAllByIdInBatch(executionIdsToDeleteArgumentCaptor.capture());
        assertThat(executionIdsToDeleteArgumentCaptor.getValue()).containsExactlyInAnyOrder(executionId1, executionId2, executionId3);
    }

    @Test
    void purgeAllProjects_purgeOlderExecutions_whenValueAndTypeSettingsFoundAndCorrect() {
        // Given

        // Project 1
        var project1 = mock(ProjectDTO.class);
        var projectCode1 = "project-code-1";
        var projectId1 = 1L;
        var value1 = "1";
        var type1 = "anyDurationType1";
        var startDate1 = mock(Date.class);
        Execution executionToDelete11 = mock(Execution.class);
        Long executionId11 = 11L;
        List<Execution> executionsToDelete1 = List.of(executionToDelete11);

        // Project 2
        var project2 = mock(ProjectDTO.class);
        var projectCode2 = "project-code-2";
        var projectId2 = 2L;
        var value2 = "2";
        var type2 = "anyDurationType2";
        var startDate2 = mock(Date.class);
        Execution executionToDelete21 = mock(Execution.class);
        Execution executionToDelete22 = mock(Execution.class);
        Long executionId21 = 21L;
        Long executionId22 = 22L;
        List<Execution> executionsToDelete2 = List.of(executionToDelete21, executionToDelete22);

        // Project 3
        var project3 = mock(ProjectDTO.class);
        var projectCode3 = "project-code-3";
        var projectId3 = 3L;
        var value3 = "3";
        var type3 = "anyDurationType3";
        var startDate3 = mock(Date.class);
        Execution executionToDelete31 = mock(Execution.class);
        Execution executionToDelete32 = mock(Execution.class);
        Execution executionToDelete33 = mock(Execution.class);
        Long executionId31 = 31L;
        Long executionId32 = 32L;
        Long executionId33 = 33L;
        List<Execution> executionsToDelete3 = List.of(executionToDelete31, executionToDelete32, executionToDelete33);

        // When
        when(projectService.findAll()).thenReturn(List.of(project1, project2, project3));

        when(project1.getId()).thenReturn(projectId1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(settingService.get(projectId1, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value1);
        when(settingService.get(projectId1, Settings.EXECUTION_PURGE_DURATION_TYPE)).thenReturn(type1);
        when(dateService.getTodayDateMinusPeriod(1, type1)).thenReturn(Optional.of(startDate1));
        when(executionRepository.findByCycleDefinitionProjectIdAndTestDateTimeBefore(projectId1, startDate1)).thenReturn(executionsToDelete1);
        when(executionToDelete11.getId()).thenReturn(executionId11);

        when(project2.getId()).thenReturn(projectId2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(settingService.get(projectId2, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value2);
        when(settingService.get(projectId2, Settings.EXECUTION_PURGE_DURATION_TYPE)).thenReturn(type2);
        when(dateService.getTodayDateMinusPeriod(2, type2)).thenReturn(Optional.of(startDate2));
        when(executionRepository.findByCycleDefinitionProjectIdAndTestDateTimeBefore(projectId2, startDate2)).thenReturn(executionsToDelete2);
        when(executionToDelete21.getId()).thenReturn(executionId21);
        when(executionToDelete22.getId()).thenReturn(executionId22);

        when(project3.getId()).thenReturn(projectId3);
        when(project3.getCode()).thenReturn(projectCode3);
        when(settingService.get(projectId3, Settings.EXECUTION_PURGE_DURATION_VALUE)).thenReturn(value3);
        when(settingService.get(projectId3, Settings.EXECUTION_PURGE_DURATION_TYPE)).thenReturn(type3);
        when(dateService.getTodayDateMinusPeriod(3, type3)).thenReturn(Optional.of(startDate3));
        when(executionRepository.findByCycleDefinitionProjectIdAndTestDateTimeBefore(projectId3, startDate3)).thenReturn(executionsToDelete3);
        when(executionToDelete31.getId()).thenReturn(executionId31);
        when(executionToDelete32.getId()).thenReturn(executionId32);
        when(executionToDelete33.getId()).thenReturn(executionId33);

        // Then
        purgeService.purgeAllProjects();
        var executionIdsToDeleteArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executionRepository, times(3)).deleteAllByIdInBatch(executionIdsToDeleteArgumentCaptor.capture());
        assertThat(executionIdsToDeleteArgumentCaptor.getAllValues())
                .hasSize(3)
                .containsExactlyInAnyOrder(List.of(executionId11), List.of(executionId21, executionId22), List.of(executionId31, executionId32, executionId33));
    }
}
