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

package com.decathlon.ara.service.authentication;

import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationService {

    @NonNull
    private final RestTemplate restTemplate;

    @NonNull
    private final AuthenticationStrategy authenticationStrategy;

    /**
     * Authenticate an user and return the authentication details
     * @param request the request sent to authenticate the user
     * @return the authentication response
     * @throws AuthenticationException thrown if the authentication has failed
     */
    public ResponseEntity<UserAuthenticationDetailsDTO> authenticate(UserAuthenticationRequestDTO request) throws AuthenticationException {
        Authenticator authenticator = getAuthenticator(request);
        ResponseEntity<UserAuthenticationDetailsDTO> authenticationResponse = authenticator.authenticate(request);
        return authenticationResponse;
    }

    /**
     * Authenticate an  external application and return the authentication details
     * @param request the request sent to authenticate the application
     * @return the authentication response
     * @throws AuthenticationException thrown if the authentication has failed
     */
    public ResponseEntity<AppAuthenticationDetailsDTO> authenticate(AppAuthenticationRequestDTO request) throws AuthenticationException {
        Authenticator authenticator = getAuthenticator(request);
        ResponseEntity<AppAuthenticationDetailsDTO> authenticationResponse = authenticator.authenticate(request);
        return authenticationResponse;
    }

    /**
     * Get an authenticator from a request, if any one matching
     * @param request the request
     * @return the authenticator
     * @throws AuthenticationException if no authenticator matching the request found
     */
    private Authenticator getAuthenticator(AuthenticationRequestDTO request) throws AuthenticationException {
        if (request == null) {
            throw new AuthenticationException("Couldn't not authenticate because the request was null");
        }
        String providerName = request.getProvider();
        Authenticator authenticator = authenticationStrategy
                .getAuthenticator(providerName)
                .orElseThrow(() ->
                        new AuthenticationException(String.format("The provider given (%s) is not supported", providerName))
                );
        return authenticator;
    }
}
