package com.decathlon.ara.service;

import com.decathlon.ara.domain.Communication;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.ProjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
    private AuthorityService authorityService;

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

        var currentUserEntity = mock(UserEntity.class);

        var createdProjectId = 1L;

        var mappedProject = mock(Project.class);
        var savedProject = mock(Project.class);
        var createdProjectDTO = mock(ProjectDTO.class);

        // When
        when(projectToCreate.getCode()).thenReturn(projectCode);
        when(projectToCreate.getName()).thenReturn(projectName);
        when(projectRepository.findOneByCode(projectCode)).thenReturn(null);
        when(projectRepository.findOneByName(projectName)).thenReturn(null);
        when(projectMapper.getProjectEntityFromProjectDTO(projectToCreate)).thenReturn(mappedProject);
        when(projectRepository.save(mappedProject)).thenReturn(savedProject);
        when(projectMapper.getProjectDTOFromProjectEntity(savedProject)).thenReturn(createdProjectDTO);
        when(createdProjectDTO.getId()).thenReturn(createdProjectId);

        // Then
        var createdProject = projectService.create(projectToCreate, currentUserEntity);

        assertThat(createdProject).isSameAs(createdProjectDTO);

        verify(mappedProject).setCreationUser(currentUserEntity);

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

    @Test
    void update_throwNotFoundException_whenProjectNotFound() {
        // Given
        var projectId = 1L;

        var projectToUpdate = mock(ProjectDTO.class);

        var currentUserEntity = mock(UserEntity.class);

        // When
        when(projectToUpdate.getId()).thenReturn(projectId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class, () -> projectService.update(projectToUpdate, currentUserEntity));
    }

    @Test
    void update_updateProject_whenBusinessRulesAreValid() throws BadRequestException {
        // Given
        var projectId = 1L;
        var projectCode = "project-code";
        var projectName = "Project Name";

        var projectToUpdate = mock(ProjectDTO.class);

        var persistedProjectBeforeUpdate = mock(Project.class);

        var currentUserEntity = mock(UserEntity.class);

        var mappedProject = mock(Project.class);
        var savedProject = mock(Project.class);
        var updatedProjectDTO = mock(ProjectDTO.class);

        var communication1 = mock(Communication.class);
        var communication2 = mock(Communication.class);
        var communication3 = mock(Communication.class);
        var communications = List.of(communication1, communication2, communication3);

        // When
        when(projectToUpdate.getId()).thenReturn(projectId);
        when(projectToUpdate.getCode()).thenReturn(projectCode);
        when(projectToUpdate.getName()).thenReturn(projectName);
        when(persistedProjectBeforeUpdate.getId()).thenReturn(projectId);
        when(persistedProjectBeforeUpdate.getCommunications()).thenReturn(communications);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(persistedProjectBeforeUpdate));
        when(projectRepository.findOneByCode(projectCode)).thenReturn(persistedProjectBeforeUpdate);
        when(projectRepository.findOneByName(projectName)).thenReturn(persistedProjectBeforeUpdate);
        when(projectMapper.getProjectEntityFromProjectDTO(projectToUpdate)).thenReturn(mappedProject);
        when(projectRepository.save(mappedProject)).thenReturn(savedProject);
        when(projectMapper.getProjectDTOFromProjectEntity(savedProject)).thenReturn(updatedProjectDTO);

        // Then
        var now = ZonedDateTime.now();
        var oneSecondBeforeNow = now.minusSeconds(1);
        var oneSecondAfterNow = now.plusSeconds(1);

        var updatedProject = projectService.update(projectToUpdate, currentUserEntity);

        assertThat(updatedProject).isSameAs(updatedProjectDTO);

        ArgumentCaptor<List<Communication>> communicationsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mappedProject).setCommunications(communicationsArgumentCaptor.capture());
        assertThat(communicationsArgumentCaptor.getValue()).isSameAs(communications);

        var updateDateArgumentCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        verify(mappedProject).setUpdateDate(updateDateArgumentCaptor.capture());
        assertThat(updateDateArgumentCaptor.getValue()).isBetween(oneSecondBeforeNow, oneSecondAfterNow);

        verify(mappedProject).setUpdateUser(currentUserEntity);

        verify(mappedProject, never()).setCreationUser(any());
    }

    @Test
    void findAll_returnEmptyList_whenUserHasNoProfile() {
        // Given

        // When
        when(authorityService.getProfile()).thenReturn(Optional.empty());

        // Then
        var userProjects = projectService.findAll();
        assertThat(userProjects).isEmpty();
        verify(projectRepository, never()).findAllByOrderByName();
        verify(projectRepository, never()).findByCodeInOrderByName(anyCollection());
    }

    @ParameterizedTest
    @EnumSource(
            value = UserAccountProfile.class,
            names = {"SUPER_ADMIN", "AUDITOR"}
    )
    void findAll_returnAllProjects_whenUserIsSuperAdminOrAuditor(UserAccountProfile profile) {
        // Given
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var allProjects = List.of(project1, project2, project3);

        var mappedProject1 = mock(ProjectDTO.class);
        var mappedProject2 = mock(ProjectDTO.class);
        var mappedProject3 = mock(ProjectDTO.class);
        var mappedProjects = List.of(mappedProject1, mappedProject2, mappedProject3);

        // When
        when(authorityService.getProfile()).thenReturn(Optional.of(profile));
        when(projectRepository.findAllByOrderByName()).thenReturn(allProjects);
        when(projectMapper.getProjectDTOFromProjectEntity(project1)).thenReturn(mappedProject1);
        when(projectMapper.getProjectDTOFromProjectEntity(project2)).thenReturn(mappedProject2);
        when(projectMapper.getProjectDTOFromProjectEntity(project3)).thenReturn(mappedProject3);

        // Then
        var userProjects = projectService.findAll();
        assertThat(userProjects).containsExactlyInAnyOrderElementsOf(mappedProjects);
        verify(projectRepository, never()).findByCodeInOrderByName(anyCollection());
    }

    @Test
    void findAll_returnOnlyProjectsInUserScope_whenUserIsScopedUser() {
        // Given
        var profile = UserAccountProfile.SCOPED_USER;
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var scopedProjects = List.of(project1, project2, project3);

        var projectCode1 = "project-code-1";
        var projectCode2 = "project-code-2";
        var projectCode3 = "project-code-3";
        var projectCodes = List.of(projectCode1, projectCode2, projectCode3);

        var mappedProject1 = mock(ProjectDTO.class);
        var mappedProject2 = mock(ProjectDTO.class);
        var mappedProject3 = mock(ProjectDTO.class);
        var mappedProjects = List.of(mappedProject1, mappedProject2, mappedProject3);

        // When
        when(authorityService.getProfile()).thenReturn(Optional.of(profile));
        when(authorityService.getScopedProjectCodes()).thenReturn(projectCodes);
        when(projectRepository.findByCodeInOrderByName(projectCodes)).thenReturn(scopedProjects);
        when(projectMapper.getProjectDTOFromProjectEntity(project1)).thenReturn(mappedProject1);
        when(projectMapper.getProjectDTOFromProjectEntity(project2)).thenReturn(mappedProject2);
        when(projectMapper.getProjectDTOFromProjectEntity(project3)).thenReturn(mappedProject3);

        // Then
        var userProjects = projectService.findAll();
        assertThat(userProjects).containsExactlyInAnyOrderElementsOf(mappedProjects);
        verify(projectRepository, never()).findAllByOrderByName();
    }

    @Test
    void delete_deleteProject() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        projectService.delete(projectCode);
        verify(projectRepository, times(1)).deleteByCode(projectCode);
        verify(authorityService, times(1)).refreshCurrentUserAccountAuthorities();
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

        var currentUserEntity = mock(UserEntity.class);

        // When
        when(projectRepository.findOneByCode(projectCode)).thenReturn(null);
        when(projectRepository.findOneByName(expectedProjectName)).thenReturn(null);
        when(projectMapper.getProjectEntityFromProjectDTO(any(ProjectDTO.class))).thenReturn(mappedProject);
        when(projectRepository.save(mappedProject)).thenReturn(savedProject);
        when(projectMapper.getProjectDTOFromProjectEntity(savedProject)).thenReturn(createdProjectDTO);
        when(createdProjectDTO.getId()).thenReturn(createdProjectId);

        // Then
        var createdProject = projectService.createFromCode(projectCode, currentUserEntity);

        assertThat(createdProject).isSameAs(createdProjectDTO);

        var mappedProjectArgumentCaptor = ArgumentCaptor.forClass(ProjectDTO.class);
        verify(projectMapper).getProjectEntityFromProjectDTO(mappedProjectArgumentCaptor.capture());
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
