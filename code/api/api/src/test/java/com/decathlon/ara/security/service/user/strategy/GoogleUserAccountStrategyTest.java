package com.decathlon.ara.security.service.user.strategy;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleUserAccountStrategyTest {

    @InjectMocks
    private GoogleUserAccountStrategy googleUserAccountStrategy;

    @Test
    void getUserAccount_returnUserAccount() {
        // Given
        var googleUser = mock(OidcUser.class);
        var userFirstName = "user-firstname";
        var userLastName = "user-lastName";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var userEntityLogin = "user-entity-login";
        var providerName = "provider-name";
        var userEntity = new UserEntity(userEntityLogin, providerName);

        // When
        when(googleUser.getGivenName()).thenReturn(userFirstName);
        when(googleUser.getFamilyName()).thenReturn(userLastName);
        when(googleUser.getEmail()).thenReturn(userEmail);
        when(googleUser.getPicture()).thenReturn(userPictureUrl);

        // Then
        assertThat(googleUserAccountStrategy.getLogin(googleUser)).isEqualTo(userEmail);
        assertThat(googleUserAccountStrategy.getFirstName(googleUser)).contains(userFirstName);
        assertThat(googleUserAccountStrategy.getLastName(googleUser)).contains(userLastName);
        assertThat(googleUserAccountStrategy.getEmail(googleUser)).contains(userEmail);
        assertThat(googleUserAccountStrategy.getPictureUrl(googleUser)).contains(userPictureUrl);

        var userAccount = googleUserAccountStrategy.getUserAccount(googleUser, userEntity);
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
