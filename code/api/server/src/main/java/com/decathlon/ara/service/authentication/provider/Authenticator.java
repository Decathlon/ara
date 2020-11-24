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

import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationTokenDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationUserDetailsDTO;
import org.apache.commons.lang3.StringUtils;

public abstract class Authenticator {

    public AuthenticationDetailsDTO authenticate(AuthenticationRequestDTO request) throws AuthenticationException {
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

        return new AuthenticationDetailsDTO()
                .withProvider(provider)
                .withToken(token)
                .withUser(user);
    }

    protected abstract AuthenticationTokenDTO getToken(AuthenticationRequestDTO request) throws AuthenticationTokenNotFetchedException, AuthenticationConfigurationNotFoundException;

    protected abstract AuthenticationUserDetailsDTO getUser(AuthenticationTokenDTO token) throws AuthenticationUserNotFetchedException, AuthenticationConfigurationNotFoundException;
}
