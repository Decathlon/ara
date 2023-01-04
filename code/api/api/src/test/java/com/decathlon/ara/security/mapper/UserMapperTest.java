package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void getUserAccountFromPersistedUser_returnUserAccount_whenPersistedUserIsNotNull(UserEntity.UserEntityProfile persistedProfile) {
        // Given
        var userToConvert = mock(UserEntity.class);
        var userLogin = "user-login";
        var providerName = "provider-name";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var defaultProject = mock(Project.class);
        var defaultProjectCode = "default-project-code";

        var userEntityRole1 = mock(UserEntityRoleOnProject.class);
        var userEntityScope1 = UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN;
        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";

        var userEntityRole2 = mock(UserEntityRoleOnProject.class);
        var userEntityScope2 = UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER;
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";

        var userEntityRole3 = mock(UserEntityRoleOnProject.class);
        var userEntityScope3 = UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER;
        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";

        var userEntityRoles = List.of(userEntityRole1, userEntityRole2, userEntityRole3);

        // When
        when(userToConvert.getLogin()).thenReturn(userLogin);
        when(userToConvert.getProviderName()).thenReturn(providerName);
        when(userToConvert.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(userToConvert.getLastName()).thenReturn(Optional.of(userLastName));
        when(userToConvert.getEmail()).thenReturn(Optional.of(userEmail));
        when(userToConvert.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(userToConvert.getDefaultProject()).thenReturn(Optional.of(defaultProject));
        when(defaultProject.getCode()).thenReturn(defaultProjectCode);

        when(userToConvert.getProfile()).thenReturn(persistedProfile);

        when(userToConvert.getRolesOnProjectWhenScopedUser()).thenReturn(userEntityRoles);
        when(userEntityRole1.getRole()).thenReturn(userEntityScope1);
        when(userEntityRole1.getProject()).thenReturn(project1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(userEntityRole2.getRole()).thenReturn(userEntityScope2);
        when(userEntityRole2.getProject()).thenReturn(project2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(userEntityRole3.getRole()).thenReturn(userEntityScope3);
        when(userEntityRole3.getProject()).thenReturn(project3);
        when(project3.getCode()).thenReturn(projectCode3);

        // Then
        var convertedUser = userMapper.getUserAccountFromPersistedUser(userToConvert);
        assertThat(convertedUser)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "defaultProjectCode",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .containsExactly(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(persistedProfile.name()),
                        defaultProjectCode,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
        assertThat(convertedUser.getScopes())
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.ADMIN),
                        tuple(projectCode2, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode3, UserAccountScopeRole.MEMBER)
                );
    }
}
