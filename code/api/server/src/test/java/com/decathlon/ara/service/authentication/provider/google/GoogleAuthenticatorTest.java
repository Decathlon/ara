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

package com.decathlon.ara.service.authentication.provider.google;

import com.decathlon.ara.configuration.authentication.clients.google.AuthenticationGoogleConfiguration;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleToken;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleUser;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationDetailsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthenticatorTest {

    @Mock
    private AuthenticationGoogleConfiguration googleConfiguration;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleAuthenticator authenticator;

    @Test
    public void authenticate_throwAuthenticationTokenNotFetchedException_whenRedirectUriNull() {
        // Given
        AuthenticationRequestDTO request = mock(AuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(request.getRedirectUri()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenClientSecretNotFound() {
        // Given
        AuthenticationRequestDTO request = mock(AuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String redirectUri = "http://redirect_uri.com";

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(request.getRedirectUri()).thenReturn(redirectUri);
        when(googleConfiguration.getClientSecret()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationTokenNotFetchedException_whenTokenAPICallThrowException() {
        // Given
        AuthenticationRequestDTO request = mock(AuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String redirectUri = "http://redirect_uri.com";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(request.getRedirectUri()).thenReturn(redirectUri);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://redirect_uri.com&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenThrow(new RestClientException("Token API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallThrowException() {
        // Given
        AuthenticationRequestDTO request = mock(AuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String redirectUri = "http://redirect_uri.com";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";
        Integer expiration = 3600;
        String tokenType = "token_type";
        String tokenScope = "token_scope";

        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(request.getRedirectUri()).thenReturn(redirectUri);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://redirect_uri.com&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(token.getExpiration()).thenReturn(expiration);
        when(token.getScope()).thenReturn(tokenScope);
        when(token.getType()).thenReturn(tokenType);
        when(restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, userRequest, GoogleUser.class))
                .thenThrow(new RestClientException("User API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallReturnsAnErrorStatus() {
        // Given
        AuthenticationRequestDTO request = mock(AuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String redirectUri = "http://redirect_uri.com";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";
        Integer expiration = 3600;
        String tokenType = "token_type";
        String tokenScope = "token_scope";

        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        ResponseEntity<GoogleUser> userResponseEntity = mock(ResponseEntity.class);

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(request.getRedirectUri()).thenReturn(redirectUri);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://redirect_uri.com&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(token.getExpiration()).thenReturn(expiration);
        when(token.getScope()).thenReturn(tokenScope);
        when(token.getType()).thenReturn(tokenType);
        when(restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, userRequest, GoogleUser.class))
                .thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationUserNotFetchedException_whenUserEmailIsNotVerified() {
        // Given
        AuthenticationRequestDTO request = mock(AuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String redirectUri = "http://redirect_uri.com";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";
        Integer expiration = 3600;
        String tokenType = "token_type";
        String tokenScope = "token_scope";

        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        ResponseEntity<GoogleUser> userResponseEntity = mock(ResponseEntity.class);

        GoogleUser user = mock(GoogleUser.class);

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(request.getRedirectUri()).thenReturn(redirectUri);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://redirect_uri.com&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(token.getExpiration()).thenReturn(expiration);
        when(token.getScope()).thenReturn(tokenScope);
        when(token.getType()).thenReturn(tokenType);
        when(restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, userRequest, GoogleUser.class))
                .thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(userResponseEntity.getBody()).thenReturn(user);
        when(user.getVerifiedEmail()).thenReturn(false);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);
    }

    @Test
    public void authenticate_returnAuthenticationDetails_whenNoErrorOccurred() throws AuthenticationException {
        // Given
        AuthenticationRequestDTO request = mock(AuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String redirectUri = "http://redirect_uri.com";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";
        Integer expiration = 3600;
        String tokenType = "token_type";
        String tokenScope = "token_scope";

        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        ResponseEntity<GoogleUser> userResponseEntity = mock(ResponseEntity.class);

        GoogleUser user = mock(GoogleUser.class);
        String accountId = "googleAccountId";
        String name = "Google user name";
        String picture = "google_user_picture_url";
        String email = "user@gmail.com";

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(request.getRedirectUri()).thenReturn(redirectUri);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://redirect_uri.com&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(token.getExpiration()).thenReturn(expiration);
        when(token.getScope()).thenReturn(tokenScope);
        when(token.getType()).thenReturn(tokenType);
        when(restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, userRequest, GoogleUser.class))
                .thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(userResponseEntity.getBody()).thenReturn(user);
        when(user.getVerifiedEmail()).thenReturn(true);
        when(user.getAccountId()).thenReturn(accountId);
        when(user.getName()).thenReturn(name);
        when(user.getEmail()).thenReturn(email);
        when(user.getPicture()).thenReturn(picture);

        // Then
        AuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getProvider()).isEqualTo(provider);
        assertThat(authenticationDetails.getUser())
                .extracting(
                        "id",
                        "name",
                        "login",
                        "email",
                        "picture"
                )
                .contains(
                        accountId,
                        name,
                        name,
                        email,
                        picture
                );
    }
}
