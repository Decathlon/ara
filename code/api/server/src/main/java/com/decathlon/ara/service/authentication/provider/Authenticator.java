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
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationTokenDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationUserDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public abstract class Authenticator {

    private JwtTokenAuthenticationService jwtTokenAuthenticationService;

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

        AuthenticationTokenDTO token = getToken(request);
        AuthenticationUserDetailsDTO user = getUser(token);

        UserAuthenticationDetailsDTO authenticationDetails = new UserAuthenticationDetailsDTO(user);
        authenticationDetails.setProvider(provider);
        return authenticationDetails;
    }

    /**
     * Get token details from a request
     * @param request the request
     * @return the token details
     * @throws AuthenticationTokenNotFetchedException if the token could not be fetched
     * @throws AuthenticationConfigurationNotFoundException if any required configuration value was missing
     */
    protected abstract AuthenticationTokenDTO getToken(UserAuthenticationRequestDTO request) throws AuthenticationTokenNotFetchedException, AuthenticationConfigurationNotFoundException;

    /**
     * Get the user details from a token
     * @param token the token
     * @return the user details
     * @throws AuthenticationUserNotFetchedException if the user could not be fetched
     * @throws AuthenticationConfigurationNotFoundException if any required configuration value was missing
     */
    protected abstract AuthenticationUserDetailsDTO getUser(AuthenticationTokenDTO token) throws AuthenticationUserNotFetchedException, AuthenticationConfigurationNotFoundException;

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
    protected abstract Boolean isAValidToken(String token) throws AuthenticationConfigurationNotFoundException;
}
