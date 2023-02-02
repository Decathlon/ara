package com.decathlon.ara.security.service.login;

import com.decathlon.ara.Entities;
import com.decathlon.ara.security.dto.authentication.user.AuthenticatedOAuth2User;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserLoginServiceTest {

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private AuthorityMapper authorityMapper;

    @InjectMocks
    private OAuth2UserLoginService loginService;

    @Test
    void manageUserLoginRequest_ignoreUserAuthorities_whenUserNotFoundInSession() throws ForbiddenException {
        // Given
        var request = mock(OAuth2UserRequest.class);
        var userService = mock(DefaultOAuth2UserService.class);
        var oauth2User = mock(OAuth2User.class);
        var clientRegistration = mock(ClientRegistration.class);

        var userLogin = "user-name";
        var providerName = "provider-name";

        var entry1 = entry("attribute-1", "attribute-value-1");
        var entry2 = entry("attribute-2", "attribute-value-2");
        var entry3 = entry("attribute-3", "attribute-value-3");
        var nameEntry = entry(StandardClaimNames.NAME, userLogin);
        Map<String, Object> attributes = Map.ofEntries(entry1, entry2, entry3, nameEntry);

        // When
        when(userAccountService.getDefaultOAuth2UserService()).thenReturn(userService);
        when(userService.loadUser(request)).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(request.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(providerName);
        when(userSessionService.getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName)).thenReturn(Optional.empty());

        // Then
        var updatedUser = loginService.manageUserLoginRequest(request);
        assertThat(updatedUser).isNotNull().isExactlyInstanceOf(DefaultOAuth2User.class);
        assertThat(updatedUser.getAuthorities()).isEmpty();
        assertThat(updatedUser.getAttributes()).containsExactlyInAnyOrderEntriesOf(attributes);
        assertThat(updatedUser.getName()).isEqualTo(userLogin);
        verify(userAccountService, never()).getCurrentUserAccountFromAuthenticatedOAuth2User(any());
        verify(userAccountService, never()).createUserAccountFromAuthenticatedOAuth2User(any());
    }

    @Test
    void manageUserLoginRequest_saveUser_whenUserNotFoundInDatabase() throws ForbiddenException {
        // Given
        var request = mock(OAuth2UserRequest.class);
        var userService = mock(DefaultOAuth2UserService.class);
        var oauth2User = mock(OAuth2User.class);
        var clientRegistration = mock(ClientRegistration.class);
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var userAccount = mock(UserAccount.class);

        var authority1 = mock(GrantedAuthority.class);
        var authorityValue1 = "authority-1";
        var authority2 = mock(GrantedAuthority.class);
        var authorityValue2 = "authority-2";
        var authority3 = mock(GrantedAuthority.class);
        var authorityValue3 = "authority-3";
        HashSet authorities = new HashSet<>(List.of(authority1, authority2, authority3));

        var userLogin = "user-name";
        var providerName = "provider-name";

        var entry1 = entry("attribute-1", "attribute-value-1");
        var entry2 = entry("attribute-2", "attribute-value-2");
        var entry3 = entry("attribute-3", "attribute-value-3");
        var nameEntry = entry(StandardClaimNames.NAME, userLogin);
        Map<String, Object> attributes = Map.ofEntries(entry1, entry2, entry3, nameEntry);

        // When
        when(userAccountService.getDefaultOAuth2UserService()).thenReturn(userService);
        when(userService.loadUser(request)).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(request.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(providerName);
        when(userSessionService.getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName)).thenReturn(Optional.of(authenticatedUser));
        when(userAccountService.getCurrentUserAccountFromAuthenticatedOAuth2User(authenticatedUser)).thenReturn(Optional.empty());
        when(userAccountService.createUserAccountFromAuthenticatedOAuth2User(authenticatedUser)).thenReturn(userAccount);
        when(authorityMapper.getGrantedAuthoritiesFromUserAccount(userAccount)).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn(authorityValue1);
        when(authority2.getAuthority()).thenReturn(authorityValue2);
        when(authority3.getAuthority()).thenReturn(authorityValue3);

        // Then
        var updatedUser = loginService.manageUserLoginRequest(request);
        assertThat(updatedUser).isNotNull().isExactlyInstanceOf(DefaultOAuth2User.class);
        assertThat(updatedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(authorities);
        assertThat(updatedUser.getAttributes()).containsExactlyInAnyOrderEntriesOf(attributes);
        assertThat(updatedUser.getName()).isEqualTo(userLogin);
        verify(userAccountService, times(1)).createUserAccountFromAuthenticatedOAuth2User(authenticatedUser);
    }

    @Test
    void manageUserLoginRequest_ignoreUserAuthorities_whenAnExceptionIsThrownWhilePersistingTheUser() throws ForbiddenException {
        // Given
        var request = mock(OAuth2UserRequest.class);
        var userService = mock(DefaultOAuth2UserService.class);
        var oauth2User = mock(OAuth2User.class);
        var clientRegistration = mock(ClientRegistration.class);
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var userLogin = "user-name";
        var providerName = "provider-name";

        var entry1 = entry("attribute-1", "attribute-value-1");
        var entry2 = entry("attribute-2", "attribute-value-2");
        var entry3 = entry("attribute-3", "attribute-value-3");
        var nameEntry = entry(StandardClaimNames.NAME, userLogin);
        Map<String, Object> attributes = Map.ofEntries(entry1, entry2, entry3, nameEntry);

        // When
        when(userAccountService.getDefaultOAuth2UserService()).thenReturn(userService);
        when(userService.loadUser(request)).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(request.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(providerName);
        when(userSessionService.getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName)).thenReturn(Optional.of(authenticatedUser));
        when(userAccountService.getCurrentUserAccountFromAuthenticatedOAuth2User(authenticatedUser)).thenReturn(Optional.empty());
        when(userAccountService.createUserAccountFromAuthenticatedOAuth2User(authenticatedUser)).thenThrow(new ForbiddenException(Entities.USER, "persisting user"));

        // Then
        var updatedUser = loginService.manageUserLoginRequest(request);
        assertThat(updatedUser).isNotNull().isExactlyInstanceOf(DefaultOAuth2User.class);
        assertThat(updatedUser.getAuthorities()).isEmpty();
        assertThat(updatedUser.getAttributes()).containsExactlyInAnyOrderEntriesOf(attributes);
        assertThat(updatedUser.getName()).isEqualTo(userLogin);
        verify(userAccountService, times(1)).createUserAccountFromAuthenticatedOAuth2User(authenticatedUser);
    }

    @Test
    void manageUserLoginRequest_fetchUser_whenUserFoundInDatabase() throws ForbiddenException {
        // Given
        var request = mock(OAuth2UserRequest.class);
        var userService = mock(DefaultOAuth2UserService.class);
        var oauth2User = mock(OAuth2User.class);
        var clientRegistration = mock(ClientRegistration.class);
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var userAccount = mock(UserAccount.class);

        var authority1 = mock(GrantedAuthority.class);
        var authorityValue1 = "authority-1";
        var authority2 = mock(GrantedAuthority.class);
        var authorityValue2 = "authority-2";
        var authority3 = mock(GrantedAuthority.class);
        var authorityValue3 = "authority-3";
        HashSet authorities = new HashSet<>(List.of(authority1, authority2, authority3));

        var userLogin = "user-name";
        var providerName = "provider-name";

        var entry1 = entry("attribute-1", "attribute-value-1");
        var entry2 = entry("attribute-2", "attribute-value-2");
        var entry3 = entry("attribute-3", "attribute-value-3");
        var nameEntry = entry(StandardClaimNames.NAME, userLogin);
        Map<String, Object> attributes = Map.ofEntries(entry1, entry2, entry3, nameEntry);

        // When
        when(userAccountService.getDefaultOAuth2UserService()).thenReturn(userService);
        when(userService.loadUser(request)).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(request.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(providerName);
        when(userSessionService.getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName)).thenReturn(Optional.of(authenticatedUser));
        when(userAccountService.getCurrentUserAccountFromAuthenticatedOAuth2User(authenticatedUser)).thenReturn(Optional.of(userAccount));
        when(authorityMapper.getGrantedAuthoritiesFromUserAccount(userAccount)).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn(authorityValue1);
        when(authority2.getAuthority()).thenReturn(authorityValue2);
        when(authority3.getAuthority()).thenReturn(authorityValue3);

        // Then
        var updatedUser = loginService.manageUserLoginRequest(request);
        assertThat(updatedUser).isNotNull().isExactlyInstanceOf(DefaultOAuth2User.class);
        assertThat(updatedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(authorities);
        assertThat(updatedUser.getAttributes()).containsExactlyInAnyOrderEntriesOf(attributes);
        assertThat(updatedUser.getName()).isEqualTo(userLogin);
        verify(userAccountService, never()).createUserAccountFromAuthenticatedOAuth2User(any());
    }
}
