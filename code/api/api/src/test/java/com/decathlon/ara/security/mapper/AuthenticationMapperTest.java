package com.decathlon.ara.security.mapper;

import com.decathlon.ara.security.service.user.strategy.UserAccountStrategy;
import com.decathlon.ara.security.service.user.strategy.select.UserStrategySelector;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationMapperTest {

    @Mock
    private UserStrategySelector strategySelector;

    @InjectMocks
    private AuthenticationMapper authenticationMapper;

    @Test
    void getAuthenticatedOAuth2UserFromAuthentication_throwForbiddenException_whenPrincipalIsNull() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var providerName = "provider-name";

        // When
        when(authentication.getPrincipal()).thenReturn(null);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);

        // Then
        assertThrows(ForbiddenException.class, () -> authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getAuthenticatedOAuth2UserFromAuthentication_throwForbiddenException_whenProviderNameIsBlank(String providerName) {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var oauth2User = mock(OAuth2User.class);

        // When
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);

        // Then
        assertThrows(ForbiddenException.class, () -> authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication));
    }

    @Test
    void getAuthenticatedOAuth2UserFromAuthentication_returnAuthenticatedOAuth2User_whenPrincipalAndProviderNameAreNotNullOrBlank() throws ForbiddenException {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var oauth2User = mock(OAuth2User.class);
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var userLogin = "user-login";
        var userFirstName = "user-first-name";
        var userLastName = "user-last-name";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        // When
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(strategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(strategy.getFirstName(oauth2User)).thenReturn(Optional.of(userFirstName));
        when(strategy.getLastName(oauth2User)).thenReturn(Optional.of(userLastName));
        when(strategy.getEmail(oauth2User)).thenReturn(Optional.of(userEmail));
        when(strategy.getPictureUrl(oauth2User)).thenReturn(Optional.of(userPictureUrl));

        // Then
        var authenticatedOAuth2User = authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication);
        assertThat(authenticatedOAuth2User)
                .extracting(
                        "providerName",
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        providerName,
                        userLogin,
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        Optional.of(userEmail),
                        Optional.of(userPictureUrl)
                );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName_throwForbiddenException_whenProviderNameIsBlank(String providerName) {
        // Given
        var oauth2User = mock(OAuth2User.class);

        // When

        // Then
        assertThrows(ForbiddenException.class, () -> authenticationMapper.getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName));
    }

    @Test
    void getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName_returnAuthenticatedOAuth2User_whenPrincipalAndProviderNameAreNotNullOrBlank() throws ForbiddenException {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var userLogin = "user-login";
        var userFirstName = "user-first-name";
        var userLastName = "user-last-name";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        // When
        when(strategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(strategy.getFirstName(oauth2User)).thenReturn(Optional.of(userFirstName));
        when(strategy.getLastName(oauth2User)).thenReturn(Optional.of(userLastName));
        when(strategy.getEmail(oauth2User)).thenReturn(Optional.of(userEmail));
        when(strategy.getPictureUrl(oauth2User)).thenReturn(Optional.of(userPictureUrl));

        // Then
        var authenticatedOAuth2User = authenticationMapper.getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName);
        assertThat(authenticatedOAuth2User)
                .extracting(
                        "providerName",
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        providerName,
                        userLogin,
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        Optional.of(userEmail),
                        Optional.of(userPictureUrl)
                );
    }
}
