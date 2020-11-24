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
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationTokenDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationUserDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomAuthenticator extends Authenticator {

    @NonNull
    private final AuthenticationCustomConfiguration customConfiguration;

    @NonNull
    private final RestTemplate restTemplate;

    protected AuthenticationTokenDTO getToken(AuthenticationRequestDTO request) throws AuthenticationConfigurationNotFoundException, AuthenticationTokenNotFetchedException {
        String code = request.getCode();
        String clientId = request.getClientId();

        AuthenticationCustomTokenConfiguration tokenConfiguration = customConfiguration.getToken();
        if (tokenConfiguration == null) {
            String errorMessage = "No custom token configuration found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        String tokenUri = tokenConfiguration.getUri();
        if (StringUtils.isBlank(tokenUri)) {
            String errorMessage = "No custom token configuration URI found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = tokenConfiguration.getFields();
        if (tokenFieldsConfiguration == null) {
            String errorMessage = "No custom token configuration fields found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }

        Map<String, String> tokenParameters = Map.ofEntries(
                entry("code", code),
                entry("client_id", clientId)
        );
        HttpEntity<MultiValueMap<String, String>> tokenRequest = tokenConfiguration.getRequest(tokenParameters);

        ResponseEntity<Object> tokenResponseEntity;
        try {
            tokenResponseEntity = restTemplate.postForEntity(tokenUri, tokenRequest, Object.class);
        } catch (RestClientException exception) {
            String errorMessage = String.format("Token not fetched because an error occurred while calling the token API (%s)", tokenUri);
            log.error(errorMessage);
            throw new AuthenticationTokenNotFetchedException(errorMessage, exception);
        }
        HttpStatus tokenResponseStatus = tokenResponseEntity.getStatusCode();
        if (tokenResponseStatus.isError()) {
            String errorMessage = String.format("Token not fetched because the token API (%s) returned an error code (%s)", tokenUri, tokenResponseStatus);
            log.error(errorMessage);
            throw new AuthenticationTokenNotFetchedException(errorMessage);
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> allTokenValues = mapper.convertValue(tokenResponseEntity.getBody(), Map.class);
        Object rawTokenId = allTokenValues.get(tokenFieldsConfiguration.getId());
        String tokenId = null;
        if (rawTokenId != null) {
            tokenId = rawTokenId instanceof Integer ? (String.valueOf(rawTokenId)) : (String) rawTokenId;
        }
        String accessToken = (String) allTokenValues.get(tokenFieldsConfiguration.getAccess());
        String refreshToken = (String) allTokenValues.get(tokenFieldsConfiguration.getRefresh());
        String tokenScope = (String) allTokenValues.get(tokenFieldsConfiguration.getScope());
        String tokenType = (String) allTokenValues.get(tokenFieldsConfiguration.getType());
        Object rawTokenExpiration = allTokenValues.get(tokenFieldsConfiguration.getExpiration());
        Integer expiration = null;
        if (rawTokenExpiration != null) {
            if (rawTokenExpiration instanceof Integer) {
                expiration = (Integer) rawTokenExpiration;
            } else if (rawTokenExpiration instanceof String) {
                try {
                    expiration = Integer.parseInt((String) rawTokenExpiration);
                } catch (NumberFormatException exception) {
                    log.warn(String.format("Token (%s) expiration not saved because not an integer (%s)", tokenUri, rawTokenExpiration), exception);
                }
            }
        }
        return new AuthenticationTokenDTO()
                .withId(tokenId)
                .withAccessToken(accessToken)
                .withRefreshToken(refreshToken)
                .withExpirationDuration(expiration)
                .withScope(tokenScope)
                .withType(tokenType);
    }

    protected AuthenticationUserDetailsDTO getUser(AuthenticationTokenDTO token) throws AuthenticationConfigurationNotFoundException, AuthenticationUserNotFetchedException {
        AuthenticationCustomUserConfiguration userConfiguration = customConfiguration.getUser();
        if (userConfiguration == null) {
            String errorMessage = "No custom user configuration found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        String userUri = userConfiguration.getUri();
        if (StringUtils.isBlank(userUri)) {
            String errorMessage = "No custom user configuration URI found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = userConfiguration.getFields();
        if (userFieldsConfiguration == null) {
            String errorMessage = "No custom user configuration fields found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }

        Map<String, String> userParameters = new HashMap<>();
        String tokenType = token.getType();
        String tokenValue = token.getAccessToken();
        if (StringUtils.isNotBlank(tokenType)) {
            userParameters.put("token_type", tokenType);
        }
        if (StringUtils.isNotBlank(tokenValue)) {
            userParameters.put("token_value", tokenValue);
        }

        HttpEntity<MultiValueMap<String, String>> userRequest = userConfiguration.getRequest(userParameters);
        HttpMethod userHttpMethod = userConfiguration.getHttpMethod();
        ResponseEntity<Object> userResponseEntity;
        try {
            userResponseEntity = restTemplate.exchange(userUri, userHttpMethod, userRequest, Object.class);
        } catch (RestClientException exception) {
            String errorMessage = String.format("User not fetched because an error occurred while calling the user API (%s)", userUri);
            log.error(errorMessage);
            throw new AuthenticationUserNotFetchedException(errorMessage, exception);
        }
        HttpStatus userResponseStatus = userResponseEntity.getStatusCode();
        if (userResponseStatus.isError()) {
            String errorMessage = String.format("User not fetched because the user API (%s) returned an error code (%s)", userUri, userResponseStatus);
            log.error(errorMessage);
            throw new AuthenticationUserNotFetchedException(errorMessage);
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> allUserValues = mapper.convertValue(userResponseEntity.getBody(), Map.class);
        Object rawUserId = allUserValues.get(userFieldsConfiguration.getId());
        String userId = null;
        if (rawUserId != null) {
            userId = rawUserId instanceof Integer ? String.valueOf(rawUserId) : (String) rawUserId;
        }
        String userName = (String) allUserValues.get(userFieldsConfiguration.getName());
        String userLogin = (String) allUserValues.get(userFieldsConfiguration.getLogin());
        String userEmail = (String) allUserValues.get(userFieldsConfiguration.getEmail());
        String userPictureUrl = (String) allUserValues.get(userFieldsConfiguration.getPictureUrl());
        return new AuthenticationUserDetailsDTO()
                .withId(userId)
                .withLogin(userLogin)
                .withName(userName)
                .withEmail(userEmail)
                .withPicture(userPictureUrl);
    }
}
