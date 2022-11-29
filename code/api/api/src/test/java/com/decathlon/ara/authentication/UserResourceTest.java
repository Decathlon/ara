package com.decathlon.ara.authentication;

import com.decathlon.ara.security.dto.user.LoggedInUserDTO;
import com.decathlon.ara.security.service.user.UserService;
import com.decathlon.ara.web.rest.authentication.UserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserResource userResource;

    @Test
    void getUserDetails_returnBadRequestResponse_whenUserNotFound() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(userService.getLoggedInUserDTO(authentication)).thenReturn(Optional.empty());

        // Then
        var response = userResource.getUserDetails(authentication);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getUserDetails_returnRequestResponseContainingUser_whenUserFound() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var user = mock(LoggedInUserDTO.class);

        // When
        when(userService.getLoggedInUserDTO(authentication)).thenReturn(Optional.of(user));

        // Then
        var response = userResource.getUserDetails(authentication);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(user);
    }
    
}
