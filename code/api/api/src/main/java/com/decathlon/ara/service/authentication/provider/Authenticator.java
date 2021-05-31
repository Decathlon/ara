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

package com.decathlon.ara.service.authentication.provider;

import com.decathlon.ara.configuration.authentication.provider.AuthenticationProviderConfiguration;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.dto.authentication.provider.AuthenticatorToken;
import com.decathlon.ara.service.dto.authentication.provider.AuthenticatorUser;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationUserDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public abstract class Authenticator<T extends AuthenticatorToken, U extends AuthenticatorUser, C extends AuthenticationProviderConfiguration> {

    protected Class<T> tokenType;

    protected Class<U> userType;

    protected Class<C> configurationType;

    protected JwtTokenAuthenticationService jwtTokenAuthenticationService;

    protected RestTemplate restTemplate;

    /**
     * Authenticate an user and return the authentication details if successfully authenticated
     * @param request the request
     * @return the authentication details
     * @throws AuthenticationException if the authentication failed
     */
    public ResponseEntity<UserAuthenticationDetailsDTO> authenticate(UserAuthenticationRequestDTO request) throws AuthenticationException {
        if (request == null) {
            throw new AuthenticationException("The request cannot be null");
        }
        String provider = request.getProvider();
        if (StringUtils.isBlank(provider)) {
            throw new AuthenticationException("The provider is required");
        }
        String code = request.getCode();
        if (StringUtils.isBlank(code)) {
            throw new AuthenticationTokenNotFetchedException("The token cannot be fetched without a code");
        }
        C configuration = getAuthenticatorConfiguration();
        if (!configuration.isEnabled()) {
            throw new AuthenticationException(String.format("You cannot authenticate with this provider (%s) because it is not enabled", provider));
        }

        T token = getToken(request);
        U user = getUser(token);
        AuthenticationUserDetailsDTO convertedUser = convertUser(user);

        UserAuthenticationDetailsDTO authenticationDetails = new UserAuthenticationDetailsDTO().withUser(convertedUser);

        Optional<Integer> accessTokenDurationInSeconds = token.getAccessTokenDurationInSeconds();
        HttpHeaders headers = jwtTokenAuthenticationService.createAuthenticationResponseCookieHeader(accessTokenDurationInSeconds);

        return ResponseEntity.ok().headers(headers).body(authenticationDetails);
    }

    /**
     * Get the authenticator authentication
     * @return the authenticator authentication
     */
    protected abstract C getAuthenticatorConfiguration();

    /**
     * Get token from a request
     * @param request the request
     * @return the token
     * @throws AuthenticationTokenNotFetchedException if the token could not be fetched
     * @throws AuthenticationConfigurationNotFoundException if any required configuration value was missing
     */
    protected abstract T getToken(UserAuthenticationRequestDTO request) throws AuthenticationTokenNotFetchedException, AuthenticationConfigurationNotFoundException;

    /**
     * Get the user from a token
     * @param token the token
     * @return the user
     * @throws AuthenticationUserNotFetchedException if the user could not be fetched
     * @throws AuthenticationConfigurationNotFoundException if any required configuration value was missing
     */
    protected abstract U getUser(T token) throws AuthenticationUserNotFetchedException, AuthenticationConfigurationNotFoundException;

    /**
     * Get the uri to call to fetch the token
     * @param request the authentication request
     * @return the token api uri
     * @throws AuthenticationTokenNotFetchedException if the token was not fetched due to an error
     * @throws AuthenticationConfigurationNotFoundException if the token was not fetched because a configuration value was missing
     */
    protected abstract String getTokenUri(UserAuthenticationRequestDTO request) throws AuthenticationTokenNotFetchedException, AuthenticationConfigurationNotFoundException;

    /**
     * Get the token http method
     * @return the token http method
     */
    protected abstract HttpMethod getTokenMethod();

    /**
     * Get the token request given to call the api
     * @param request the authentication request
     * @return the token api request
     */
    protected abstract HttpEntity getTokenRequest(UserAuthenticationRequestDTO request);

    /**
     * Get the uri to call to fetch the user
     * @return the user api uri
     * @throws AuthenticationConfigurationNotFoundException if the user was not fetched because a configuration value was missing
     */
    protected abstract String getUserUri() throws AuthenticationConfigurationNotFoundException;

    /**
     * Get the user http method
     * @return the user http method
     */
    protected abstract HttpMethod getUserMethod();

    /**
     * Get the user request given to call the api
     * @param token the token previously fetched
     * @return the user api request
     */
    protected abstract HttpEntity getUserRequest(T token);

    /**
     * Convert an user returned by the oauth provider api into an ARA user
     * @param user the user to convert
     * @return the converted ARA user
     */
    protected abstract AuthenticationUserDetailsDTO convertUser(U user);

    /**
     * Authenticate an application and return the authentication details if successfully authenticated
     * @param request the request
     * @return the authentication details
     * @throws AuthenticationException if the authentication failed
     */
    public ResponseEntity<AppAuthenticationDetailsDTO> authenticate(AppAuthenticationRequestDTO request) throws AuthenticationException {
        if (request == null) {
            throw new AuthenticationException("Authentication failed because the request cannot be null");
        }

        String provider = request.getProvider();
        if (StringUtils.isBlank(provider)) {
            throw new AuthenticationException("Authentication failed because no provider given");
        }

        String accessToken = request.getToken();
        if (StringUtils.isBlank(accessToken)) {
            throw new AuthenticationException("Authentication failed because no token found in the request");
        }

        C configuration = getAuthenticatorConfiguration();
        if (!configuration.isEnabled()) {
            throw new AuthenticationException(String.format("You cannot authenticate with this provider (%s) because it is not enabled", provider));
        }

        Pair<Boolean, Optional<Integer>> validTokenPair = isAValidToken(accessToken);
        boolean isAValidToken = validTokenPair.getFirst();
        if (isAValidToken) {
            Optional<Integer> tokenAge = validTokenPair.getSecond();
            Long tokenAgeValue = jwtTokenAuthenticationService.getJWTTokenExpirationInSecond(tokenAge);
            String generatedToken = jwtTokenAuthenticationService.generateToken(tokenAgeValue);
            AppAuthenticationDetailsDTO authenticationDetails = new AppAuthenticationDetailsDTO(generatedToken);
            return ResponseEntity.ok().body(authenticationDetails);
        }
        String errorMessage = String.format("The authentication failed because the token (%s) given was not valid", accessToken);
        throw new AuthenticationException(errorMessage);
    }

    /**
     * Check whether a token is valid or not. If valid, can also return the age (in second)
     * @param token the token to check
     * @return true iff the token is still valid
     */
    protected Pair<Boolean, Optional<Integer>> isAValidToken(String token) throws AuthenticationException {
        String url = getTokenValidationUri(token);
        HttpMethod method = getTokenValidationMethod();
        HttpEntity request = getTokenValidationRequest(token);

        ResponseEntity<Object> response;
        try {
            response = restTemplate.exchange(url, method, request, Object.class);
        }
        catch (RestClientException exception) {
            return Pair.of(false, Optional.empty());
        }

        HttpStatus status = response.getStatusCode();
        if (status.isError()) {
            return Pair.of(false, Optional.empty());
        }

        Object responseBody = response.getBody();
        if (responseBody == null) {
            return Pair.of(true, Optional.empty());
        }

        Optional<Integer> expiration = getTokenRemainingTimeInSecond(responseBody);
        Optional<Pair<String, Optional<Object>>> valueToCheck = getValueToCheck();
        if (!valueToCheck.isPresent()) {
            return Pair.of(true, expiration);
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> allValues = mapper.convertValue(responseBody, Map.class);

        String fieldName = valueToCheck.get().getFirst();

        if (!allValues.containsKey(fieldName)) {
            String errorMessage = String.format("The field %s was not found when checking the token.", fieldName);
            throw new AuthenticationException(errorMessage);
        }

        Object actualValue = allValues.get(fieldName);
        Optional<Object> expectedValue = valueToCheck.get().getSecond();
        if (expectedValue.isPresent()) {
            return Pair.of(expectedValue.get().equals(actualValue), expiration);
        }
        if (actualValue instanceof Boolean) {
            return Pair.of((Boolean) actualValue, expiration);
        }
        if (actualValue instanceof String) {
            return Pair.of(Boolean.parseBoolean((String) actualValue), expiration);
        }
        String errorMessage = "Authentication failed: the token validation api returned an unexpected value";
        throw new AuthenticationException(errorMessage);
    }

    /**
     * Get the uri to call to check whether a token is valid or not
     * @param token the token to check
     * @return the token validation uri
     * @throws AuthenticationConfigurationNotFoundException if a configuration value was required but not found
     */
    protected abstract String getTokenValidationUri(String token) throws AuthenticationConfigurationNotFoundException;

    /**
     * Get the token validation http method
     * @return the token validation http method
     */
    protected abstract HttpMethod getTokenValidationMethod();

    /**
     * Get the token validation http request
     * @param token the token to check
     * @return the http request
     */
    protected abstract HttpEntity getTokenValidationRequest(String token);

    /**
     * After calling the token verification api, it may be required to check a value in the returned object.
     * In this case, the method returns a pair of key (the field name) and (expected) value
     * Here are the main cases:
     * > Empty optional: no value to check, if the api returned no error code, then the token is valid
     * > The pair contains the field name, but the optional containing the value is empty: the field name must be a boolean (or a string containing a boolean value, i.e "true" or "false")
     * In this case iff the value is evaluated as true (or "true") then the token is valid.
     * > The pair contains the field name, and the value: this value is compared to the value found in the response.
     * The token is valid only if the value returned and the value contained in the pair (in the optional) are equals
     * @return the value to check, if any
     */
    protected abstract Optional<Pair<String, Optional<Object>>> getValueToCheck();

    /**
     * Get the remaining token age, in seconds
     * @param validationToken the validation token details
     * @return the remaining token age, if any
     */
    private Optional<Integer> getTokenRemainingTimeInSecond(Object validationToken) {
        if (validationToken == null) {
            return Optional.empty();
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> allValues = mapper.convertValue(validationToken, Map.class);

        Optional<String> expirationFieldName = getTokenExpirationFieldName();
        if (expirationFieldName.isPresent()) {
            Object expiration = allValues.get(expirationFieldName.get());
            if (expiration instanceof String) {
                return Optional.of(Integer.valueOf((String) expiration));
            }
            if (expiration instanceof Integer) {
                return Optional.of((Integer) expiration);
            }
        }

        Optional<String> timestampFieldName = getTokenExpirationTimestampFieldName();
        if (timestampFieldName.isPresent()) {
            Object rawTimestamp = allValues.get(timestampFieldName.get());
            Integer timestamp = null;
            if (rawTimestamp instanceof String) {
                timestamp = Integer.valueOf((String) rawTimestamp);
            }
            if (rawTimestamp instanceof Integer) {
                timestamp = (Integer) rawTimestamp;
            }
            if (timestamp != null) {
                Long remainingTimeInMillisecond = timestamp.longValue() * 1000 - new Date().getTime();
                Integer remainingTimeInSecond = remainingTimeInMillisecond >= 0 ? remainingTimeInMillisecond.intValue() / 1000 : null ;
                return Optional.ofNullable(remainingTimeInSecond);
            }
        }
        return Optional.empty();
    }

    /**
     * Get token expiration field name, if any
     * @return token expiration field name, if any
     */
    protected abstract Optional<String> getTokenExpirationFieldName();

    /**
     * Get token expiration timestamp field name, if any
     * @return token expiration timestamp field name, if any
     */
    protected abstract Optional<String> getTokenExpirationTimestampFieldName();
}
