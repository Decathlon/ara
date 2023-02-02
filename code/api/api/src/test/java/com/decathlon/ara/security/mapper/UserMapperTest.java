package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.role.ProjectRole;
import com.decathlon.ara.domain.security.member.user.User;
import com.decathlon.ara.domain.security.member.user.UserProfile;
import com.decathlon.ara.domain.security.member.user.UserScope;
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
    @EnumSource(value = UserProfile.class)
    void getUserAccountFromUser_returnUserAccount_whenUserIsNotNull(UserProfile userProfile) {
        // Given
        var userToConvert = mock(User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var defaultProject = mock(Project.class);
        var defaultProjectCode = "default-project-code";

        var scope1 = mock(UserScope.class);
        var role1 = ProjectRole.ADMIN;
        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";

        var scope2 = mock(UserScope.class);
        var role2 = ProjectRole.MAINTAINER;
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";

        var scope3 = mock(UserScope.class);
        var role3 = ProjectRole.MEMBER;
        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";

        var scopes = List.of(scope1, scope2, scope3);

        // When
        when(userToConvert.getLogin()).thenReturn(userLogin);
        when(userToConvert.getProviderName()).thenReturn(providerName);
        when(userToConvert.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(userToConvert.getLastName()).thenReturn(Optional.of(userLastName));
        when(userToConvert.getEmail()).thenReturn(Optional.of(userEmail));
        when(userToConvert.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(userToConvert.getDefaultProject()).thenReturn(Optional.of(defaultProject));
        when(defaultProject.getCode()).thenReturn(defaultProjectCode);

        when(userToConvert.getProfile()).thenReturn(userProfile);

        when(userToConvert.getScopes()).thenReturn(scopes);
        when(scope1.getRole()).thenReturn(role1);
        when(scope1.getProject()).thenReturn(project1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(scope2.getRole()).thenReturn(role2);
        when(scope2.getProject()).thenReturn(project2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(scope3.getRole()).thenReturn(role3);
        when(scope3.getProject()).thenReturn(project3);
        when(project3.getCode()).thenReturn(projectCode3);

        // Then
        var convertedUser = userMapper.getUserAccountFromUser(userToConvert);
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
                        UserAccountProfile.valueOf(userProfile.name()),
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
