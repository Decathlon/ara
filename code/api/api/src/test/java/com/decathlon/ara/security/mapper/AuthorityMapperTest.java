package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.UserSessionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorityMapperTest {

    @InjectMocks
    private AuthorityMapper authorityMapper;

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void getGrantedAuthoritiesFromUserEntity_returnProfileAndScopedAuthorities_whenUserEntityHasProfileAndScopes(UserEntity.UserEntityProfile profile) {
        // Given
        var persistedUser = mock(UserEntity.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var role1 = UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN;
        var scope1 = mock(UserEntityRoleOnProject.class);

        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var role2 = UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER;
        var scope2 = mock(UserEntityRoleOnProject.class);

        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";
        var role3 = UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER;
        var scope3 = mock(UserEntityRoleOnProject.class);

        var scopes = List.of(scope1, scope2, scope3);

        // When
        when(persistedUser.getProfile()).thenReturn(profile);
        when(persistedUser.getRolesOnProjectWhenScopedUser()).thenReturn(scopes);
        when(scope1.getProject()).thenReturn(project1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(scope1.getRole()).thenReturn(role1);
        when(scope2.getProject()).thenReturn(project2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(scope2.getRole()).thenReturn(role2);
        when(scope3.getProject()).thenReturn(project3);
        when(project3.getCode()).thenReturn(projectCode3);
        when(scope3.getRole()).thenReturn(role3);

        // Then
        Set<GrantedAuthority> authorities = authorityMapper.getGrantedAuthoritiesFromUserEntity(persistedUser);
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        String.format("%s%s", UserSessionService.AUTHORITY_USER_PROFILE_PREFIX, profile),
                        String.format("%s%s:ADMIN", UserSessionService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode1),
                        String.format("%s%s:MAINTAINER", UserSessionService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode2),
                        String.format("%s%s:MEMBER", UserSessionService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode3)
                );
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountProfile.class)
    void getGrantedAuthoritiesFromUserAccount_returnProfileAndScopedAuthorities_whenUserAccountHasProfileAndScopes(UserAccountProfile profile) {
        // Given
        var userAccount = mock(UserAccount.class);

        var projectCode1 = "project-code1";
        var role1 = UserAccountScopeRole.ADMIN;
        var scope1 = mock(UserAccountScope.class);

        var projectCode2 = "project-code2";
        var role2 = UserAccountScopeRole.MAINTAINER;
        var scope2 = mock(UserAccountScope.class);

        var projectCode3 = "project-code3";
        var role3 = UserAccountScopeRole.MEMBER;
        var scope3 = mock(UserAccountScope.class);

        var scopes = List.of(scope1, scope2, scope3);

        // When
        when(userAccount.getProfile()).thenReturn(profile);
        when(userAccount.getScopes()).thenReturn(scopes);
        when(scope1.getProject()).thenReturn(projectCode1);
        when(scope1.getRole()).thenReturn(role1);
        when(scope2.getProject()).thenReturn(projectCode2);
        when(scope2.getRole()).thenReturn(role2);
        when(scope3.getProject()).thenReturn(projectCode3);
        when(scope3.getRole()).thenReturn(role3);

        // Then
        Set<GrantedAuthority> authorities = authorityMapper.getGrantedAuthoritiesFromUserAccount(userAccount);
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        String.format("%s%s", UserSessionService.AUTHORITY_USER_PROFILE_PREFIX, profile),
                        String.format("%s%s:ADMIN", UserSessionService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode1),
                        String.format("%s%s:MAINTAINER", UserSessionService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode2),
                        String.format("%s%s:MEMBER", UserSessionService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode3)
                );
    }
}
