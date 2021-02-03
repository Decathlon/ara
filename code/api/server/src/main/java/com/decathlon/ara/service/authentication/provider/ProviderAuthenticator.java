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
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.dto.authentication.provider.AuthenticatorToken;
import com.decathlon.ara.service.dto.authentication.provider.AuthenticatorUser;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public abstract class ProviderAuthenticator<T extends AuthenticatorToken, U extends AuthenticatorUser, C extends AuthenticationProviderConfiguration> extends Authenticator<T, U, C> {

    public ProviderAuthenticator(
            Class<T> tokenType,
            Class<U> userType,
            Class<C> configurationType,
            JwtTokenAuthenticationService jwtTokenAuthenticationService,
            RestTemplate restTemplate
    ) {
        super(tokenType, userType, configurationType, jwtTokenAuthenticationService, restTemplate);
    }

    @Override
    protected T getToken(UserAuthenticationRequestDTO request) throws AuthenticationTokenNotFetchedException, AuthenticationConfigurationNotFoundException {
        String tokenUri = getTokenUri(request);
        HttpMethod tokenMethod = getTokenMethod();
        HttpEntity<T> tokenRequest = getTokenRequest(request);

        ResponseEntity<T> tokenResponse;
        try {
            tokenResponse = restTemplate.exchange(tokenUri, tokenMethod, tokenRequest, tokenType);
        } catch (RestClientException exception) {
            String errorMessage = String.format("Token not fetched because an error occurred while calling the API (%s)", tokenUri);
            throw new AuthenticationTokenNotFetchedException(errorMessage, exception);
        }

        HttpStatus tokenResponseStatus = tokenResponse.getStatusCode();
        if (tokenResponseStatus.isError()) {
            String errorMessage = String.format("Token not fetched because the token API (%s) returned an error code (%s)", tokenUri, tokenResponseStatus);
            throw new AuthenticationTokenNotFetchedException(errorMessage);
        }
        return tokenResponse.getBody();
    }

    @Override
    protected abstract HttpEntity<T> getTokenRequest(UserAuthenticationRequestDTO request);

    @Override
    protected U getUser(T token) throws AuthenticationUserNotFetchedException, AuthenticationConfigurationNotFoundException {
        String userUri = getUserUri();
        HttpMethod userMethod = getUserMethod();
        HttpEntity<U> userRequest = getUserRequest(token);
        ResponseEntity<U> userResponse;

        try {
            userResponse = restTemplate.exchange(userUri, userMethod, userRequest, userType);
        } catch (RestClientException exception) {
            String errorMessage = String.format("User not fetched because an error occurred while calling the API (%s)", userUri);
            throw new AuthenticationUserNotFetchedException(errorMessage, exception);
        }

        HttpStatus userResponseStatus = userResponse.getStatusCode();
        if (userResponseStatus.isError()) {
            String errorMessage = String.format("User not fetched because the user API (%s) returned an error code (%s)", userUri, userResponseStatus);
            throw new AuthenticationUserNotFetchedException(errorMessage);
        }
        return userResponse.getBody();
    }

    @Override
    protected abstract HttpEntity<U> getUserRequest(T token);
}
