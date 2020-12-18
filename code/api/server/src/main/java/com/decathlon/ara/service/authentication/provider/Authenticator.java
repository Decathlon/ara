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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public abstract class Authenticator<T extends AuthenticatorToken, U extends AuthenticatorUser> {

    protected Class<T> tokenType;

    protected Class<U> userType;

    protected JwtTokenAuthenticationService jwtTokenAuthenticationService;

    protected RestTemplate restTemplate;

    /**
     * Authenticate an user and return the authentication details if successfully authenticated
     * @param request the request
     * @return the authentication details
     * @throws AuthenticationException if the authentication failed
     */
    public UserAuthenticationDetailsDTO authenticate(UserAuthenticationRequestDTO request) throws AuthenticationException {
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
        String clientId = request.getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new AuthenticationTokenNotFetchedException("The token cannot be fetched without a client id");
        }

        T token = getToken(request);
        U user = getUser(token);
        AuthenticationUserDetailsDTO convertedUser = convertUser(user);

        UserAuthenticationDetailsDTO authenticationDetails = new UserAuthenticationDetailsDTO(convertedUser);
        authenticationDetails.setProvider(provider);
        return authenticationDetails;
    }

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
    public AppAuthenticationDetailsDTO authenticate(AppAuthenticationRequestDTO request) throws AuthenticationException {
        if (request == null) {
            throw new AuthenticationException("Authentication failed because the request cannot be null");
        }

        String accessToken = request.getToken();
        if (StringUtils.isBlank(accessToken)) {
            throw new AuthenticationException("Authentication failed because no token found in the request");
        }

        Boolean isAValidToken = isAValidToken(accessToken);
        if (isAValidToken) {
            String generatedToken = jwtTokenAuthenticationService.generateToken();
            AppAuthenticationDetailsDTO authenticationDetails = new AppAuthenticationDetailsDTO(generatedToken);
            authenticationDetails.setProvider(request.getProvider());
            return authenticationDetails;
        }
        String errorMessage = String.format("The authentication failed because the token (%s) given was not valid", accessToken);
        throw new AuthenticationException(errorMessage);
    }

    /**
     * Check whether a token is valid or not
     * @param token the token to check
     * @return true iff the token is still valid
     */
    protected Boolean isAValidToken(String token) throws AuthenticationException {
        String url = getTokenValidationUri(token);
        HttpMethod method = getTokenValidationMethod();
        HttpEntity request = getTokenValidationRequest(token);

        ResponseEntity<Object> response;
        try {
            response = restTemplate.exchange(url, method, request, Object.class);
        }
        catch (RestClientException exception) {
            return false;
        }

        HttpStatus status = response.getStatusCode();
        if (status.isError()) {
            return false;
        }

        Optional<Pair<String, Optional<Object>>> valueToCheck = getValueToCheck();
        if (!valueToCheck.isPresent()) {
            return true;
        }

        String fieldName = valueToCheck.get().getFirst();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> allValues = mapper.convertValue(response.getBody(), Map.class);

        if (!allValues.containsKey(fieldName)) {
            String errorMessage = String.format("The field %s was not found when checking the token.", fieldName);
            throw new AuthenticationException(errorMessage);
        }

        Object actualValue = allValues.get(fieldName);
        Optional<Object> expectedValue = valueToCheck.get().getSecond();
        if (expectedValue.isPresent()) {
            return expectedValue.get().equals(actualValue);
        }
        if (actualValue instanceof Boolean) {
            return (Boolean) actualValue;
        }
        if (actualValue instanceof String) {
            return Boolean.parseBoolean((String) actualValue);
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
}
