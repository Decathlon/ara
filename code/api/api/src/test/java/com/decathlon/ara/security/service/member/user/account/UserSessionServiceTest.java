package com.decathlon.ara.security.service.member.user.account;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.repository.security.member.user.account.UserRepository;
import com.decathlon.ara.security.dto.authentication.user.AuthenticatedOAuth2User;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.mapper.AuthenticationMapper;
import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.mapper.UserMapper;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthorityMapper authorityMapper;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @InjectMocks
    private UserSessionService userSessionService;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getCurrentUserAccountScopeRoleFromProjectCode_returnEmptyOptional_whenProjectCodeIsNull(String projectCode) {
        // Given

        // When

        // Then
        var role = userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode);
        assertThat(role).isNotPresent();
    }

    @Test
    void getCurrentUserAccountScopeRoleFromProjectCode_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var role = userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode);
        assertThat(role).isNotPresent();
    }

    @Test
    void getCurrentUserAccountScopeRoleFromProjectCode_returnEmptyOptional_whenUserIsNotAuthenticated() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var role = userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode);
        assertThat(role).isNotPresent();
    }

    @Test
    void getCurrentUserAccountScopeRoleFromProjectCode_returnEmptyOptional_whenNoAuthorityFound() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var role = userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode);
        assertThat(role).isNotPresent();
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void getCurrentUserAccountScopeRoleFromProjectCode_returnRole_whenProjectFound(UserAccountScopeRole matchingRole) {
        // Given
        var matchingProjectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authorities = Set.of(authority1, authority2, authority3);

        var projectCode1 = "project-code1";
        var projectCode2 = "project-code2";
        var scope1 = mock(UserAccountScope.class);
        var scope2 = mock(UserAccountScope.class);
        var matchingScope = mock(UserAccountScope.class);
        var scopes = List.of(scope1, scope2, matchingScope);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountScopesFromAuthorities(authorities)).thenReturn(scopes);
        when(scope1.getProject()).thenReturn(projectCode1);
        when(scope2.getProject()).thenReturn(projectCode2);
        when(matchingScope.getProject()).thenReturn(matchingProjectCode);
        when(matchingScope.getRole()).thenReturn(matchingRole);

        // Then
        var actualRole = userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(matchingProjectCode);
        assertThat(actualRole).isPresent().contains(matchingRole);
    }

    @Test
    void getCurrentUserAccountScopeRoleFromProjectCode_returnEmptyOptional_whenProjectNotFound() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authorities = Set.of(authority1, authority2, authority3);

        var projectCode1 = "project-code1";
        var projectCode2 = "project-code2";
        var projectCode3 = "project-code3";
        var scope1 = mock(UserAccountScope.class);
        var scope2 = mock(UserAccountScope.class);
        var scope3 = mock(UserAccountScope.class);
        var scopes = List.of(scope1, scope2, scope3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountScopesFromAuthorities(authorities)).thenReturn(scopes);
        when(scope1.getProject()).thenReturn(projectCode1);
        when(scope2.getProject()).thenReturn(projectCode2);
        when(scope3.getProject()).thenReturn(projectCode3);

        // Then
        var role = userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode);
        assertThat(role).isNotPresent();
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
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var scopes = userSessionService.getCurrentUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getCurrentUserScopes_returnEmptyList_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        var scopes = userSessionService.getCurrentUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getCurrentUserScopes_returnEmptyList_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var scopes = userSessionService.getCurrentUserScopes();
        assertThat(scopes).isEmpty();
    }

    @Test
    void getCurrentUserScopes_returnUserAccountScopes_whenMatchingScopesFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authority4 = mock(GrantedAuthority.class);
        var authority5 = mock(GrantedAuthority.class);
        var authorities = Set.of(authority1, authority2, authority3, authority4, authority5);

        var scope1 = mock(UserAccountScope.class);
        var scope2 = mock(UserAccountScope.class);
        var scope3 = mock(UserAccountScope.class);
        var matchingScopes = List.of(scope1, scope2, scope3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountScopesFromAuthorities(authorities)).thenReturn(matchingScopes);

        // Then
        var actualScopes = userSessionService.getCurrentUserScopes();
        assertThat(actualScopes).containsExactlyInAnyOrderElementsOf(matchingScopes);
    }

    @Test
    void getCurrentUserProfile_throwForbiddenException_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.getCurrentUserProfile());
    }

    @Test
    void getCurrentUserProfile_throwForbiddenException_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.getCurrentUserProfile());
    }

    @Test
    void getCurrentUserProfile_throwForbiddenException_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.getCurrentUserProfile());
    }

    @Test
    void getCurrentUserProfile_throwForbiddenException_whenNoAuthorityFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.getCurrentUserProfile());
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountProfile.class)
    void getCurrentUserProfile_returnProfile_whenProfileFound(UserAccountProfile userProfile) throws ForbiddenException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountProfileFromAuthorities(authorities)).thenReturn(Optional.of(userProfile));

        // Then
        var profile = userSessionService.getCurrentUserProfile();
        assertThat(profile).isEqualTo(userProfile);
    }

    @Test
    void getCurrentUserProfile_throwForbiddenException_whenProfileNotFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountProfileFromAuthorities(authorities)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.getCurrentUserProfile());
    }

    @Test
    void refreshCurrentUserAuthorities_throwForbiddenException_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.refreshCurrentUserAuthorities());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void refreshCurrentUserAuthorities_throwForbiddenException_whenAuthenticationMapperThrowsForbiddenException() throws ForbiddenException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication)).thenThrow(new ForbiddenException(Entities.USER, "searching for current user"));

        // Then
        assertThrows(ForbiddenException.class, () -> userSessionService.refreshCurrentUserAuthorities());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void refreshCurrentUserAuthorities_throwForbiddenException_whenUserNotFound() throws ForbiddenException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication)).thenReturn(authenticatedUser);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

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

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var currentUser = mock(User.class);
        var currentUserAccount = mock(UserAccount.class);

        var updatedAuthority1 = mock(GrantedAuthority.class);
        var updatedAuthority2 = mock(GrantedAuthority.class);
        var updatedAuthority3 = mock(GrantedAuthority.class);
        var updatedAuthorities = Set.of(updatedAuthority1, updatedAuthority2, updatedAuthority3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication)).thenReturn(authenticatedUser);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(currentUser));
        when(userMapper.getFullScopeAccessUserAccountFromUser(currentUser)).thenReturn(currentUserAccount);
        when(authorityMapper.getGrantedAuthoritiesFromUserAccount(currentUserAccount)).thenReturn(updatedAuthorities);

        // Then
        userSessionService.refreshCurrentUserAuthorities();

        var authenticationToUpdateArgumentCaptor = ArgumentCaptor.forClass(OAuth2AuthenticationToken.class);
        verify(securityContext, times(1)).setAuthentication(authenticationToUpdateArgumentCaptor.capture());
        var authenticationToUpdate = authenticationToUpdateArgumentCaptor.getValue();
        assertThat(authenticationToUpdate.getPrincipal()).isSameAs(principal);
        assertThat(authenticationToUpdate.getAuthorities()).containsExactlyInAnyOrder(updatedAuthority1, updatedAuthority2, updatedAuthority3);
        assertThat(authenticationToUpdate.getAuthorizedClientRegistrationId()).isEqualTo(providerName);
    }

    @Test
    void getCurrentAuthenticatedOAuth2User_returnEmptyOptional_whenAuthenticationIsNotInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2User();
        assertThat(authenticatedUser).isNotPresent();
    }

    @Test
    void getCurrentAuthenticatedOAuth2User_returnEmptyOptional_whenAuthenticationMapperThrowsForbiddenException() throws ForbiddenException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication)).thenThrow(new ForbiddenException(Entities.USER, "fetching the current user"));

        // Then
        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2User();
        assertThat(authenticatedUser).isNotPresent();
    }

    @Test
    void getCurrentAuthenticatedOAuth2User_returnAuthenticatedOAuth2User_whenAuthenticationMapperReturnsAuthenticatedOAuth2User() throws ForbiddenException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var authenticatedOAuth2User = mock(AuthenticatedOAuth2User.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication)).thenReturn(authenticatedOAuth2User);

        // Then
        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2User();
        assertThat(authenticatedUser).containsSame(authenticatedOAuth2User);
    }

    @Test
    void getCurrentAuthenticatedOAuth2UserFromAuthentication_returnEmptyOptional_whenAuthenticationMapperThrowsForbiddenException() throws ForbiddenException {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication)).thenThrow(new ForbiddenException(Entities.USER, "fetching the current user"));

        // Then
        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2UserFromAuthentication(authentication);
        assertThat(authenticatedUser).isNotPresent();
    }

    @Test
    void getCurrentAuthenticatedOAuth2UserFromAuthentication_returnAuthenticatedOAuth2User_whenAuthenticationMapperReturnsAuthenticatedOAuth2User() throws ForbiddenException {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        var authenticatedOAuth2User = mock(AuthenticatedOAuth2User.class);

        // When
        when(authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication)).thenReturn(authenticatedOAuth2User);

        // Then
        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2UserFromAuthentication(authentication);
        assertThat(authenticatedUser).containsSame(authenticatedOAuth2User);
    }

    @Test
    void getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName_returnEmptyOptional_whenAuthenticationMapperThrowsForbiddenException() throws ForbiddenException {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var providerName = "provider-name";

        // When
        when(authenticationMapper.getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName)).thenThrow(new ForbiddenException(Entities.USER, "fetching the current user"));

        // Then
        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName);
        assertThat(authenticatedUser).isNotPresent();
    }

    @Test
    void getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName_returnAuthenticatedOAuth2User_whenAuthenticationMapperReturnsAuthenticatedOAuth2User() throws ForbiddenException {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var providerName = "provider-name";

        var authenticatedOAuth2User = mock(AuthenticatedOAuth2User.class);

        // When
        when(authenticationMapper.getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName)).thenReturn(authenticatedOAuth2User);

        // Then
        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName);
        assertThat(authenticatedUser).containsSame(authenticatedOAuth2User);
    }

    @Test
    void canManageGroup_returnFalse_whenAuthenticationIsNull() {
        // Given
        var groupId = 1L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isFalse();
    }

    @Test
    void canManageGroup_returnFalse_whenUserIsNotAuthenticated() {
        // Given
        var groupId = 1L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isFalse();
    }

    @Test
    void canManageGroup_returnFalse_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var groupId = 1L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isFalse();
    }

    @Test
    void canManageGroup_returnFalse_whenNoAuthorityFound() {
        // Given
        var groupId = 1L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(null);

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isFalse();
    }

    @Test
    void canManageGroup_returnFalse_whenProfileNotFound() {
        // Given
        var groupId = 1L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountProfileFromAuthorities(authorities)).thenReturn(Optional.empty());

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isFalse();
    }

    @Test
    void canManageGroup_returnTrue_whenProfileIsSuperAdmin() {
        // Given
        var groupId = 1L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        var profile = UserAccountProfile.SUPER_ADMIN;

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountProfileFromAuthorities(authorities)).thenReturn(Optional.of(profile));

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isTrue();
    }

    @Test
    void canManageGroup_returnFalse_whenProfileIsAuditor() {
        // Given
        var groupId = 1L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        var profile = UserAccountProfile.AUDITOR;

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountProfileFromAuthorities(authorities)).thenReturn(Optional.of(profile));

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isFalse();
    }

    @Test
    void canManageGroup_returnTrue_whenProfileIsScopedUserAndGroupIdFoundInContext() {
        // Given
        var groupId = 3L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        var profile = UserAccountProfile.SCOPED_USER;

        var managedGroupId1 = 1L;
        var managedGroupId2 = 2L;
        var managedGroupId3 = 3L;
        var managedGroupIds = List.of(managedGroupId1, managedGroupId2, managedGroupId3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountProfileFromAuthorities(authorities)).thenReturn(Optional.of(profile));
        when(authorityMapper.getManagedUserAccountGroupIdsFromAuthorities(authorities)).thenReturn(managedGroupIds);

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isTrue();
    }

    @Test
    void canManageGroup_returnFalse_whenProfileIsScopedUserButGroupIdNotFoundInContext() {
        // Given
        var groupId = 4L;

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        var profile = UserAccountProfile.SCOPED_USER;

        var managedGroupId1 = 1L;
        var managedGroupId2 = 2L;
        var managedGroupId3 = 3L;
        var managedGroupIds = List.of(managedGroupId1, managedGroupId2, managedGroupId3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authorityMapper.getUserAccountProfileFromAuthorities(authorities)).thenReturn(Optional.of(profile));
        when(authorityMapper.getManagedUserAccountGroupIdsFromAuthorities(authorities)).thenReturn(managedGroupIds);

        // Then
        var canManageGroup = userSessionService.canManageGroup(groupId);
        assertThat(canManageGroup).isFalse();
    }
}
