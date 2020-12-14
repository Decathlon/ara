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

package com.decathlon.ara.service.authentication.provider.github;

import com.decathlon.ara.configuration.authentication.clients.github.AuthenticationGithubConfiguration;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubToken;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubUser;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GithubAuthenticatorTest {

    @Mock
    private AuthenticationGithubConfiguration githubConfiguration;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtTokenAuthenticationService jwtTokenAuthenticationService;

    @InjectMocks
    private GithubAuthenticator authenticator;

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenClientSecretNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "github_client_id";
        String code = "github_code";
        String provider = "provider";

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(githubConfiguration.getClientSecret()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationTokenNotFetchedException_whenTokenAPICallThrowException() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "github_client_id";
        String code = "github_code";
        String provider = "provider";

        String secret = "github_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GithubToken> tokenRequest = new HttpEntity<>(tokenHeader);

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(githubConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://github.com/login/oauth/access_token?client_id=github_client_id&scope=user:email%20read:user&client_secret=github_secret&code=github_code",
                        tokenRequest,
                        GithubToken.class
                )
        ).thenThrow(new RestClientException("Token API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallThrowException() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "github_client_id";
        String code = "github_code";
        String provider = "provider";

        String secret = "github_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GithubToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GithubToken token = mock(GithubToken.class);

        String accessToken = "access";
        String tokenType = "token_type";
        String tokenScope = "token_scope";

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(githubConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://github.com/login/oauth/access_token?client_id=github_client_id&scope=user:email%20read:user&client_secret=github_secret&code=github_code",
                        tokenRequest,
                        GithubToken.class
                )
        ).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(token.getTokenType()).thenReturn(tokenType);
        when(token.getScope()).thenReturn(tokenScope);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("User API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);

        ArgumentCaptor<String> userUrlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> userHttpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> userHttpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                userUrlArgumentCaptor.capture(),
                userHttpMethodArgumentCaptor.capture(),
                userHttpEntityArgumentCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );
        assertThat(userUrlArgumentCaptor.getValue()).isEqualTo("https://api.github.com/user");
        assertThat(userHttpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        HttpEntity<GithubUser> userRequest = userHttpEntityArgumentCaptor.getValue();
        HttpHeaders userHeader = userRequest.getHeaders();
        assertThat(userRequest.hasBody()).isFalse();
        assertThat(userHeader).hasSize(1);
        assertThat(userHeader.get("Authorization")).isEqualTo(Arrays.asList("token access"));
    }

    @Test
    public void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallReturnsAnErrorStatus() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "github_client_id";
        String code = "github_code";
        String provider = "provider";

        String secret = "github_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GithubToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GithubToken token = mock(GithubToken.class);

        String accessToken = "access";
        String tokenType = "token_type";
        String tokenScope = "token_scope";

        ResponseEntity<GithubUser> userResponseEntity = mock(ResponseEntity.class);

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(githubConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://github.com/login/oauth/access_token?client_id=github_client_id&scope=user:email%20read:user&client_secret=github_secret&code=github_code",
                        tokenRequest,
                        GithubToken.class
                )
        ).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(token.getTokenType()).thenReturn(tokenType);
        when(token.getScope()).thenReturn(tokenScope);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);

        ArgumentCaptor<String> userUrlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> userHttpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> userHttpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                userUrlArgumentCaptor.capture(),
                userHttpMethodArgumentCaptor.capture(),
                userHttpEntityArgumentCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );
        assertThat(userUrlArgumentCaptor.getValue()).isEqualTo("https://api.github.com/user");
        assertThat(userHttpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        HttpEntity<GithubUser> userRequest = userHttpEntityArgumentCaptor.getValue();
        HttpHeaders userHeader = userRequest.getHeaders();
        assertThat(userRequest.hasBody()).isFalse();
        assertThat(userHeader).hasSize(1);
        assertThat(userHeader.get("Authorization")).isEqualTo(Arrays.asList("token access"));
    }

    @Test
    public void authenticate_returnAuthenticationDetails_whenNoErrorOccurred() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        String clientId = "github_client_id";
        String code = "github_code";
        String provider = "provider";

        String secret = "github_secret";

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GithubToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GithubToken token = mock(GithubToken.class);

        String accessToken = "access";
        String tokenType = "token_type";
        String tokenScope = "token_scope";

        ResponseEntity<GithubUser> userResponseEntity = mock(ResponseEntity.class);
        GithubUser user = mock(GithubUser.class);
        Integer userId = 123;
        String userLogin = "github_login";
        String userEmail = "user_email";
        String userPicture = "github_picture_url";

        // When
        when(request.getClientId()).thenReturn(clientId);
        when(request.getCode()).thenReturn(code);
        when(request.getProvider()).thenReturn(provider);
        when(githubConfiguration.getClientSecret()).thenReturn(secret);
        when(
                restTemplate.postForObject(
                        "https://github.com/login/oauth/access_token?client_id=github_client_id&scope=user:email%20read:user&client_secret=github_secret&code=github_code",
                        tokenRequest,
                        GithubToken.class
                )
        ).thenReturn(token);
        when(token.getAccessToken()).thenReturn(accessToken);
        when(token.getTokenType()).thenReturn(tokenType);
        when(token.getScope()).thenReturn(tokenScope);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(userResponseEntity.getBody()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(user.getLogin()).thenReturn(userLogin);
        when(user.getEmail()).thenReturn(userEmail);
        when(user.getPicture()).thenReturn(userPicture);

        // Then
        UserAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);

        ArgumentCaptor<String> userUrlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> userHttpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> userHttpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                userUrlArgumentCaptor.capture(),
                userHttpMethodArgumentCaptor.capture(),
                userHttpEntityArgumentCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );
        assertThat(userUrlArgumentCaptor.getValue()).isEqualTo("https://api.github.com/user");
        assertThat(userHttpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        HttpEntity<GithubUser> userRequest = userHttpEntityArgumentCaptor.getValue();
        HttpHeaders userHeader = userRequest.getHeaders();
        assertThat(userRequest.hasBody()).isFalse();
        assertThat(userHeader).hasSize(1);
        assertThat(userHeader.get("Authorization")).isEqualTo(Arrays.asList("token access"));
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getProvider()).isEqualTo("provider");
        assertThat(authenticationDetails.getUser())
                .extracting(
                        "id",
                        "name",
                        "login",
                        "email",
                        "picture"
                )
                .contains(
                        "123",
                        null,
                        userLogin,
                        userEmail,
                        userPicture
                );
    }

    @Test
    public void authenticate_throwAuthenticationException_whenRequestIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> authenticator.authenticate((AppAuthenticationRequestDTO) null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoTokenGiven() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);

        // When
        when(request.getToken()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenExceptionThrownWhileCheckingToken() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "github_token";

        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", "token github_token");
        HttpEntity<Object> requestEntity = new HttpEntity<>(header);

        // When
        when(request.getToken()).thenReturn(token);
        when(restTemplate.exchange("https://api.github.com", HttpMethod.GET, requestEntity, Object.class))
                .thenThrow(new RestClientException("Token checking API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenTokenCheckingApiCallReturnsAnErrorStatus() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "github_token";

        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", "token github_token");
        HttpEntity<Object> requestEntity = new HttpEntity<>(header);

        ResponseEntity response = mock(ResponseEntity.class);

        // When
        when(request.getToken()).thenReturn(token);
        when(restTemplate.exchange("https://api.github.com", HttpMethod.GET, requestEntity, Object.class))
                .thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_returnAuthenticationDetails_whenValidTokenGiven() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "github_token";
        String provider = "provider";

        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", "token github_token");
        HttpEntity<Object> requestEntity = new HttpEntity<>(header);

        ResponseEntity response = mock(ResponseEntity.class);

        String jwt = "generated_jwt";

        // When
        when(request.getToken()).thenReturn(token);
        when(request.getProvider()).thenReturn(provider);
        when(restTemplate.exchange("https://api.github.com", HttpMethod.GET, requestEntity, Object.class))
                .thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(jwtTokenAuthenticationService.generateToken()).thenReturn(jwt);

        // Then
        AppAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getProvider()).isEqualTo(provider);
        assertThat(authenticationDetails.getAccessToken()).isEqualTo(jwt);
    }
}
