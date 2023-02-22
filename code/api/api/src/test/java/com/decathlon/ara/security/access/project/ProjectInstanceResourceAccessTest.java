package com.decathlon.ara.security.access.project;

import com.decathlon.ara.security.dto.permission.ResourcePermission;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.member.user.account.UserSessionService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.decathlon.ara.Entities.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectInstanceResourceAccessTest {

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectInstanceResourceAccess projectInstanceResourceAccess;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void isEnabled_returnFalse_whenProjectCodeIsBlank(String projectCode) {
        // Given
        var permission = mock(ResourcePermission.class);

        // When

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isFalse();
    }

    @Test
    void isEnabled_returnFalse_whenResourcePermissionIsNull() {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, null);
        assertThat(isEnabled).isFalse();
    }

    @Test
    void isEnabled_returnFalse_whenProjectDoesNotExist() {
        // Given
        var projectCode = "project-code";
        var permission = mock(ResourcePermission.class);

        // When
        when(projectService.exists(projectCode)).thenReturn(false);

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isFalse();
    }

    @Test
    void isEnabled_returnFalse_whenExceptionThrownWhileFetchingCurrentUserProfile() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var permission = mock(ResourcePermission.class);

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenThrow(new ForbiddenException(USER, "fetch profile"));

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isFalse();
    }

    @Test
    void isEnabled_returnTrue_whenUserIsSuperAdmin() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var permission = mock(ResourcePermission.class);
        var profile = UserAccountProfile.SUPER_ADMIN;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourcePermission.class,
            names = {"FETCH"}
    )
    void isEnabled_returnTrue_whenUserIsAuditorAndPermissionDoesNotAlterData(ResourcePermission permission) throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var profile = UserAccountProfile.AUDITOR;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourcePermission.class,
            names = {"ALTER"}
    )
    void isEnabled_returnFalse_whenUserIsAuditorAndPermissionAltersData(ResourcePermission permission) throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var profile = UserAccountProfile.AUDITOR;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isFalse();
    }

    @Test
    void isEnabled_returnFalse_whenUserIsScopedUserButProjectWasNotInItsScope() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var permission = mock(ResourcePermission.class);
        var profile = UserAccountProfile.SCOPED_USER;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);
        when(userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode)).thenReturn(Optional.empty());

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourcePermission.class
    )
    void isEnabled_returnTrue_whenUserIsAnAdminScopedUser(ResourcePermission permission) throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var profile = UserAccountProfile.SCOPED_USER;
        var role = UserAccountScopeRole.ADMIN;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);
        when(userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode)).thenReturn(Optional.of(role));

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourcePermission.class,
            names = {"FETCH"}
    )
    void isEnabled_returnTrue_whenUserIsAMaintainerScopedUserAndPermissionDoesNotAlterData(ResourcePermission permission) throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var profile = UserAccountProfile.SCOPED_USER;
        var role = UserAccountScopeRole.MAINTAINER;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);
        when(userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode)).thenReturn(Optional.of(role));

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourcePermission.class,
            names = {"ALTER"}
    )
    void isEnabled_returnFalse_whenUserIsAMaintainerScopedUserAndPermissionAltersData(ResourcePermission permission) throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var profile = UserAccountProfile.SCOPED_USER;
        var role = UserAccountScopeRole.MAINTAINER;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);
        when(userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode)).thenReturn(Optional.of(role));

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourcePermission.class,
            names = {"FETCH"}
    )
    void isEnabled_returnTrue_whenUserIsAMemberScopedUserAndPermissionDoesNotAlterData(ResourcePermission permission) throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var profile = UserAccountProfile.SCOPED_USER;
        var role = UserAccountScopeRole.MEMBER;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);
        when(userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode)).thenReturn(Optional.of(role));

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourcePermission.class,
            names = {"ALTER"}
    )
    void isEnabled_returnFalse_whenUserIsAMemberScopedUserAndPermissionAltersData(ResourcePermission permission) throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var profile = UserAccountProfile.SCOPED_USER;
        var role = UserAccountScopeRole.MEMBER;

        // When
        when(projectService.exists(projectCode)).thenReturn(true);
        when(userSessionService.getCurrentUserProfile()).thenReturn(profile);
        when(userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode)).thenReturn(Optional.of(role));

        // Then
        var isEnabled = projectInstanceResourceAccess.isEnabled(projectCode, permission);
        assertThat(isEnabled).isFalse();
    }
}
