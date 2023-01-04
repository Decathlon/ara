package com.decathlon.ara.security.service.user.strategy;

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
    void getUserDetails_returnUserDetails() {
        // Given
        var googleUser = mock(OidcUser.class);
        var userFirstName = "user-firstname";
        var userLastName = "user-lastName";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

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
    }
}
