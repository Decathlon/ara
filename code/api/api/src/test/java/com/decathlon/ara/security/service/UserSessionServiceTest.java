package com.decathlon.ara.security.service;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.loader.DemoLoaderConstants;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.service.user.strategy.UserAccountStrategy;
import com.decathlon.ara.security.service.user.strategy.select.UserStrategySelector;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserStrategySelector userStrategySelector;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private AuthorityMapper authorityMapper;

    @InjectMocks
    private UserSessionService userSessionService;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getCurrentUserRoleOnProject_returnEmptyOptional_whenProjectCodeIsNull(String projectCode) {
        // Given

        // When

        // Then
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getCurrentUserRoleOnProject_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getCurrentUserRoleOnProject_returnEmptyOptional_whenUserIsNotAuthenticated() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getCurrentUserRoleOnProject_returnEmptyOptional_whenNoAuthorityFound() {
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
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getCurrentUserRoleOnProject_returnAdminRole_whenProjectIsDemoProject() {
        // Given
        var projectCode = DemoLoaderConstants.DEMO_PROJECT_CODE;

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
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isPresent().contains(UserAccountScopeRole.ADMIN);
    }

    @Test
    void getCurrentUserRoleOnProject_returnRole_whenProjectFound() {
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
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isPresent().contains(UserAccountScopeRole.ADMIN);
    }

    @Test
    void getCurrentUserRoleOnProject_returnEmptyOptional_whenProjectNotFound() {
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
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getCurrentUserRoleOnProject_returnEmptyOptional_whenRoleFoundButWasUnknown() {
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
        var roleOnProject = userSessionService.getCurrentUserRoleOnProject(projectCode);
        assertThat(roleOnProject).isNotPresent();
    }

    @Test
    void getCurrentUserScopedProjectCodes_returnEmptyList_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var projectCodes = userSessionService.getCurrentUserScopedProjectCodes();
        assertThat(projectCodes).isEmpty();
    }

    @Test
    void getCurrentUserScopedProjectCodes_returnEmptyList_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var projectCodes = userSessionService.getCurrentUserScopedProjectCodes();
        assertThat(projectCodes).isEmpty();
    }

    @Test
    void getCurrentUserScopedProjectCodes_returnEmptyList_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var projectCodes = userSessionService.getCurrentUserScopedProjectCodes();
        assertThat(projectCodes).isEmpty();
    }

    @Test
    void getCurrentUserScopedProjectCodes_returnProjectCodesPlusDemoProjectCode_whenScopedProjectAuthoritiesFound() {
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
        var projectCodes = userSessionService.getCurrentUserScopedProjectCodes();
        assertThat(projectCodes)
                .containsExactlyInAnyOrder("project-code1", "project-code2", "project-code3", DemoLoaderConstants.DEMO_PROJECT_CODE)
                .hasSize(4);
    }

    @Test
    void getCurrentUserScopes_returnEmptyList_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var scopes = userSessionService.getCurrentUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getCurrentUserScopes_returnEmptyList_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var scopes = userSessionService.getCurrentUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getCurrentUserScopes_returnEmptyList_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var scopes = userSessionService.getCurrentUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getCurrentUserScopes_returnUserAccountScopesPlusDemoProjectScope_whenScopedProjectAuthoritiesFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authority4 = mock(GrantedAuthority.class);
        var authority5 = mock(GrantedAuthority.class);
        var authority6 = mock(GrantedAuthority.class);
        Collection authorities = Set.of(authority1, authority2, authority3, authority4, authority5, authority6);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn("USER_PROFILE:SUPER_ADMIN");
        when(authority4.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code1:MEMBER");
        when(authority2.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code2:mAiNtAiNeR");
        when(authority3.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code3:admin");
        when(authority6.getAuthority()).thenReturn("USER_PROJECT_SCOPE:project-code4:not-a-role");
        when(authority5.getAuthority()).thenReturn("unknown_authority");

        // Then
        var scopes = userSessionService.getCurrentUserScopes();
        assertThat(scopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple("project-code1", UserAccountScopeRole.MEMBER),
                        tuple("project-code2", UserAccountScopeRole.MAINTAINER),
                        tuple("project-code3", UserAccountScopeRole.ADMIN),
                        tuple(DemoLoaderConstants.DEMO_PROJECT_CODE, UserAccountScopeRole.ADMIN)
                );
    }

    @Test
    void getCurrentUserProfile_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var profile = userSessionService.getCurrentUserProfile();
        assertThat(profile).isNotPresent();
    }

    @Test
    void getCurrentUserProfile_returnEmptyOptional_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var profile = userSessionService.getCurrentUserProfile();
        assertThat(profile).isNotPresent();
    }

    @Test
    void getCurrentUserProfile_returnEmptyOptional_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var profile = userSessionService.getCurrentUserProfile();
        assertThat(profile).isNotPresent();
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountProfile.class)
    void getCurrentUserProfile_returnProfile_whenProfileFound(UserAccountProfile userProfile) {
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
        var profile = userSessionService.getCurrentUserProfile();
        assertThat(profile).isPresent().contains(userProfile);
    }

    @Test
    void getCurrentUserProfile_returnEmptyOptional_whenProfileFoundButUnknown() {
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
        var profile = userSessionService.getCurrentUserProfile();
        assertThat(profile).isNotPresent();
    }

    @Test
    void getCurrentUserProfile_returnEmptyOptional_whenProfileNotFound() {
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
        var profile = userSessionService.getCurrentUserProfile();
        assertThat(profile).isNotPresent();
    }

    @Test
    void refreshCurrentUserAuthorities_throwForbiddenException_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.refreshCurrentUserAuthorities());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void refreshCurrentUserAuthorities_throwForbiddenException_whenUserNotFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.refreshCurrentUserAuthorities());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void refreshCurrentUserAuthorities_updateAuthorities_whenUserFound() throws ForbiddenException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var userEntity = mock(UserEntity.class);

        var updatedAuthority1 = mock(GrantedAuthority.class);
        var updatedAuthority2 = mock(GrantedAuthority.class);
        var updatedAuthority3 = mock(GrantedAuthority.class);
        var updatedAuthorities = Set.of(updatedAuthority1, updatedAuthority2, updatedAuthority3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(userEntity));
        when(authorityMapper.getGrantedAuthoritiesFromUserEntity(userEntity)).thenReturn(updatedAuthorities);

        // Then
        userSessionService.refreshCurrentUserAuthorities();

        var authenticationToUpdateArgumentCaptor = ArgumentCaptor.forClass(OAuth2AuthenticationToken.class);
        verify(securityContext, times(1)).setAuthentication(authenticationToUpdateArgumentCaptor.capture());
        var authenticationToUpdate = authenticationToUpdateArgumentCaptor.getValue();
        assertThat(authenticationToUpdate.getPrincipal()).isSameAs(principal);
        assertThat(authenticationToUpdate.getAuthorities()).containsExactlyInAnyOrder(updatedAuthority1, updatedAuthority2, updatedAuthority3);
        assertThat(authenticationToUpdate.getAuthorizedClientRegistrationId()).isEqualTo(providerName);
    }
}
