package com.decathlon.ara.security.service;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorityServiceTest {

    @InjectMocks
    private AuthorityService authorityService;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getRoleOnProject_returnEmptyOptional_whenProjectCodeIsNull(String projectCode) {
        // Given

        // When

        // Then
        var roleOnProject = authorityService.getRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getRoleOnProject_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var roleOnProject = authorityService.getRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getRoleOnProject_returnEmptyOptional_whenUserIsNotAuthenticated() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var roleOnProject = authorityService.getRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getRoleOnProject_returnEmptyOptional_whenNoAuthorityFound() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var roleOnProject = authorityService.getRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getRoleOnProject_returnRole_whenProjectFound() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority1, authority2, authority3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code1:MEMBER");
        when(authority2.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code2:MAINTAINER");
        when(authority3.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code:ADMIN");

        // Then
        var roleOnProject = authorityService.getRoleOnProject(projectCode);
        assertThat(roleOnProject).isPresent().contains(UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN);
    }

    @Test
    void getRoleOnProject_returnEmptyOptional_whenProjectNotFound() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority1, authority2, authority3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code1:MEMBER");
        when(authority2.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code2:MAINTAINER");
        when(authority3.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code3:ADMIN");

        // Then
        var roleOnProject = authorityService.getRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getRoleOnProject_returnEmptyOptional_whenRoleFoundButWasUnknown() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority1, authority2, authority3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code1:MEMBER");
        when(authority2.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code2:MAINTAINER");
        when(authority3.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code:UNKNOWN_ROLE");

        // Then
        var roleOnProject = authorityService.getRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getScopedProjectCodes_returnEmptyList_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var projectCodes = authorityService.getScopedProjectCodes();
        assertThat(projectCodes).isEmpty();
    }

    @Test
    void getScopedProjectCodes_returnEmptyList_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var projectCodes = authorityService.getScopedProjectCodes();
        assertThat(projectCodes).isEmpty();
    }

    @Test
    void getScopedProjectCodes_returnEmptyList_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var projectCodes = authorityService.getScopedProjectCodes();
        assertThat(projectCodes).isEmpty();
    }

    @Test
    void getScopedProjectCodes_returnProjectCodes_whenScopedProjectAuthoritiesFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authority4 = mock(GrantedAuthority.class);
        var authority5 = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority1, authority2, authority3, authority4, authority5);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn("USER_PROFILE:SUPER_ADMIN");
        when(authority2.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code2:MAINTAINER");
        when(authority3.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code3:ADMIN");
        when(authority4.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code1:MEMBER");
        when(authority5.getAuthority()).thenReturn("unknown_authority");

        // Then
        var projectCodes = authorityService.getScopedProjectCodes();
        assertThat(projectCodes)
                .containsExactly("project-code1", "project-code2", "project-code3")
                .hasSize(3);
    }

    @Test
    void getLoggedInUserScopes_returnEmptyList_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var scopes = authorityService.getLoggedInUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getLoggedInUserScopes_returnEmptyList_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var scopes = authorityService.getLoggedInUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getLoggedInUserScopes_returnEmptyList_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var scopes = authorityService.getLoggedInUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getLoggedInUserScopes_returnLoggedInUserScopes_whenScopedProjectAuthoritiesFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authority4 = mock(GrantedAuthority.class);
        var authority5 = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority1, authority2, authority3, authority4, authority5);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn("USER_PROFILE:SUPER_ADMIN");
        when(authority2.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code2:role2");
        when(authority3.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code3:role3");
        when(authority4.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code1:role1");
        when(authority5.getAuthority()).thenReturn("unknown_authority");

        // Then
        var scopes = authorityService.getLoggedInUserScopes();
        assertThat(scopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple("project-code1", "role1"),
                        tuple("project-code2", "role2"),
                        tuple("project-code3", "role3")
                );
    }

    @Test
    void getProfile_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var profile = authorityService.getProfile();
        assertThat(profile).isNotPresent();
    }

    @Test
    void getProfile_returnEmptyOptional_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var profile = authorityService.getProfile();
        assertThat(profile).isNotPresent();
    }

    @Test
    void getProfile_returnEmptyOptional_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var profile = authorityService.getProfile();
        assertThat(profile).isNotPresent();
    }

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void getProfile_returnProfile_whenProfileFound(UserEntity.UserEntityProfile userProfile) {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority.getAuthority()).thenReturn("USER_PROFILE:" + userProfile.name());

        // Then
        var profile = authorityService.getProfile();
        assertThat(profile).isPresent().contains(userProfile);
    }

    @Test
    void getProfile_returnEmptyOptional_whenProfileFoundButUnknown() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority.getAuthority()).thenReturn("USER_PROFILE:UNKNOWN_PROFILE");

        // Then
        var profile = authorityService.getProfile();
        assertThat(profile).isNotPresent();
    }

    @Test
    void getProfile_returnEmptyOptional_whenProfileNotFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority.getAuthority()).thenReturn("any_other_authority");

        // Then
        var profile = authorityService.getProfile();
        assertThat(profile).isNotPresent();
    }
}
