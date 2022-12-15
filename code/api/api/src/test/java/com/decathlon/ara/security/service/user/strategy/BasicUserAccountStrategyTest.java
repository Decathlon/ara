package com.decathlon.ara.security.service.user.strategy;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicUserAccountStrategyTest {

    @Test
    void getUserAccount_returnUserAccount_whenOidcUserFieldValuesFound() {
        // Given
        var customAttributes = new HashMap<String, String>();

        var oauth2User = mock(OidcUser.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastName";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var userEntityLogin = "user-entity-login";
        var providerName = "provider-name";
        var userEntity = new UserEntity(userEntityLogin, providerName);

        // When
        when(oauth2User.getName()).thenReturn(userLogin);
        when(oauth2User.getGivenName()).thenReturn(userFirstName);
        when(oauth2User.getFamilyName()).thenReturn(userLastName);
        when(oauth2User.getEmail()).thenReturn(userEmail);
        when(oauth2User.getPicture()).thenReturn(userPictureUrl);

        // Then
        var strategy = new BasicUserAccountStrategy(customAttributes);
        assertThat(strategy.getLogin(oauth2User)).isEqualTo(userLogin);
        assertThat(strategy.getFirstName(oauth2User)).contains(userFirstName);
        assertThat(strategy.getLastName(oauth2User)).contains(userLastName);
        assertThat(strategy.getEmail(oauth2User)).contains(userEmail);
        assertThat(strategy.getPictureUrl(oauth2User)).contains(userPictureUrl);

        var userAccount = strategy.getUserAccount(oauth2User, userEntity);
        assertThat(userAccount)
                .extracting(
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        userEntityLogin,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
    }

    @Test
    void getUserAccount_returnUserAccount_whenOidcUserAttributesFound() {
        // Given
        var customAttributes = new HashMap<String, String>();

        var oauth2User = mock(OidcUser.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastName";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var userEntityLogin = "user-entity-login";
        var providerName = "provider-name";
        var userEntity = new UserEntity(userEntityLogin, providerName);

        // When
        when(oauth2User.getName()).thenReturn(userLogin);
        when(oauth2User.getGivenName()).thenReturn(null);
        when(oauth2User.getFamilyName()).thenReturn(null);
        when(oauth2User.getEmail()).thenReturn(null);
        when(oauth2User.getPicture()).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(userFirstName);
        when(oauth2User.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(userLastName);
        when(oauth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn(userEmail);
        when(oauth2User.getAttribute(StandardClaimNames.PICTURE)).thenReturn(userPictureUrl);

        // Then
        var strategy = new BasicUserAccountStrategy(customAttributes);
        assertThat(strategy.getLogin(oauth2User)).isEqualTo(userLogin);
        assertThat(strategy.getFirstName(oauth2User)).contains(userFirstName);
        assertThat(strategy.getLastName(oauth2User)).contains(userLastName);
        assertThat(strategy.getEmail(oauth2User)).contains(userEmail);
        assertThat(strategy.getPictureUrl(oauth2User)).contains(userPictureUrl);

        var userAccount = strategy.getUserAccount(oauth2User, userEntity);
        assertThat(userAccount)
                .extracting(
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        userEntityLogin,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
    }

    @Test
    void getUserAccount_returnUserAccountWithCustomValues_whenOidcUserAttributesNotFound() {
        // Given
        var oauth2User = mock(OidcUser.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastName";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var userEntityLogin = "user-entity-login";
        var providerName = "provider-name";
        var userEntity = new UserEntity(userEntityLogin, providerName);

        var customFirstNameAttribute = "user-firstname-attribute";
        var customLastNameAttribute = "user-lastName-attribute";
        var customEmailAttribute = "user-email-attribute";
        var customPictureUrlAttribute = "user-picture-url-attribute";

        var customAttributes = Map.ofEntries(
                entry(StandardClaimNames.GIVEN_NAME, customFirstNameAttribute),
                entry(StandardClaimNames.FAMILY_NAME, customLastNameAttribute),
                entry(StandardClaimNames.EMAIL, customEmailAttribute),
                entry(StandardClaimNames.PICTURE, customPictureUrlAttribute)
        );

        // When
        when(oauth2User.getName()).thenReturn(userLogin);
        when(oauth2User.getGivenName()).thenReturn(null);
        when(oauth2User.getFamilyName()).thenReturn(null);
        when(oauth2User.getEmail()).thenReturn(null);
        when(oauth2User.getPicture()).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.PICTURE)).thenReturn(null);
        when(oauth2User.getAttribute(customFirstNameAttribute)).thenReturn(userFirstName);
        when(oauth2User.getAttribute(customLastNameAttribute)).thenReturn(userLastName);
        when(oauth2User.getAttribute(customEmailAttribute)).thenReturn(userEmail);
        when(oauth2User.getAttribute(customPictureUrlAttribute)).thenReturn(userPictureUrl);

        // Then
        var strategy = new BasicUserAccountStrategy(customAttributes);
        assertThat(strategy.getLogin(oauth2User)).isEqualTo(userLogin);
        assertThat(strategy.getFirstName(oauth2User)).contains(userFirstName);
        assertThat(strategy.getLastName(oauth2User)).contains(userLastName);
        assertThat(strategy.getEmail(oauth2User)).contains(userEmail);
        assertThat(strategy.getPictureUrl(oauth2User)).contains(userPictureUrl);

        var userAccount = strategy.getUserAccount(oauth2User, userEntity);
        assertThat(userAccount)
                .extracting(
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        userEntityLogin,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
    }

    @Test
    void getUserAccount_returnUserAccount_whenOAuth2UserAttributesFound() {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastName";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var userEntityLogin = "user-entity-login";
        var providerName = "provider-name";
        var userEntity = new UserEntity(userEntityLogin, providerName);

        // When
        when(oauth2User.getName()).thenReturn(userLogin);
        when(oauth2User.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(userFirstName);
        when(oauth2User.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(userLastName);
        when(oauth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn(userEmail);
        when(oauth2User.getAttribute(StandardClaimNames.PICTURE)).thenReturn(userPictureUrl);

        // Then
        var strategy = new BasicUserAccountStrategy(null);
        assertThat(strategy.getLogin(oauth2User)).isEqualTo(userLogin);
        assertThat(strategy.getFirstName(oauth2User)).contains(userFirstName);
        assertThat(strategy.getLastName(oauth2User)).contains(userLastName);
        assertThat(strategy.getEmail(oauth2User)).contains(userEmail);
        assertThat(strategy.getPictureUrl(oauth2User)).contains(userPictureUrl);

        var userAccount = strategy.getUserAccount(oauth2User, userEntity);
        assertThat(userAccount)
                .extracting(
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        userEntityLogin,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
    }

    @Test
    void getUserAccount_returnUserAccountWithCustomValues_whenOAuth2UserAttributesNotFound() {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastName";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var userEntityLogin = "user-entity-login";
        var providerName = "provider-name";
        var userEntity = new UserEntity(userEntityLogin, providerName);

        var customFirstNameAttribute = "user-firstname-attribute";
        var customLastNameAttribute = "user-lastName-attribute";
        var customEmailAttribute = "user-email-attribute";
        var customPictureUrlAttribute = "user-picture-url-attribute";

        var customAttributes = Map.ofEntries(
                entry(StandardClaimNames.GIVEN_NAME, customFirstNameAttribute),
                entry(StandardClaimNames.FAMILY_NAME, customLastNameAttribute),
                entry(StandardClaimNames.EMAIL, customEmailAttribute),
                entry(StandardClaimNames.PICTURE, customPictureUrlAttribute)
        );

        // When
        when(oauth2User.getName()).thenReturn(userLogin);
        when(oauth2User.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn(null);
        when(oauth2User.getAttribute(StandardClaimNames.PICTURE)).thenReturn(null);
        when(oauth2User.getAttribute(customFirstNameAttribute)).thenReturn(userFirstName);
        when(oauth2User.getAttribute(customLastNameAttribute)).thenReturn(userLastName);
        when(oauth2User.getAttribute(customEmailAttribute)).thenReturn(userEmail);
        when(oauth2User.getAttribute(customPictureUrlAttribute)).thenReturn(userPictureUrl);

        // Then
        var strategy = new BasicUserAccountStrategy(customAttributes);
        assertThat(strategy.getLogin(oauth2User)).isEqualTo(userLogin);
        assertThat(strategy.getFirstName(oauth2User)).contains(userFirstName);
        assertThat(strategy.getLastName(oauth2User)).contains(userLastName);
        assertThat(strategy.getEmail(oauth2User)).contains(userEmail);
        assertThat(strategy.getPictureUrl(oauth2User)).contains(userPictureUrl);

        var userAccount = strategy.getUserAccount(oauth2User, userEntity);
        assertThat(userAccount)
                .extracting(
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .contains(
                        userEntityLogin,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
    }
}
