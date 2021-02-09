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

package com.decathlon.ara.configuration.security.jwt;

import com.decathlon.ara.configuration.authentication.AuthenticationConfiguration;
import com.decathlon.ara.configuration.security.AuthenticationJwtTokenConfiguration;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenAuthenticationServiceTest {

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationJwtTokenConfiguration tokenConfiguration;

    @InjectMocks
    private JwtTokenAuthenticationService tokenAuthenticationService;

    @Test
    void createAuthenticationResponseCookieHeader_returnEmptyHeader_whenAuthenticationIsNotEnabled() throws AuthenticationException {
        // Given

        // When
        when(authenticationConfiguration.isEnabled()).thenReturn(false);

        // Then
        HttpHeaders header = tokenAuthenticationService.createAuthenticationResponseCookieHeader(Optional.empty());
        assertThat(header).isNotNull().isEmpty();
    }

    @Test
    void createAuthenticationResponseCookieHeader_throwAuthenticationException_whenTokenExpirationNotFound() {
        // Given

        // When
        when(authenticationConfiguration.isEnabled()).thenReturn(true);
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(0L);

        // Then
        assertThatThrownBy(() -> tokenAuthenticationService.createAuthenticationResponseCookieHeader(Optional.empty()))
                .isExactlyInstanceOf(AuthenticationException.class);
    }

    @Test
    void createAuthenticationResponseCookieHeader_throwAuthenticationConfigurationNotFoundException_whenTokenSecretNotFound() {
        // Given
        Long tokenExpirationInMilliseconds = 3600000L;

        // When
        when(authenticationConfiguration.isEnabled()).thenReturn(true);
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(tokenExpirationInMilliseconds);
        when(tokenConfiguration.getTokenSecret()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> tokenAuthenticationService.createAuthenticationResponseCookieHeader(Optional.empty()))
                .isExactlyInstanceOf(AuthenticationConfigurationNotFoundException.class);
    }

    @Test
    void createAuthenticationResponseCookieHeader_returnHeaderContainingCookie_whenNoError() throws AuthenticationException {
        // Given
        Long tokenExpirationInSeconds = 3600L;

        String secret = "well_hidden_jwt_secret!";

        // When
        when(authenticationConfiguration.isEnabled()).thenReturn(true);
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(tokenExpirationInSeconds);
        when(tokenConfiguration.getTokenSecret()).thenReturn(secret);
        when(tokenConfiguration.isUsingHttps()).thenReturn(true);

        // Then
        HttpHeaders header = tokenAuthenticationService.createAuthenticationResponseCookieHeader(Optional.empty());
        assertThat(header).isNotNull().isNotEmpty();
        assertThat(header.get(HttpHeaders.SET_COOKIE).get(0))
                .contains(
                        "Path=/;",
                        "Max-Age=3600;",
                        "Secure;",
                        "HttpOnly;",
                        "SameSite=Strict"
                );
    }

    @Test
    void deleteAuthenticationCookie_returnHeaderContainingEmptyCookie_whenIsUsingHttps() {
        // Given

        // When
        when(tokenConfiguration.isUsingHttps()).thenReturn(true);

        // Then
        HttpHeaders header = tokenAuthenticationService.deleteAuthenticationCookie();
        assertThat(header).isNotNull().isNotEmpty();
        assertThat(header.get(HttpHeaders.SET_COOKIE).get(0))
                .contains(
                        "ara-access-token=;",
                        "Path=/;",
                        "Max-Age=0;",
                        "Secure;",
                        "HttpOnly;",
                        "SameSite=Strict"
                );
    }

    @Test
    void deleteAuthenticationCookie_returnHeaderContainingEmptyCookie_whenIsNotUsingHttps() {
        // Given

        // When
        when(tokenConfiguration.isUsingHttps()).thenReturn(false);

        // Then
        HttpHeaders header = tokenAuthenticationService.deleteAuthenticationCookie();
        assertThat(header).isNotNull().isNotEmpty();
        assertThat(header.get(HttpHeaders.SET_COOKIE).get(0))
                .contains(
                        "ara-access-token=;",
                        "Path=/;",
                        "Max-Age=0;",
                        "HttpOnly;",
                        "SameSite=Strict"
                )
                .doesNotContain(
                        "Secure"
                );
    }

    @Test
    void getAuthenticationFromRequest_returnEmptyAuthentication_whenHeaderAuthorizationNotFoundAndCookieNull() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        // When
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // Then
        Optional<Authentication> authentication = tokenAuthenticationService.getAuthenticationFromRequest(request);
        assertThat(authentication)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getAuthenticationFromRequest_returnEmptyAuthentication_whenHeaderAuthorizationDoesNotContainBearerAndCookieNull() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        String authorization = "Some value";

        // When
        when(request.getHeader("Authorization")).thenReturn(authorization);
        when(request.getCookies()).thenReturn(null);

        // Then
        Optional<Authentication> authentication = tokenAuthenticationService.getAuthenticationFromRequest(request);
        assertThat(authentication)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getAuthenticationFromRequest_returnEmptyAuthentication_whenHeaderAuthorizationContainsOnlyTheBearerAndCookieNull() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        String authorization = "Bearer   ";

        // When
        when(request.getHeader("Authorization")).thenReturn(authorization);
        when(request.getCookies()).thenReturn(null);

        // Then
        Optional<Authentication> authentication = tokenAuthenticationService.getAuthenticationFromRequest(request);
        assertThat(authentication)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getAuthenticationFromRequest_returnEmptyAuthentication_whenHeaderAuthorizationNotFoundAndCookiesEmpty() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        Cookie[] cookies = new Cookie[0];

        // When
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(cookies);

        // Then
        Optional<Authentication> authentication = tokenAuthenticationService.getAuthenticationFromRequest(request);
        assertThat(authentication)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getAuthenticationFromRequest_returnEmptyAuthentication_whenHeaderAuthorizationNotFoundAndCookieNameNotFound() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        Cookie cookie1 = mock(Cookie.class);
        Cookie cookie2 = mock(Cookie.class);
        Cookie cookie3 = mock(Cookie.class);
        Cookie[] cookies = new Cookie[] {
                cookie1,
                cookie2,
                cookie3
        };

        // When
        when(request.getHeader("Authorization")).thenReturn(null);
        when(cookie1.getName()).thenReturn("not_a_jwt_access_token");
        when(cookie2.getName()).thenReturn("neither_this_one");
        when(cookie3.getName()).thenReturn("nor_this_one");
        when(request.getCookies()).thenReturn(cookies);

        // Then
        Optional<Authentication> authentication = tokenAuthenticationService.getAuthenticationFromRequest(request);
        assertThat(authentication)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getAuthenticationFromRequest_returnAuthentication_whenHeaderAuthorizationBearerFound() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        String authorization = "Bearer the_jwt_authorization_token";

        // When
        when(request.getHeader("Authorization")).thenReturn(authorization);

        // Then
        Optional<Authentication> authentication = tokenAuthenticationService.getAuthenticationFromRequest(request);
        assertThat(authentication)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void getAuthenticationFromRequest_returnAuthentication_whenHeaderAuthorizationNotFoundButCookieFound() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        Cookie cookie1 = mock(Cookie.class);
        Cookie cookie2 = mock(Cookie.class);
        Cookie[] cookies = new Cookie[] {
                cookie1,
                cookie2
        };

        // When
        when(request.getHeader("Authorization")).thenReturn(null);
        when(cookie1.getName()).thenReturn("not_a_jwt_access_token");
        when(cookie2.getName()).thenReturn("ara-access-token");
        when(cookie2.getValue()).thenReturn("the_jwt_token");
        when(request.getCookies()).thenReturn(cookies);

        // Then
        Optional<Authentication> authentication = tokenAuthenticationService.getAuthenticationFromRequest(request);
        assertThat(authentication)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void getJWTTokenExpiration_throwAuthenticationException_whenTokenExpirationConfigurationNotFoundAndAuthenticationTokenExpirationHasNoValue() {
        // Given

        // When
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> tokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.empty()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void getJWTTokenExpiration_throwAuthenticationException_whenConfigurationTokenExpirationZeroAndAuthenticationTokenExpirationHasNoValue() {
        // Given

        // When
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(0L);

        // Then
        assertThatThrownBy(() -> tokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.empty()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void getJWTTokenExpiration_throwAuthenticationException_whenConfigurationTokenExpirationZeroAndAuthenticationTokenExpirationZero() {
        // Given

        // When
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(0L);

        // Then
        assertThatThrownBy(() -> tokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.of(0)))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void getJWTTokenExpiration_returnConfigurationExpiration_whenConfigurationTokenExpirationGreaterThanZeroAndAuthenticationTokenExpirationZero() throws AuthenticationException {
        // Given
        Long configurationExpiration = 3600L;

        // When
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(configurationExpiration);

        // Then
        Long expiration = tokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.of(0));
        assertThat(expiration).isEqualTo(configurationExpiration);
    }

    @Test
    void getJWTTokenExpiration_returnAuthenticationTokenExpiration_whenTokenExpirationConfigurationNotFoundAndAuthenticationTokenExpirationGreaterThanZero() throws AuthenticationException {
        // Given
        Integer authenticationTokenExpiration = 3600;

        // When
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(null);

        // Then
        Long expiration = tokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.of(authenticationTokenExpiration));
        assertThat(expiration).isEqualTo(authenticationTokenExpiration.longValue());
    }

    @Test
    void getJWTTokenExpiration_returnConfigurationExpiration_whenTokenExpirationConfigurationLowerThanAuthenticationTokenExpiration() throws AuthenticationException {
        // Given
        Integer authenticationTokenExpiration = 3600;
        Long configurationExpiration = 1800L;

        // When
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(configurationExpiration);

        // Then
        Long expiration = tokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.of(authenticationTokenExpiration));
        assertThat(expiration).isEqualTo(configurationExpiration);
    }

    @Test
    void getJWTTokenExpiration_returnAuthenticationTokenExpiration_whenTokenExpirationConfigurationGreaterThanAuthenticationTokenExpiration() throws AuthenticationException {
        // Given
        Integer authenticationTokenExpiration = 1800;
        Long configurationExpiration = 3600L;

        // When
        when(tokenConfiguration.getAccessTokenExpirationInSecond()).thenReturn(configurationExpiration);

        // Then
        Long expiration = tokenAuthenticationService.getJWTTokenExpirationInSecond(Optional.of(authenticationTokenExpiration));
        assertThat(expiration).isEqualTo(authenticationTokenExpiration.longValue());
    }
}
