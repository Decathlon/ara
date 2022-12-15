package com.decathlon.ara.security.dto.user;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.AuthorityService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountTest {

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void UserAccountConstructor_createUserAccountWithoutAttribute_whenUserAttributesEmpty(UserEntity.UserEntityProfile userEntityProfile) {
        // Given
        var userAttributes = new HashMap<String, String>();
        var userEntity = mock(UserEntity.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

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
        when(userEntity.getLogin()).thenReturn(userLogin);
        when(userEntity.getProviderName()).thenReturn(providerName);
        when(userEntity.getProfile()).thenReturn(userEntityProfile);

        when(userEntity.getRolesOnProjectWhenScopedUser()).thenReturn(userEntityRoles);
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
        var userAccount = new UserAccount(userAttributes, userEntity);
        assertThat(userAccount)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(userEntityProfile.name()),
                        null,
                        null,
                        null,
                        null
                );
        assertThat(userAccount.getScopes())
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.ADMIN),
                        tuple(projectCode2, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode3, UserAccountScopeRole.MEMBER)
                );
    }

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void UserAccountConstructor_createUserAccountWithAttributes_whenUserAttributesIsNotEmpty(UserEntity.UserEntityProfile userEntityProfile) {
        // Given
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";
        var userAttributes = Map.ofEntries(
                entry(StandardClaimNames.GIVEN_NAME, userFirstName),
                entry(StandardClaimNames.FAMILY_NAME, userLastName),
                entry(StandardClaimNames.EMAIL, userEmail),
                entry(StandardClaimNames.PICTURE, userPictureUrl)
        );
        var userEntity = mock(UserEntity.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

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
        when(userEntity.getLogin()).thenReturn(userLogin);
        when(userEntity.getProviderName()).thenReturn(providerName);
        when(userEntity.getProfile()).thenReturn(userEntityProfile);

        when(userEntity.getRolesOnProjectWhenScopedUser()).thenReturn(userEntityRoles);
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
        var userAccount = new UserAccount(userAttributes, userEntity);
        assertThat(userAccount)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(userEntityProfile.name()),
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
        assertThat(userAccount.getScopes())
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.ADMIN),
                        tuple(projectCode2, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode3, UserAccountScopeRole.MEMBER)
                );
    }

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void getMatchingAuthorities_returnProfileAndScopedAuthorities_whenUserAccountHasProfileAndScopes(UserEntity.UserEntityProfile userEntityProfile) {
        // Given
        var userEntity = mock(UserEntity.class);

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
        when(userEntity.getProfile()).thenReturn(userEntityProfile);
        when(userEntity.getRolesOnProjectWhenScopedUser()).thenReturn(userEntityRoles);

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
        Set<GrantedAuthority> authorities = new UserAccount(new HashMap<>(), userEntity).getMatchingAuthorities();
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        String.format("%s%s", AuthorityService.AUTHORITY_USER_PROFILE_PREFIX, userEntityProfile),
                        String.format("%s%s:ADMIN", AuthorityService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode1),
                        String.format("%s%s:MAINTAINER", AuthorityService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode2),
                        String.format("%s%s:MEMBER", AuthorityService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode3)
                );
    }
}
