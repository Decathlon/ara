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

import com.decathlon.ara.configuration.AraConfiguration;
import com.decathlon.ara.configuration.authentication.provider.google.AuthenticationGoogleConfiguration;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleToken;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleUser;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthenticatorTest {

    @Mock
    private AuthenticationGoogleConfiguration googleConfiguration;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtTokenAuthenticationService jwtTokenAuthenticationService;

    @Mock
    private AraConfiguration araConfiguration;

    @InjectMocks
    private GoogleAuthenticator authenticator;

    @Data
    @AllArgsConstructor
    private class DummyGoogleVerificationTokenContainingVerifiedEmailFieldAsString {

        private String email_verified;
    }

    @Data
    @AllArgsConstructor
    private class DummyGoogleVerificationTokenContainingVerifiedEmailFieldAsBoolean {

        private Boolean email_verified;
    }

    @Data
    @AllArgsConstructor
    private class DummyGoogleVerificationTokenNotContainingVerifiedEmailField {

        private String does_not_contain_email_verified_field;
    }

    @Test
    void authenticate_throwAuthenticationException_whenProviderNotEnabled() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String code = "google_code";
        String provider = "provider";

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(false);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    // User
    @Test
    void authenticate_throwAuthenticationConfigurationNotFoundException_whenClientBaseUrlNull() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String code = "google_code";
        String provider = "provider";

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    void authenticate_throwAuthenticationConfigurationNotFoundException_whenClientSecretNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org";

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    void authenticate_throwAuthenticationConfigurationNotFoundException_whenClientIdNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org";

        String secret = "google_secret";

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(googleConfiguration.getClientId()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    void authenticate_throwAuthenticationTokenNotFetchedException_whenTokenAPICallThrowException() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(googleConfiguration.getClientId()).thenReturn(clientId);
        when(
                restTemplate.exchange(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://client_base_url.org/login/google&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        HttpMethod.POST,
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenThrow(new RestClientException("Token API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    void authenticate_throwAuthenticationTokenNotFetchedException_whenTokenAPICallReturnsAnErrorStatus() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org/";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        ResponseEntity<GoogleToken> response = mock(ResponseEntity.class);

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(googleConfiguration.getClientId()).thenReturn(clientId);
        when(
                restTemplate.exchange(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://client_base_url.org/login/google&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        HttpMethod.POST,
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallThrowException() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        ResponseEntity<GoogleToken> response = mock(ResponseEntity.class);
        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";

        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(googleConfiguration.getClientId()).thenReturn(clientId);
        when(
                restTemplate.exchange(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://client_base_url.org/login/google&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        HttpMethod.POST,
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, userRequest, GoogleUser.class))
                .thenThrow(new RestClientException("User API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);
    }

    @Test
    void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallReturnsAnErrorStatus() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org/";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        ResponseEntity<GoogleToken> response = mock(ResponseEntity.class);
        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";

        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        ResponseEntity<GoogleUser> userResponseEntity = mock(ResponseEntity.class);

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(googleConfiguration.getClientId()).thenReturn(clientId);
        when(
                restTemplate.exchange(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://client_base_url.org/login/google&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        HttpMethod.POST,
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, userRequest, GoogleUser.class))
                .thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);
    }

    @Test
    void authenticate_throwAuthenticationUserNotFetchedException_whenUserEmailIsNotVerified() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        ResponseEntity<GoogleToken> response = mock(ResponseEntity.class);
        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";

        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        ResponseEntity<GoogleUser> userResponseEntity = mock(ResponseEntity.class);

        GoogleUser user = mock(GoogleUser.class);

        // When
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(googleConfiguration.getClientId()).thenReturn(clientId);
        when(
                restTemplate.exchange(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://client_base_url.org/login/google&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        HttpMethod.POST,
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
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
    void authenticate_returnAuthenticationDetails_whenNoErrorOccurred() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "google_client_id";
        String code = "google_code";
        String provider = "provider";
        String clientBaseUrl = "http://client_base_url.org/";

        String secret = "google_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        ResponseEntity<GoogleToken> response = mock(ResponseEntity.class);
        GoogleToken token = mock(GoogleToken.class);

        String accessToken = "access";

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
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(araConfiguration.getClientBaseUrl()).thenReturn(clientBaseUrl);
        when(googleConfiguration.getClientSecret()).thenReturn(secret);
        when(googleConfiguration.getClientId()).thenReturn(clientId);
        when(
                restTemplate.exchange(
                        "https://oauth2.googleapis.com/token?" +
                                "client_id=google_client_id&" +
                                "client_secret=google_secret&" +
                                "redirect_uri=http://client_base_url.org/login/google&" +
                                "grant_type=authorization_code&" +
                                "code=google_code",
                        HttpMethod.POST,
                        tokenRequest,
                        GoogleToken.class
                )
        ).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
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
        ResponseEntity<UserAuthenticationDetailsDTO> authenticationResponse = authenticator.authenticate(request);
        UserAuthenticationDetailsDTO authenticationDetails = authenticationResponse.getBody();
        assertThat(authenticationDetails).isNotNull();
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
        verify(jwtTokenAuthenticationService).createAuthenticationResponseCookieHeader(Optional.empty());
    }

    // Application
    @Test
    void authenticate_throwAuthenticationException_whenRequestIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> authenticator.authenticate((AppAuthenticationRequestDTO) null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_throwAuthenticationException_whenNoProviderGiven() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);

        // When
        when(request.getProvider()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_throwAuthenticationException_whenNoTokenGiven() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String provider = "provider";

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_throwAuthenticationException_whenProviderIsNotEnabled() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "token";
        String provider = "provider";

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(token);
        when(googleConfiguration.isEnabled()).thenReturn(false);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_throwAuthenticationException_whenExceptionThrownWhileCheckingToken() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "token";
        String provider = "provider";

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(token);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(restTemplate.exchange("https://oauth2.googleapis.com/tokeninfo?access_token=token", HttpMethod.GET, null, Object.class))
                .thenThrow(new RestClientException("Token checking API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_throwAuthenticationException_whenTokenCheckingApiCallReturnsAnErrorStatus() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "token";
        String provider = "provider";

        ResponseEntity response = mock(ResponseEntity.class);

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(token);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(restTemplate.exchange("https://oauth2.googleapis.com/tokeninfo?access_token=token", HttpMethod.GET, null, Object.class))
                .thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_throwAuthenticationException_whenEmailVerifiedFieldNotFound() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "token";
        String provider = "provider";

        ResponseEntity response = mock(ResponseEntity.class);

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(token);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(restTemplate.exchange("https://oauth2.googleapis.com/tokeninfo?access_token=token", HttpMethod.GET, null, Object.class))
                .thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(new DummyGoogleVerificationTokenNotContainingVerifiedEmailField("some_dummy_value"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not_a_boolean_value", "false"})
    void authenticate_throwAuthenticationException_whenEmailNotVerified(String emailVerified) {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "token";
        String provider = "provider";

        ResponseEntity response = mock(ResponseEntity.class);

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(token);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(restTemplate.exchange("https://oauth2.googleapis.com/tokeninfo?access_token=token", HttpMethod.GET, null, Object.class))
                .thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(new DummyGoogleVerificationTokenContainingVerifiedEmailFieldAsString(emailVerified));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_returnAuthenticationDetails_whenEmailVerifiedIsAStringAndValidTokenGiven() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "token";
        String provider = "provider";

        ResponseEntity response = mock(ResponseEntity.class);

        String jwt = "generated_jwt";

        Long tokenAge = 3600L;

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(token);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(restTemplate.exchange("https://oauth2.googleapis.com/tokeninfo?access_token=token", HttpMethod.GET, null, Object.class))
                .thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(jwtTokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.empty())).thenReturn(tokenAge);
        when(jwtTokenAuthenticationService.generateToken(tokenAge)).thenReturn(jwt);
        when(response.getBody()).thenReturn(new DummyGoogleVerificationTokenContainingVerifiedEmailFieldAsString("true"));

        // Then
        ResponseEntity<AppAuthenticationDetailsDTO> authenticationResponse = authenticator.authenticate(request);
        AppAuthenticationDetailsDTO authenticationDetails = authenticationResponse.getBody();
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getAccessToken()).isEqualTo(jwt);
    }

    @Test
    void authenticate_returnAuthenticationDetails_whenEmailVerifiedIsABooleanAndValidTokenGiven() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "token";
        String provider = "provider";

        ResponseEntity response = mock(ResponseEntity.class);

        String jwt = "generated_jwt";

        Long tokenAge = 3600L;

        // When
        when(request.getProvider()).thenReturn(provider);
        when(request.getToken()).thenReturn(token);
        when(googleConfiguration.isEnabled()).thenReturn(true);
        when(restTemplate.exchange("https://oauth2.googleapis.com/tokeninfo?access_token=token", HttpMethod.GET, null, Object.class))
                .thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(jwtTokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.empty())).thenReturn(tokenAge);
        when(jwtTokenAuthenticationService.generateToken(tokenAge)).thenReturn(jwt);
        when(response.getBody()).thenReturn(new DummyGoogleVerificationTokenContainingVerifiedEmailFieldAsBoolean(true));

        // Then
        ResponseEntity<AppAuthenticationDetailsDTO> authenticationResponse = authenticator.authenticate(request);
        AppAuthenticationDetailsDTO authenticationDetails = authenticationResponse.getBody();
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getAccessToken()).isEqualTo(jwt);
    }
}
