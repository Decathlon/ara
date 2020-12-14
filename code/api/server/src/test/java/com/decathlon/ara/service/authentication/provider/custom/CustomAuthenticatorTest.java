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

package com.decathlon.ara.service.authentication.provider.custom;

import com.decathlon.ara.configuration.authentication.clients.custom.AuthenticationCustomConfiguration;
import com.decathlon.ara.configuration.authentication.clients.custom.token.AuthenticationCustomTokenConfiguration;
import com.decathlon.ara.configuration.authentication.clients.custom.token.AuthenticationCustomTokenFieldsConfiguration;
import com.decathlon.ara.configuration.authentication.clients.custom.user.AuthenticationCustomUserConfiguration;
import com.decathlon.ara.configuration.authentication.clients.custom.user.AuthenticationCustomUserFieldsConfiguration;
import com.decathlon.ara.configuration.authentication.clients.custom.validation.AuthenticationCustomTokenValidationConfiguration;
import com.decathlon.ara.configuration.authentication.clients.custom.validation.AuthenticationCustomTokenValidationFieldConfiguration;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomAuthenticatorTest {

    @Mock
    private AuthenticationCustomConfiguration configuration;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtTokenAuthenticationService jwtTokenAuthenticationService;

    @Mock
    private AuthenticationCustomTokenValidationConfiguration tokenValidationConfiguration;

    @InjectMocks
    private CustomAuthenticator authenticator;

    @Data
    private class DummyTokenForTest {

        private String dummyTokenIdAsString;

        private Integer dummyTokenIdAsInteger;

        private String dummyAccess;

        private String dummyRefresh;

        private String dummyExpirationAsString;

        private Integer dummyExpirationAsInteger;

        private String dummyType;

        private String dummyScope;
    }

    @Data
    private class DummyUserForTest {

        private String dummyUserIdAsString;

        private Integer dummyUserIdAsInteger;

        private String dummyName;

        private String dummyLogin;

        private String dummyEmail;

        private String dummyPictureUrl;
    }

    @Data
    private class DummyTokenVerificationContainingBooleanField {

        private Boolean stillActive;
    }

    @Data
    private class DummyTokenVerificationContainingNonBooleanField {

        private String stillActive;
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoRequestGiven() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> authenticator.authenticate((UserAuthenticationRequestDTO) null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoProviderGiven() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);

        // When
        when(request.getProvider()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationTokenNotFetchedException_whenNoCodeGiven() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationTokenNotFetchedException_whenNoClientIdGiven() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenTokenConfigurationNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenTokenConfigurationUriNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenTokenConfigurationFieldsNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn("the_token_uri");
        when(tokenConfiguration.getFields()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationTokenNotFetchedException_whenTokenAPICallThrowException() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenThrow(new RestClientException("API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationTokenNotFetchedException_whenTokenAPICallReturnsAnErrorStatus() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationTokenNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenUserConfigurationNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(new DummyTokenForTest());
        when(configuration.getUser()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenUserConfigurationURINotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(new DummyTokenForTest());
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenUserConfigurationFieldsNotFound() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        String userUri = "the_user_uri";

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(new DummyTokenForTest());
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(userUri);
        when(userConfiguration.getFields()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallThrowException() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        String userUri = "the_user_uri";

        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = mock(AuthenticationCustomUserFieldsConfiguration.class);

        HttpEntity<MultiValueMap<String, String>> userRequest = mock(HttpEntity.class);

        HttpMethod userHttpMethod = HttpMethod.GET;

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(new DummyTokenForTest());
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(userUri);
        when(userConfiguration.getFields()).thenReturn(userFieldsConfiguration);
        when(userConfiguration.getRequest(anyMap())).thenReturn(userRequest);
        when(userConfiguration.getHttpMethod()).thenReturn(userHttpMethod);
        when(restTemplate.exchange(userUri, userHttpMethod, userRequest, Object.class)).thenThrow(new RestClientException("API call error"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);
    }

    @Test
    public void authenticate_throwAuthenticationUserNotFetchedException_whenUserAPICallReturnsAnErrorStatus() {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        String userUri = "the_user_uri";

        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = mock(AuthenticationCustomUserFieldsConfiguration.class);

        HttpEntity<MultiValueMap<String, String>> userRequest = mock(HttpEntity.class);

        HttpMethod userHttpMethod = HttpMethod.GET;

        ResponseEntity<Object> userResponseEntity = mock(ResponseEntity.class);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(new DummyTokenForTest());
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(userUri);
        when(userConfiguration.getFields()).thenReturn(userFieldsConfiguration);
        when(userConfiguration.getRequest(anyMap())).thenReturn(userRequest);
        when(userConfiguration.getHttpMethod()).thenReturn(userHttpMethod);
        when(restTemplate.exchange(userUri, userHttpMethod, userRequest, Object.class)).thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationUserNotFetchedException.class);
    }

    @Test
    public void authenticate_returnFullyFilledAuthenticationDetails_whenAllFieldsGivenAndFound() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        String userUri = "the_user_uri";

        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = mock(AuthenticationCustomUserFieldsConfiguration.class);

        HttpEntity<MultiValueMap<String, String>> userRequest = mock(HttpEntity.class);

        HttpMethod userHttpMethod = HttpMethod.GET;

        ResponseEntity<Object> userResponseEntity = mock(ResponseEntity.class);

        String tokenId = "myTokenId";
        String tokenAccess = "myTokenAccess";
        String tokenRefresh = "myTokenRefresh";
        Integer tokenExpiration = 3600;
        String tokenType = "myTokenType";
        String tokenScope = "myTokenScope";
        DummyTokenForTest dummyTokenForTest = new DummyTokenForTest();
        dummyTokenForTest.setDummyTokenIdAsString(tokenId);
        dummyTokenForTest.setDummyAccess(tokenAccess);
        dummyTokenForTest.setDummyRefresh(tokenRefresh);
        dummyTokenForTest.setDummyExpirationAsInteger(tokenExpiration);
        dummyTokenForTest.setDummyType(tokenType);
        dummyTokenForTest.setDummyScope(tokenScope);

        String userId = "myUserId";
        String userLogin = "myUserLogin";
        String userName = "myUserName";
        String userEmail = "myUserEmail";
        String userPictureUrl = "myUserPicture";
        DummyUserForTest dummyUserForTest = new DummyUserForTest();
        dummyUserForTest.setDummyUserIdAsString(userId);
        dummyUserForTest.setDummyLogin(userLogin);
        dummyUserForTest.setDummyName(userName);
        dummyUserForTest.setDummyEmail(userEmail);
        dummyUserForTest.setDummyPictureUrl(userPictureUrl);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenFieldsConfiguration.getId()).thenReturn("dummyTokenIdAsString");
        when(tokenFieldsConfiguration.getAccess()).thenReturn("dummyAccess");
        when(tokenFieldsConfiguration.getRefresh()).thenReturn("dummyRefresh");
        when(tokenFieldsConfiguration.getExpiration()).thenReturn("dummyExpirationAsInteger");
        when(tokenFieldsConfiguration.getType()).thenReturn("dummyType");
        when(tokenFieldsConfiguration.getScope()).thenReturn("dummyScope");
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(dummyTokenForTest);
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(userUri);
        when(userConfiguration.getFields()).thenReturn(userFieldsConfiguration);
        when(userFieldsConfiguration.getId()).thenReturn("dummyUserIdAsString");
        when(userFieldsConfiguration.getLogin()).thenReturn("dummyLogin");
        when(userFieldsConfiguration.getName()).thenReturn("dummyName");
        when(userFieldsConfiguration.getEmail()).thenReturn("dummyEmail");
        when(userFieldsConfiguration.getPictureUrl()).thenReturn("dummyPictureUrl");
        when(userConfiguration.getRequest(anyMap())).thenReturn(userRequest);
        when(userConfiguration.getHttpMethod()).thenReturn(userHttpMethod);
        when(restTemplate.exchange(userUri, userHttpMethod, userRequest, Object.class)).thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(userResponseEntity.getBody()).thenReturn(dummyUserForTest);

        // Then
        UserAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
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
                        userId,
                        userName,
                        userLogin,
                        userEmail,
                        userPictureUrl
                );
    }

    @Test
    public void authenticate_returnFullyFilledAuthenticationDetails_whenAllFieldsGivenAndFoundAndIdsAreIntegers() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        String userUri = "the_user_uri";

        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = mock(AuthenticationCustomUserFieldsConfiguration.class);

        HttpEntity<MultiValueMap<String, String>> userRequest = mock(HttpEntity.class);

        HttpMethod userHttpMethod = HttpMethod.GET;

        ResponseEntity<Object> userResponseEntity = mock(ResponseEntity.class);

        Integer tokenId = 12345;
        String tokenAccess = "myTokenAccess";
        String tokenRefresh = "myTokenRefresh";
        Integer tokenExpiration = 3600;
        String tokenType = "myTokenType";
        String tokenScope = "myTokenScope";
        DummyTokenForTest dummyTokenForTest = new DummyTokenForTest();
        dummyTokenForTest.setDummyTokenIdAsInteger(tokenId);
        dummyTokenForTest.setDummyAccess(tokenAccess);
        dummyTokenForTest.setDummyRefresh(tokenRefresh);
        dummyTokenForTest.setDummyExpirationAsInteger(tokenExpiration);
        dummyTokenForTest.setDummyType(tokenType);
        dummyTokenForTest.setDummyScope(tokenScope);

        Integer userId = 67890;
        String userLogin = "myUserLogin";
        String userName = "myUserName";
        String userEmail = "myUserEmail";
        String userPictureUrl = "myUserPicture";
        DummyUserForTest dummyUserForTest = new DummyUserForTest();
        dummyUserForTest.setDummyUserIdAsInteger(userId);
        dummyUserForTest.setDummyLogin(userLogin);
        dummyUserForTest.setDummyName(userName);
        dummyUserForTest.setDummyEmail(userEmail);
        dummyUserForTest.setDummyPictureUrl(userPictureUrl);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenFieldsConfiguration.getId()).thenReturn("dummyTokenIdAsInteger");
        when(tokenFieldsConfiguration.getAccess()).thenReturn("dummyAccess");
        when(tokenFieldsConfiguration.getRefresh()).thenReturn("dummyRefresh");
        when(tokenFieldsConfiguration.getExpiration()).thenReturn("dummyExpirationAsInteger");
        when(tokenFieldsConfiguration.getType()).thenReturn("dummyType");
        when(tokenFieldsConfiguration.getScope()).thenReturn("dummyScope");
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(dummyTokenForTest);
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(userUri);
        when(userConfiguration.getFields()).thenReturn(userFieldsConfiguration);
        when(userFieldsConfiguration.getId()).thenReturn("dummyUserIdAsInteger");
        when(userFieldsConfiguration.getLogin()).thenReturn("dummyLogin");
        when(userFieldsConfiguration.getName()).thenReturn("dummyName");
        when(userFieldsConfiguration.getEmail()).thenReturn("dummyEmail");
        when(userFieldsConfiguration.getPictureUrl()).thenReturn("dummyPictureUrl");
        when(userConfiguration.getRequest(anyMap())).thenReturn(userRequest);
        when(userConfiguration.getHttpMethod()).thenReturn(userHttpMethod);
        when(restTemplate.exchange(userUri, userHttpMethod, userRequest, Object.class)).thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(userResponseEntity.getBody()).thenReturn(dummyUserForTest);

        // Then
        UserAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
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
                        "67890",
                        userName,
                        userLogin,
                        userEmail,
                        userPictureUrl
                );
    }

    @Test
    public void authenticate_returnFullyFilledAuthenticationDetails_whenAllFieldsGivenAndFoundAndTokenExpirationAsStringButCorrectIntegerRepresentation() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        String userUri = "the_user_uri";

        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = mock(AuthenticationCustomUserFieldsConfiguration.class);

        HttpEntity<MultiValueMap<String, String>> userRequest = mock(HttpEntity.class);

        HttpMethod userHttpMethod = HttpMethod.GET;

        ResponseEntity<Object> userResponseEntity = mock(ResponseEntity.class);

        String tokenId = "myTokenId";
        String tokenAccess = "myTokenAccess";
        String tokenRefresh = "myTokenRefresh";
        String tokenExpiration = "3600";
        String tokenType = "myTokenType";
        String tokenScope = "myTokenScope";
        DummyTokenForTest dummyTokenForTest = new DummyTokenForTest();
        dummyTokenForTest.setDummyTokenIdAsString(tokenId);
        dummyTokenForTest.setDummyAccess(tokenAccess);
        dummyTokenForTest.setDummyRefresh(tokenRefresh);
        dummyTokenForTest.setDummyExpirationAsString(tokenExpiration);
        dummyTokenForTest.setDummyType(tokenType);
        dummyTokenForTest.setDummyScope(tokenScope);

        String userId = "myUserId";
        String userLogin = "myUserLogin";
        String userName = "myUserName";
        String userEmail = "myUserEmail";
        String userPictureUrl = "myUserPicture";
        DummyUserForTest dummyUserForTest = new DummyUserForTest();
        dummyUserForTest.setDummyUserIdAsString(userId);
        dummyUserForTest.setDummyLogin(userLogin);
        dummyUserForTest.setDummyName(userName);
        dummyUserForTest.setDummyEmail(userEmail);
        dummyUserForTest.setDummyPictureUrl(userPictureUrl);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenFieldsConfiguration.getId()).thenReturn("dummyTokenIdAsString");
        when(tokenFieldsConfiguration.getAccess()).thenReturn("dummyAccess");
        when(tokenFieldsConfiguration.getRefresh()).thenReturn("dummyRefresh");
        when(tokenFieldsConfiguration.getExpiration()).thenReturn("dummyExpirationAsString");
        when(tokenFieldsConfiguration.getType()).thenReturn("dummyType");
        when(tokenFieldsConfiguration.getScope()).thenReturn("dummyScope");
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(dummyTokenForTest);
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(userUri);
        when(userConfiguration.getFields()).thenReturn(userFieldsConfiguration);
        when(userFieldsConfiguration.getId()).thenReturn("dummyUserIdAsString");
        when(userFieldsConfiguration.getLogin()).thenReturn("dummyLogin");
        when(userFieldsConfiguration.getName()).thenReturn("dummyName");
        when(userFieldsConfiguration.getEmail()).thenReturn("dummyEmail");
        when(userFieldsConfiguration.getPictureUrl()).thenReturn("dummyPictureUrl");
        when(userConfiguration.getRequest(anyMap())).thenReturn(userRequest);
        when(userConfiguration.getHttpMethod()).thenReturn(userHttpMethod);
        when(restTemplate.exchange(userUri, userHttpMethod, userRequest, Object.class)).thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(userResponseEntity.getBody()).thenReturn(dummyUserForTest);

        // Then
        UserAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
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
                        userId,
                        userName,
                        userLogin,
                        userEmail,
                        userPictureUrl
                );
    }

    @Test
    public void authenticate_returnFullyFilledAuthenticationDetails_whenAllFieldsGivenAndFoundAndTokenExpirationAsStringButNotAnIntegerRepresentation() throws AuthenticationException {
        // Given
        UserAuthenticationRequestDTO request = mock(UserAuthenticationRequestDTO.class);
        AuthenticationCustomTokenConfiguration tokenConfiguration = mock(AuthenticationCustomTokenConfiguration.class);
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = mock(AuthenticationCustomTokenFieldsConfiguration.class);

        String tokenUri = "the_token_uri";

        HttpEntity<MultiValueMap<String, String>> tokenRequest = mock(HttpEntity.class);

        ResponseEntity<Object> tokenResponseEntity = mock(ResponseEntity.class);

        AuthenticationCustomUserConfiguration userConfiguration = mock(AuthenticationCustomUserConfiguration.class);

        String userUri = "the_user_uri";

        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = mock(AuthenticationCustomUserFieldsConfiguration.class);

        HttpEntity<MultiValueMap<String, String>> userRequest = mock(HttpEntity.class);

        HttpMethod userHttpMethod = HttpMethod.GET;

        ResponseEntity<Object> userResponseEntity = mock(ResponseEntity.class);

        String tokenId = "myTokenId";
        String tokenAccess = "myTokenAccess";
        String tokenRefresh = "myTokenRefresh";
        String tokenExpiration = "not-an-integer!";
        String tokenType = "myTokenType";
        String tokenScope = "myTokenScope";
        DummyTokenForTest dummyTokenForTest = new DummyTokenForTest();
        dummyTokenForTest.setDummyTokenIdAsString(tokenId);
        dummyTokenForTest.setDummyAccess(tokenAccess);
        dummyTokenForTest.setDummyRefresh(tokenRefresh);
        dummyTokenForTest.setDummyExpirationAsString(tokenExpiration);
        dummyTokenForTest.setDummyType(tokenType);
        dummyTokenForTest.setDummyScope(tokenScope);

        String userId = "myUserId";
        String userLogin = "myUserLogin";
        String userName = "myUserName";
        String userEmail = "myUserEmail";
        String userPictureUrl = "myUserPicture";
        DummyUserForTest dummyUserForTest = new DummyUserForTest();
        dummyUserForTest.setDummyUserIdAsString(userId);
        dummyUserForTest.setDummyLogin(userLogin);
        dummyUserForTest.setDummyName(userName);
        dummyUserForTest.setDummyEmail(userEmail);
        dummyUserForTest.setDummyPictureUrl(userPictureUrl);

        // When
        when(request.getProvider()).thenReturn("provider");
        when(request.getCode()).thenReturn("some_code");
        when(request.getClientId()).thenReturn("client_id");

        when(configuration.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getUri()).thenReturn(tokenUri);
        when(tokenConfiguration.getFields()).thenReturn(tokenFieldsConfiguration);
        when(tokenFieldsConfiguration.getId()).thenReturn("dummyTokenIdAsString");
        when(tokenFieldsConfiguration.getAccess()).thenReturn("dummyAccess");
        when(tokenFieldsConfiguration.getRefresh()).thenReturn("dummyRefresh");
        when(tokenFieldsConfiguration.getExpiration()).thenReturn("dummyExpirationAsString");
        when(tokenFieldsConfiguration.getType()).thenReturn("dummyType");
        when(tokenFieldsConfiguration.getScope()).thenReturn("dummyScope");
        when(tokenConfiguration.getRequest(anyMap())).thenReturn(tokenRequest);
        when(restTemplate.postForEntity(tokenUri, tokenRequest, Object.class)).thenReturn(tokenResponseEntity);
        when(tokenResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponseEntity.getBody()).thenReturn(dummyTokenForTest);
        when(configuration.getUser()).thenReturn(userConfiguration);
        when(userConfiguration.getUri()).thenReturn(userUri);
        when(userConfiguration.getFields()).thenReturn(userFieldsConfiguration);
        when(userFieldsConfiguration.getId()).thenReturn("dummyUserIdAsString");
        when(userFieldsConfiguration.getLogin()).thenReturn("dummyLogin");
        when(userFieldsConfiguration.getName()).thenReturn("dummyName");
        when(userFieldsConfiguration.getEmail()).thenReturn("dummyEmail");
        when(userFieldsConfiguration.getPictureUrl()).thenReturn("dummyPictureUrl");
        when(userConfiguration.getRequest(anyMap())).thenReturn(userRequest);
        when(userConfiguration.getHttpMethod()).thenReturn(userHttpMethod);
        when(restTemplate.exchange(userUri, userHttpMethod, userRequest, Object.class)).thenReturn(userResponseEntity);
        when(userResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(userResponseEntity.getBody()).thenReturn(dummyUserForTest);

        // Then
        UserAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
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
                        userId,
                        userName,
                        userLogin,
                        userEmail,
                        userPictureUrl
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
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenNoTokenValidationUriFound() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";

        // When
        when(request.getToken()).thenReturn(token);
        when(tokenValidationConfiguration.getUri()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenExceptionThrownWhileCheckingToken() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        // When
        when(request.getToken()).thenReturn(token);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class))
                .thenThrow(new RestClientException("Custom token validation API failure"));

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenTokenCheckingApiCallReturnsAnErrorStatus() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        // When
        when(request.getToken()).thenReturn(token);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_returnAuthenticationDetails_whenNoTokenValidationFieldFound() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";
        String provider = "provider";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        String jwt = "generated_token";

        // When
        when(request.getToken()).thenReturn(token);
        when(request.getProvider()).thenReturn(provider);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenValidationConfiguration.getValidationField()).thenReturn(null);
        when(jwtTokenAuthenticationService.generateToken()).thenReturn(jwt);

        // Then
        AppAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getProvider()).isEqualTo(provider);
        assertThat(authenticationDetails.getAccessToken()).isEqualTo(jwt);
    }

    @Test
    public void authenticate_returnAuthenticationDetails_whenNoTokenValidationFieldNameFound() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";
        String provider = "provider";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        String jwt = "generated_token";

        AuthenticationCustomTokenValidationFieldConfiguration tokenValidationFieldConfiguration = mock(AuthenticationCustomTokenValidationFieldConfiguration.class);

        // When
        when(request.getToken()).thenReturn(token);
        when(request.getProvider()).thenReturn(provider);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenValidationConfiguration.getValidationField()).thenReturn(tokenValidationFieldConfiguration);
        when(tokenValidationFieldConfiguration.getName()).thenReturn(null);
        when(jwtTokenAuthenticationService.generateToken()).thenReturn(jwt);

        // Then
        AppAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getProvider()).isEqualTo(provider);
        assertThat(authenticationDetails.getAccessToken()).isEqualTo(jwt);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenTokenValidationFieldNameFoundButNotFoundInResponse() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        AuthenticationCustomTokenValidationFieldConfiguration tokenValidationFieldConfiguration = mock(AuthenticationCustomTokenValidationFieldConfiguration.class);

        String fieldName = "anotherFieldName";

        // When
        when(request.getToken()).thenReturn(token);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(new DummyTokenVerificationContainingNonBooleanField());
        when(tokenValidationConfiguration.getValidationField()).thenReturn(tokenValidationFieldConfiguration);
        when(tokenValidationFieldConfiguration.getName()).thenReturn(fieldName);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationConfigurationNotFoundException_whenNoTokenValidationExpectedValueAndFieldNotABoolean() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        AuthenticationCustomTokenValidationFieldConfiguration tokenValidationFieldConfiguration = mock(AuthenticationCustomTokenValidationFieldConfiguration.class);

        String fieldName = "stillActive";

        DummyTokenVerificationContainingNonBooleanField tokenVerification = new DummyTokenVerificationContainingNonBooleanField();

        // When
        when(request.getToken()).thenReturn(token);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(tokenVerification);
        when(tokenValidationConfiguration.getValidationField()).thenReturn(tokenValidationFieldConfiguration);
        when(tokenValidationFieldConfiguration.getExpectedValue()).thenReturn(null);
        when(tokenValidationFieldConfiguration.getName()).thenReturn(fieldName);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenNoTokenValidationExpectedValueAndFieldIsBooleanAndIsFalse() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        AuthenticationCustomTokenValidationFieldConfiguration tokenValidationFieldConfiguration = mock(AuthenticationCustomTokenValidationFieldConfiguration.class);

        String fieldName = "stillActive";

        DummyTokenVerificationContainingBooleanField tokenVerification = new DummyTokenVerificationContainingBooleanField();
        tokenVerification.setStillActive(false);

        // When
        when(request.getToken()).thenReturn(token);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(tokenVerification);
        when(tokenValidationConfiguration.getValidationField()).thenReturn(tokenValidationFieldConfiguration);
        when(tokenValidationFieldConfiguration.getExpectedValue()).thenReturn(null);
        when(tokenValidationFieldConfiguration.getName()).thenReturn(fieldName);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_returnAuthenticationDetails_whenNoTokenValidationExpectedValueAndFieldIsBooleanAndIsTrue() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";
        String provider = "provider";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        AuthenticationCustomTokenValidationFieldConfiguration tokenValidationFieldConfiguration = mock(AuthenticationCustomTokenValidationFieldConfiguration.class);

        String fieldName = "stillActive";

        DummyTokenVerificationContainingBooleanField tokenVerification = new DummyTokenVerificationContainingBooleanField();
        tokenVerification.setStillActive(true);

        String jwt = "generated_jwt";

        // When
        when(request.getToken()).thenReturn(token);
        when(request.getProvider()).thenReturn(provider);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(tokenVerification);
        when(tokenValidationConfiguration.getValidationField()).thenReturn(tokenValidationFieldConfiguration);
        when(tokenValidationFieldConfiguration.getExpectedValue()).thenReturn(null);
        when(tokenValidationFieldConfiguration.getName()).thenReturn(fieldName);
        when(jwtTokenAuthenticationService.generateToken()).thenReturn(jwt);

        // Then
        AppAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getProvider()).isEqualTo(provider);
        assertThat(authenticationDetails.getAccessToken()).isEqualTo(jwt);
    }

    @Test
    public void authenticate_throwAuthenticationException_whenTokenValidationExpectedValueAndFieldIsNonBooleanAndValuesAreNotEqual() {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        AuthenticationCustomTokenValidationFieldConfiguration tokenValidationFieldConfiguration = mock(AuthenticationCustomTokenValidationFieldConfiguration.class);

        String fieldName = "stillActive";

        DummyTokenVerificationContainingNonBooleanField tokenVerification = new DummyTokenVerificationContainingNonBooleanField();
        tokenVerification.setStillActive("some_validation_value");

        // When
        when(request.getToken()).thenReturn(token);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(tokenVerification);
        when(tokenValidationConfiguration.getValidationField()).thenReturn(tokenValidationFieldConfiguration);
        when(tokenValidationFieldConfiguration.getExpectedValue()).thenReturn("another_validation_value");
        when(tokenValidationFieldConfiguration.getName()).thenReturn(fieldName);

        // Then
        assertThatThrownBy(() -> authenticator.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void authenticate_returnAuthenticationDetails_whenTokenValidationExpectedValueAndFieldIsNonBooleanAndValuesAreEqual() throws AuthenticationException {
        // Given
        AppAuthenticationRequestDTO request = mock(AppAuthenticationRequestDTO.class);
        String token = "custom_token";
        String provider = "provider";

        String url = "token_validation_url";
        HttpMethod method = HttpMethod.POST;
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        HttpEntity<MultiValueMap<String, String>> customRequest = mock(HttpEntity.class);

        ResponseEntity<Object> response = mock(ResponseEntity.class);

        AuthenticationCustomTokenValidationFieldConfiguration tokenValidationFieldConfiguration = mock(AuthenticationCustomTokenValidationFieldConfiguration.class);

        String fieldName = "stillActive";

        DummyTokenVerificationContainingNonBooleanField tokenVerification = new DummyTokenVerificationContainingNonBooleanField();
        tokenVerification.setStillActive("same_value");

        String jwt = "generated_token";

        // When
        when(request.getToken()).thenReturn(token);
        when(request.getProvider()).thenReturn(provider);
        when(tokenValidationConfiguration.getUri()).thenReturn(url);
        when(tokenValidationConfiguration.getHttpMethod()).thenReturn(method);
        when(tokenValidationConfiguration.getRequest(parameters)).thenReturn(customRequest);
        when(restTemplate.exchange(url, method, customRequest, Object.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(tokenVerification);
        when(tokenValidationConfiguration.getValidationField()).thenReturn(tokenValidationFieldConfiguration);
        when(tokenValidationFieldConfiguration.getExpectedValue()).thenReturn("same_value");
        when(tokenValidationFieldConfiguration.getName()).thenReturn(fieldName);
        when(jwtTokenAuthenticationService.generateToken()).thenReturn(jwt);

        // Then
        AppAuthenticationDetailsDTO authenticationDetails = authenticator.authenticate(request);
        assertThat(authenticationDetails).isNotNull();
        assertThat(authenticationDetails.getProvider()).isEqualTo(provider);
        assertThat(authenticationDetails.getAccessToken()).isEqualTo(jwt);
    }
}
