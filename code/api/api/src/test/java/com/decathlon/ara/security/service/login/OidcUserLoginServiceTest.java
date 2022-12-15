package com.decathlon.ara.security.service.login;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.service.user.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OidcUserLoginServiceTest {

    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private OidcUserLoginService loginService;

    @Test
    void manageUserLoginRequest_saveUser_whenUserIsNotPersisted(){
        // Given
        var request = mock(OidcUserRequest.class);
        var userService = mock(OidcUserService.class);
        var oidcUser = mock(OidcUser.class);
        var oidcIdToken =  mock(OidcIdToken.class);
        var oidcUserInfo = mock(OidcUserInfo.class);
        Map<String, Object> claims = Map.ofEntries(entry(StandardClaimNames.SUB, "subValue"));
        var clientRegistration = mock(ClientRegistration.class);
        var newlyPersistedUser = mock(UserAccount.class);

        var authority1 = mock(GrantedAuthority.class);
        var authorityValue1 = "authority-1";
        var authority2 = mock(GrantedAuthority.class);
        var authorityValue2 = "authority-2";
        var authority3 = mock(GrantedAuthority.class);
        var authorityValue3 = "authority-3";
        HashSet authorities = new HashSet<>(List.of(authority1, authority2, authority3));

        var providerName = "provider-name";

        // When
        when(userAccountService.getOidcUserService()).thenReturn(userService);
        when(userService.loadUser(request)).thenReturn(oidcUser);
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getUserInfo()).thenReturn(oidcUserInfo);
        when(oidcIdToken.getClaims()).thenReturn(claims);
        when(request.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(providerName);
        when(userAccountService.getCurrentUserAccount(oidcUser, providerName)).thenReturn(Optional.empty());
        when(userAccountService.createUserAccount(oidcUser, providerName)).thenReturn(newlyPersistedUser);
        when(newlyPersistedUser.getMatchingAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn(authorityValue1);
        when(authority2.getAuthority()).thenReturn(authorityValue2);
        when(authority3.getAuthority()).thenReturn(authorityValue3);

        // Then
        var updatedUser = loginService.manageUserLoginRequest(request);
        assertThat(updatedUser).isNotNull().isExactlyInstanceOf(DefaultOidcUser.class);
        assertThat(updatedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(authorities);
        assertThat(updatedUser.getIdToken()).isSameAs(oidcIdToken);
        assertThat(updatedUser.getUserInfo()).isSameAs(oidcUserInfo);
        verify(userAccountService, times(1)).createUserAccount(oidcUser, providerName);
    }

    @Test
    void manageUserLoginRequest_fetchUser_whenUserIsPersisted(){
        // Given
        var request = mock(OidcUserRequest.class);
        var userService = mock(OidcUserService.class);
        var oidcUser = mock(OidcUser.class);
        var oidcIdToken = mock(OidcIdToken.class);
        var oidcUserInfo = mock(OidcUserInfo.class);
        Map<String, Object> claims = Map.ofEntries(entry(StandardClaimNames.SUB, "subValue"));
        var clientRegistration = mock(ClientRegistration.class);
        var alreadyPersistedUser = mock(UserAccount.class);

        var authority1 = mock(GrantedAuthority.class);
        var authorityValue1 = "authority-1";
        var authority2 = mock(GrantedAuthority.class);
        var authorityValue2 = "authority-2";
        var authority3 = mock(GrantedAuthority.class);
        var authorityValue3 = "authority-3";
        HashSet authorities = new HashSet<>(List.of(authority1, authority2, authority3));

        var providerName = "provider-name";

        // When
        when(userAccountService.getOidcUserService()).thenReturn(userService);
        when(userService.loadUser(request)).thenReturn(oidcUser);
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getUserInfo()).thenReturn(oidcUserInfo);
        when(oidcIdToken.getClaims()).thenReturn(claims);
        when(request.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(providerName);
        when(userAccountService.getCurrentUserAccount(oidcUser, providerName)).thenReturn(Optional.of(alreadyPersistedUser));
        when(alreadyPersistedUser.getMatchingAuthorities()).thenReturn(authorities);
        when(authority1.getAuthority()).thenReturn(authorityValue1);
        when(authority2.getAuthority()).thenReturn(authorityValue2);
        when(authority3.getAuthority()).thenReturn(authorityValue3);

        // Then
        var updatedUser = loginService.manageUserLoginRequest(request);
        assertThat(updatedUser).isNotNull().isExactlyInstanceOf(DefaultOidcUser.class);
        assertThat(updatedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(authorities);
        assertThat(updatedUser.getIdToken()).isEqualTo(oidcIdToken);
        assertThat(updatedUser.getUserInfo()).isEqualTo(oidcUserInfo);
        verify(userAccountService, never()).createUserAccount(any(), anyString());
    }
}
