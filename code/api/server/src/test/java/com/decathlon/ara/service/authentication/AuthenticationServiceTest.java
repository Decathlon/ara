/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.service.authentication;

import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthenticationStrategy authenticationStrategy;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void authenticate_throwAuthenticationException_whenUserRequestIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate((UserAuthenticationRequestDTO) null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoUserAuthenticatorFound() {
        // Given
        UserAuthenticationRequestDTO authenticationRequest = mock(UserAuthenticationRequestDTO.class);

        String provider = "provider";

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticator(provider)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_returnUserAuthenticationResponse_whenAuthenticatorFound() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO authenticationRequest = mock(UserAuthenticationRequestDTO.class);

        String provider = "provider";

        Authenticator authenticator = mock(Authenticator.class);

        ResponseEntity<UserAuthenticationDetailsDTO> authenticationResponse = mock(ResponseEntity.class);

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticator(provider)).thenReturn(Optional.of(authenticator));
        when(authenticator.authenticate(authenticationRequest)).thenReturn(authenticationResponse);

        // Then
        ResponseEntity<UserAuthenticationDetailsDTO> result = authenticationService.authenticate(authenticationRequest);
        assertThat(result).isEqualTo(authenticationResponse);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenAppRequestIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate((AppAuthenticationRequestDTO) null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoAppAuthenticatorFound() {
        // Given
        AppAuthenticationRequestDTO authenticationRequest = mock(AppAuthenticationRequestDTO.class);

        String provider = "provider";

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticator(provider)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_returnAppAuthenticationDetails_whenAuthenticatorFound() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO authenticationRequest = mock(AppAuthenticationRequestDTO.class);

        String provider = "provider";

        Authenticator authenticator = mock(Authenticator.class);

        ResponseEntity<AppAuthenticationDetailsDTO> authenticationResponse = mock(ResponseEntity.class);

        // When
        when(authenticationRequest.getProvider()).thenReturn(provider);
        when(authenticationStrategy.getAuthenticator(provider)).thenReturn(Optional.of(authenticator));
        when(authenticator.authenticate(authenticationRequest)).thenReturn(authenticationResponse);

        // Then
        ResponseEntity<AppAuthenticationDetailsDTO> result = authenticationService.authenticate(authenticationRequest);
        assertThat(result).isEqualTo(authenticationResponse);
    }
}
