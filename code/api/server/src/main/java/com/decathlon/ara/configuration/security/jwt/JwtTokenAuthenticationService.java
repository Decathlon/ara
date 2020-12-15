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
import io.jsonwebtoken.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtTokenAuthenticationService {

    @NonNull
    private final AuthenticationConfiguration authenticationConfiguration;

    @NonNull
    private final AuthenticationJwtTokenConfiguration tokenConfiguration;

    private final String tokenCookieName = "ara-access-token";

    /**
     * Create a header containing an authentication cookie, if the authentication is enabled
     * @return a http header containing an authentication cookie, if the authentication is enabled
     */
    public HttpHeaders createAuthenticationResponseCookieHeader() throws AuthenticationConfigurationNotFoundException {
        HttpHeaders responseHeaders = new HttpHeaders();

        Boolean authenticationIsEnabled = authenticationConfiguration.isEnabled();
        if (authenticationIsEnabled) {
            String jwt = generateToken();
            responseHeaders = createCookieHeaderFromJwt(Optional.of(jwt));
        }

        return responseHeaders;
    }

    /**
     * Create a header containing a cookie holding a JWT token value, if given
     * @param jwt the JWT token value
     * @return a header containing the JWT cookie
     */
    private HttpHeaders createCookieHeaderFromJwt(Optional<String> jwt) {
        String tokenCookieValue = null;
        Long cookieAge = 0L;

        if (jwt.isPresent()) {
            tokenCookieValue = jwt.get();
            cookieAge = tokenConfiguration.getAccessTokenExpirationInSecond();
        }
        HttpCookie cookie = ResponseCookie.from(tokenCookieName, tokenCookieValue)
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
     * @return the generated JWT token
     */
    public String generateToken() throws AuthenticationConfigurationNotFoundException {
        Integer min = 10;
        Integer max = 30;
        String subject = RandomStringUtils.randomAscii(min, max);
        Date now = new Date();
        Long tokenExpiration = tokenConfiguration.getAccessTokenExpirationInMillisecond();
        if (tokenExpiration == null || tokenExpiration == 0L) {
            throw new AuthenticationConfigurationNotFoundException("Authentication failed: ARA cannot generate a JWT token because token expiration value was not found in the configuration file");
        }
        Long duration = now.getTime() + tokenExpiration;
        Date expirationDate = new Date(duration);
        String secret = tokenConfiguration.getTokenSecret();
        if (StringUtils.isBlank(secret)) {
            throw new AuthenticationConfigurationNotFoundException("Authentication failed: ARA cannot generate a JWT token because no secret found in the configuration file");
        }
        String token = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        return token;
    }

    /**
     * Delete the authentication cookie
     */
    public HttpHeaders deleteAuthenticationCookie() {
        HttpHeaders responseHeaders = createCookieHeaderFromJwt(Optional.empty());
        return responseHeaders;
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
            Boolean requestContainsBearerInHeader = headerRawValue.length == 2 &&  "Bearer".equals(headerRawValue[0]);
            if (requestContainsBearerInHeader) {
                String jwt = headerRawValue[1];
                JwtAuthentication authentication = new JwtAuthentication(jwt);
                return Optional.of(authentication);
            }
        }
        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];
        Optional<Authentication> authentication = Arrays.stream(cookies)
                .filter(cookie -> tokenCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .map(JwtAuthentication::new)
                .map(Authentication.class::cast)
                .findFirst();
        return authentication;
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
