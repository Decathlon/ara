package com.decathlon.ara.service;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.ProjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RootCauseRepository rootCauseRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private CommunicationService communicationService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void create_createProject_whenBusinessRulesAreValid() throws NotUniqueException {
        // Given
        var projectCode = "project-code";
        var projectName = "Project Name";
        var projectToCreate = mock(ProjectDTO.class);

        var currentUser = mock(User.class);

        var createdProjectId = 1L;

        var mappedProject = mock(Project.class);
        var savedProject = mock(Project.class);
        var createdProjectDTO = mock(ProjectDTO.class);

        // When
        when(projectToCreate.getCode()).thenReturn(projectCode);
        when(projectToCreate.getName()).thenReturn(projectName);
        when(projectRepository.findOneByCode(projectCode)).thenReturn(null);
        when(projectRepository.findOneByName(projectName)).thenReturn(null);
        when(projectMapper.getProjectFromProjectDTO(projectToCreate)).thenReturn(mappedProject);
        when(projectRepository.save(mappedProject)).thenReturn(savedProject);
        when(projectMapper.getProjectDTOFromProject(savedProject)).thenReturn(createdProjectDTO);
        when(createdProjectDTO.getId()).thenReturn(createdProjectId);

        // Then
        var createdProject = projectService.create(projectToCreate, currentUser);

        assertThat(createdProject).isSameAs(createdProjectDTO);

        verify(mappedProject).setCreationUser(currentUser);

        verify(communicationService, times(1)).initializeProject(mappedProject);
        ArgumentCaptor<List<RootCause>> rootCausesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(rootCauseRepository, times(1)).saveAll(rootCausesArgumentCaptor.capture());
        assertThat(rootCausesArgumentCaptor.getValue())
                .extracting("projectId", "name")
                .containsExactlyInAnyOrder(
                        tuple(createdProjectId, "Fragile test"),
                        tuple(createdProjectId, "Network issue"),
                        tuple(createdProjectId, "Regression"),
                        tuple(createdProjectId, "Test to update")
                );

        verify(mappedProject, never()).setUpdateUser(any());
        verify(mappedProject, never()).setUpdateDate(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void update_throwBadRequestException_whenProjectNewNameIsBlank(String newProjectName) {
        // Given
        var projectToUpdate = mock(ProjectDTO.class);
        var updateUser = mock(User.class);

        // When
        when(projectToUpdate.getName()).thenReturn(newProjectName);

        // Then
        assertThrows(BadRequestException.class, () -> projectService.update(projectToUpdate, updateUser));
    }

    @Test
    void update_throwNotFoundException_whenProjectNotFound() {
        // Given
        var projectCode = "project-code";
        var newProjectName = "New Project Name";

        var projectToUpdate = mock(ProjectDTO.class);
        var updateUser = mock(User.class);

        // When
        when(projectToUpdate.getName()).thenReturn(newProjectName);
        when(projectToUpdate.getCode()).thenReturn(projectCode);
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class, () -> projectService.update(projectToUpdate, updateUser));
    }

    @Test
    void update_throwNotUniqueException_whenProjectNewNameIsDifferentButNotUnique() {
        // Given
        var projectCode = "project-code";
        var newProjectName = "New Project Name";

        var projectToUpdate = mock(ProjectDTO.class);
        var updateUser = mock(User.class);

        var persistedProjectBeforeUpdate = mock(Project.class);
        var persistedProjectName = "Persisted Project Name";

        // When
        when(projectToUpdate.getCode()).thenReturn(projectCode);
        when(projectToUpdate.getName()).thenReturn(newProjectName);
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(persistedProjectBeforeUpdate));
        when(persistedProjectBeforeUpdate.getName()).thenReturn(persistedProjectName);
        when(projectRepository.existsByName(newProjectName)).thenReturn(true);

        // Then
        assertThrows(NotUniqueException.class, () -> projectService.update(projectToUpdate, updateUser));
    }

    @Test
    void update_updateProject_whenAllBusinessRulesAreValid() throws BadRequestException {
        // Given
        var projectCode = "project-code";
        var newProjectName = "New Project Name";
        var newProjectDescription = "A new project description";

        var projectToUpdate = mock(ProjectDTO.class);
        var updateUser = mock(User.class);

        var persistedProjectName = "Persisted Project Name";
        var persistedProjectDescription = "Persisted project description";
        var creationUser = mock(User.class);
        var persistedProjectBeforeUpdate = new Project(projectCode, persistedProjectName, creationUser);
        persistedProjectBeforeUpdate.setDescription(persistedProjectDescription);

        var savedProject = mock(Project.class);
        var mappedUpdatedProject = mock(ProjectDTO.class);

        // When
        when(projectToUpdate.getCode()).thenReturn(projectCode);
        when(projectToUpdate.getName()).thenReturn(newProjectName);
        when(projectToUpdate.getDescription()).thenReturn(newProjectDescription);
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(persistedProjectBeforeUpdate));
        when(projectRepository.existsByName(newProjectName)).thenReturn(false);
        when(projectRepository.save(persistedProjectBeforeUpdate)).thenReturn(savedProject);
        when(projectMapper.getProjectDTOFromProject(savedProject)).thenReturn(mappedUpdatedProject);

        // Then
        var now = ZonedDateTime.now();
        var oneSecondBeforeNow = now.minusSeconds(1);
        var oneSecondAfterNow = now.plusSeconds(1);

        var actualUpdatedProject = projectService.update(projectToUpdate, updateUser);

        assertThat(actualUpdatedProject).isSameAs(mappedUpdatedProject);

        var projectToPersistArgumentCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectToPersistArgumentCaptor.capture());
        var capturedProjectToPersist = projectToPersistArgumentCaptor.getValue();
        assertThat(capturedProjectToPersist)
                .extracting(
                        "code",
                        "name",
                        "description",
                        "creationUser",
                        "updateUser"
                )
                .contains(
                        projectCode,
                        newProjectName,
                        newProjectDescription,
                        creationUser,
                        updateUser
                );
        assertThat(capturedProjectToPersist.getUpdateDate()).isBetween(oneSecondBeforeNow, oneSecondAfterNow);
    }

    @Test
    void delete_deleteProject() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        projectService.delete(projectCode);
        verify(projectRepository, times(1)).deleteByCode(projectCode);
        verify(userSessionService, times(1)).refreshCurrentUserAuthorities();
    }

    @Test
    void createFromCode_createProjectFromCode_whenBusinessRulesAreValid() throws NotUniqueException {
        // Given
        var projectCode = "unknown-project_to   save         @&:1   )%*";
        var expectedProjectName = "Unknown Project To Save 1 (generated)";
        var createdProjectId = 1L;

        var mappedProject = mock(Project.class);
        var savedProject = mock(Project.class);
        var createdProjectDTO = mock(ProjectDTO.class);

        var currentUser = mock(User.class);

        // When
        when(projectRepository.findOneByCode(projectCode)).thenReturn(null);
        when(projectRepository.findOneByName(expectedProjectName)).thenReturn(null);
        when(projectMapper.getProjectFromProjectDTO(any(ProjectDTO.class))).thenReturn(mappedProject);
        when(projectRepository.save(mappedProject)).thenReturn(savedProject);
        when(projectMapper.getProjectDTOFromProject(savedProject)).thenReturn(createdProjectDTO);
        when(createdProjectDTO.getId()).thenReturn(createdProjectId);

        // Then
        var createdProject = projectService.createFromCode(projectCode, currentUser);

        assertThat(createdProject).isSameAs(createdProjectDTO);

        var mappedProjectArgumentCaptor = ArgumentCaptor.forClass(ProjectDTO.class);
        verify(projectMapper).getProjectFromProjectDTO(mappedProjectArgumentCaptor.capture());
        assertThat(mappedProjectArgumentCaptor.getValue().getName()).isEqualTo(expectedProjectName);

        verify(communicationService, times(1)).initializeProject(mappedProject);
        ArgumentCaptor<List<RootCause>> rootCausesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(rootCauseRepository, times(1)).saveAll(rootCausesArgumentCaptor.capture());
        assertThat(rootCausesArgumentCaptor.getValue())
                .extracting("projectId", "name")
                .containsExactlyInAnyOrder(
                        tuple(createdProjectId, "Fragile test"),
                        tuple(createdProjectId, "Network issue"),
                        tuple(createdProjectId, "Regression"),
                        tuple(createdProjectId, "Test to update")
                );
    }

    @Test
    void exists_returnFalse_whenProjectDoesNotExist() {
        // Given
        var projectCode = "project-code";

        // When
        when(projectRepository.existsByCode(projectCode)).thenReturn(false);

        // Then
        var exists = projectService.exists(projectCode);
        assertThat(exists).isFalse();
    }

    @Test
    void exists_returnTrue_whenProjectDoesExist() {
        // Given
        var projectCode = "project-code";

        // When
        when(projectRepository.existsByCode(projectCode)).thenReturn(true);

        // Then
        var exists = projectService.exists(projectCode);
        assertThat(exists).isTrue();
    }
}
