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
import io.jsonwebtoken.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtTokenAuthenticationService {

    @NonNull
    private final AuthenticationConfiguration authenticationConfiguration;

    @NonNull
    private final AuthenticationJwtTokenConfiguration tokenConfiguration;

    private static final String tokenCookieName = "ara-access-token";

    /**
     * Create a header containing an authentication cookie, if the authentication is enabled
     * @param authenticationTokenExpirationInSeconds the authentication token expiration (in seconds), if any
     * @return a http header containing an authentication cookie, if the authentication is enabled
     * @throws AuthenticationException thrown if the header could not be generated
     */
    public HttpHeaders createAuthenticationResponseCookieHeader(Optional<Integer> authenticationTokenExpirationInSeconds) throws AuthenticationException {
        HttpHeaders responseHeaders = new HttpHeaders();

        boolean authenticationIsEnabled = authenticationConfiguration.isEnabled();
        if (authenticationIsEnabled) {
            Long ageInSeconds = getJWTTokenExpirationInSecond(authenticationTokenExpirationInSeconds);
            String jwt = generateToken(ageInSeconds);
            responseHeaders = createCookieHeaderFromJwt(Optional.of(Pair.of(jwt, ageInSeconds)));
        }

        return responseHeaders;
    }

    /**
     * Create a header containing a cookie holding a JWT token value, if given
     * @param tokenValueAndAge a pair of the JWT token value and age (in seconds)
     * @return a header containing the JWT cookie
     */
    private HttpHeaders createCookieHeaderFromJwt(Optional<Pair<String, Long>> tokenValueAndAge) {
        String cookieValue = null;
        Long cookieAge = 0L;

        if (tokenValueAndAge.isPresent()) {
            Pair<String, Long> cookieValueAndAge = tokenValueAndAge.get();
            cookieValue = cookieValueAndAge.getFirst();
            cookieAge = cookieValueAndAge.getSecond();
        }
        HttpCookie cookie = ResponseCookie.from(tokenCookieName, cookieValue)
                .maxAge(cookieAge)
                .httpOnly(true)
                .secure(tokenConfiguration.isUsingHttps())
                .sameSite("Strict")
                .path("/")
                .build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return responseHeaders;
    }

    /**
     * Generate a new JWT token
     * @param tokenAgeInSeconds the token age in seconds
     * @return the generated JWT token
     * @throws AuthenticationConfigurationNotFoundException thrown if the secret was not found in the configuration
     */
    public String generateToken(Long tokenAgeInSeconds) throws AuthenticationConfigurationNotFoundException {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        String subject = new String(bytes, StandardCharsets.UTF_8);
        Date now = new Date();
        Long tokenExpiration = tokenAgeInSeconds * 1000;
        Long duration = now.getTime() + tokenExpiration;
        Date expirationDate = new Date(duration);
        String secret = tokenConfiguration.getTokenSecret();
        if (StringUtils.isBlank(secret)) {
            throw new AuthenticationConfigurationNotFoundException("Authentication failed: ARA cannot generate a JWT token because no secret found in the configuration file");
        }
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * Get the JWT token expiration: it cannot be greater than the authentication token expiration value
     * @param authenticationTokenExpirationInSecond the authentication token expiration (in seconds), if any
     * @return the JWT token expiration
     * @throws AuthenticationException thrown if no token expiration found,
     * i.e. neither configuration nor authentication token expiration value greater than zero
     */
    public Long getJWTTokenExpirationInSecond(Optional<Integer> authenticationTokenExpirationInSecond) throws AuthenticationException {
        Long authenticationTokenExpirationInSecondsValue = authenticationTokenExpirationInSecond
                .map(Integer::longValue)
                .orElse(0L);
        Long configurationExpiration = tokenConfiguration.getAccessTokenExpirationInSecond();
        if (configurationExpiration == null) {
            configurationExpiration = 0L;
        }
        if (configurationExpiration <= 0 && authenticationTokenExpirationInSecondsValue <= 0) {
            String errorMessage = "The token must have an expiration greater than 0";
            log.error(errorMessage);
            throw new AuthenticationException(errorMessage);
        }
        if (authenticationTokenExpirationInSecondsValue <= 0) {
            return configurationExpiration;
        }
        if (configurationExpiration <= 0) {
            return authenticationTokenExpirationInSecondsValue;
        }
        if (authenticationTokenExpirationInSecondsValue >= configurationExpiration) {
            return configurationExpiration;
        }
        return authenticationTokenExpirationInSecondsValue;
    }

    /**
     * Delete the authentication cookie
     */
    public HttpHeaders deleteAuthenticationCookie() {
        return createCookieHeaderFromJwt(Optional.empty());
    }

    /**
     * Create an authentication from a request:
     * if there is a cookie containing the JWT token, then a new authentication is created
     * @param request the request
     * @return the authentication, if the cookie is found
     */
    public Optional<Authentication> getAuthenticationFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(bearerToken)) {
            String[] headerRawValue = bearerToken.split("\\s+");
            boolean requestContainsBearerInHeader = headerRawValue.length == 2 &&  "Bearer".equals(headerRawValue[0]);
            if (requestContainsBearerInHeader) {
                String jwt = headerRawValue[1];
                JwtAuthentication authentication = new JwtAuthentication(jwt);
                return Optional.of(authentication);
            }
        }
        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];
        return Arrays.stream(cookies)
                .filter(cookie -> tokenCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .map(JwtAuthentication::new)
                .map(Authentication.class::cast)
                .findFirst();
    }

    private class JwtAuthentication implements Authentication {

        private String jwt;

        public JwtAuthentication(String jwt) {
            this.jwt = jwt;
        }

        /**
         * Check if the JWT token is valid
         * @return true iff the token is valid
         */
        private boolean hasAValidToken() {
            try {
                String secret = tokenConfiguration.getTokenSecret();
                Jwts.parser().setSigningKey(secret).parse(jwt);
            }
            catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException exception) {
                String errorMessage = String.format("The JWT token (%s) was not valid: authentication failed", jwt);
                log.error(errorMessage, exception);
                return false;
            }
            return true;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return null;
        }

        @Override
        public boolean isAuthenticated() {
            return hasAValidToken();
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean implies(Subject subject) {
            return false;
        }
    }
}
