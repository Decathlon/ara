package com.decathlon.ara.service;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.security.service.user.UserService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.mapper.GenericMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private GenericMapper genericMapper;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

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
            value = UserEntity.UserEntityProfile.class,
            names = {"SUPER_ADMIN", "AUDITOR"}
    )
    void findAll_returnAllProjects_whenUserIsSuperAdminOrAuditor(UserEntity.UserEntityProfile profile) {
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
        when(genericMapper.mapCollection(allProjects, ProjectDTO.class)).thenReturn(mappedProjects);

        // Then
        var userProjects = projectService.findAll();
        assertThat(userProjects).isSameAs(mappedProjects);
        verify(projectRepository, never()).findByCodeInOrderByName(anyCollection());
    }

    @Test
    void findAll_returnOnlyProjectsInUserScope_whenUserIsScopedUser() {
        // Given
        var profile = UserEntity.UserEntityProfile.SCOPED_USER;
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
        when(genericMapper.mapCollection(scopedProjects, ProjectDTO.class)).thenReturn(mappedProjects);

        // Then
        var userProjects = projectService.findAll();
        assertThat(userProjects).isSameAs(mappedProjects);
        verify(projectRepository, never()).findAllByOrderByName();
    }

    @Test
    void delete_deleteProject() throws BadRequestException {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        projectService.delete(projectCode);
        verify(projectRepository, times(1)).deleteByCode(projectCode);
        verify(userService, times(1)).updateAuthoritiesFromLoggedInUser();
    }
}
