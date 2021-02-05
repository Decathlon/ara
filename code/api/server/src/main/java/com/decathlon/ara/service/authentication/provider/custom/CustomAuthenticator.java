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

import com.decathlon.ara.configuration.authentication.provider.custom.AuthenticationCustomConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.token.AuthenticationCustomTokenConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.token.AuthenticationCustomTokenFieldsConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.user.AuthenticationCustomUserConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.user.AuthenticationCustomUserFieldsConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.validation.AuthenticationCustomTokenValidationConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.validation.AuthenticationCustomTokenValidationFieldConfiguration;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.provider.custom.CustomToken;
import com.decathlon.ara.service.dto.authentication.provider.custom.CustomUser;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationUserDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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
import java.util.Optional;

import static java.util.Map.entry;

@Slf4j
@Service
public class CustomAuthenticator extends Authenticator<CustomToken, CustomUser, AuthenticationCustomConfiguration> {

    private final AuthenticationCustomTokenConfiguration tokenConfiguration;

    private final AuthenticationCustomUserConfiguration userConfiguration;

    private final AuthenticationCustomTokenValidationConfiguration tokenValidationConfiguration;

    @Autowired
    public CustomAuthenticator(
            JwtTokenAuthenticationService jwtTokenAuthenticationService,
            RestTemplate restTemplate,
            AuthenticationCustomTokenConfiguration tokenConfiguration,
            AuthenticationCustomUserConfiguration userConfiguration,
            AuthenticationCustomTokenValidationConfiguration tokenValidationConfiguration,
            AuthenticationCustomConfiguration customConfiguration
    ) {
        super(jwtTokenAuthenticationService, restTemplate, customConfiguration);
        this.tokenConfiguration = tokenConfiguration;
        this.userConfiguration = userConfiguration;
        this.tokenValidationConfiguration = tokenValidationConfiguration;
    }

    @Override
    protected CustomToken getToken(UserAuthenticationRequestDTO request) throws AuthenticationConfigurationNotFoundException, AuthenticationTokenNotFetchedException {
        String tokenUri = getTokenUri(request);
        HttpMethod tokenMethod = getTokenMethod();
        HttpEntity<MultiValueMap<String, String>> tokenRequest = getTokenRequest(request);

        AuthenticationCustomTokenFieldsConfiguration tokenFieldsConfiguration = tokenConfiguration.getFields();
        if (tokenFieldsConfiguration == null) {
            String errorMessage = "No custom token configuration fields found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }

        ResponseEntity<Object> tokenResponseEntity;
        try {
            tokenResponseEntity = restTemplate.exchange(tokenUri, tokenMethod, tokenRequest, Object.class);
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
        return new CustomToken()
                .withId(tokenId)
                .withAccessToken(accessToken)
                .withRefreshToken(refreshToken)
                .withExpirationDuration(expiration)
                .withScope(tokenScope)
                .withType(tokenType);
    }

    @Override
    protected String getTokenUri(UserAuthenticationRequestDTO request) throws AuthenticationConfigurationNotFoundException {
        String tokenUri = tokenConfiguration.getUri();
        if (StringUtils.isBlank(tokenUri)) {
            String errorMessage = "No custom token configuration URI found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        return tokenUri;
    }

    @Override
    protected HttpMethod getTokenMethod() {
        return tokenConfiguration.getHttpMethod();
    }

    @Override
    protected HttpEntity<MultiValueMap<String, String>> getTokenRequest(UserAuthenticationRequestDTO request) {
        String code = request.getCode();
        Map<String, String> tokenParameters = Map.ofEntries(entry("code", code));
        return tokenConfiguration.getRequest(tokenParameters);
    }

    @Override
    protected String getUserUri() throws AuthenticationConfigurationNotFoundException {
        String userUri = userConfiguration.getUri();
        if (StringUtils.isBlank(userUri)) {
            String errorMessage = "No custom user configuration URI found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        return userUri;
    }

    @Override
    protected HttpMethod getUserMethod() {
        return userConfiguration.getHttpMethod();
    }

    @Override
    protected HttpEntity<MultiValueMap<String, String>> getUserRequest(CustomToken token) {
        Map<String, String> userParameters = new HashMap<>();
        String tokenType = token.getType();
        String tokenValue = token.getAccessToken();
        if (StringUtils.isNotBlank(tokenType)) {
            userParameters.put("token_type", tokenType);
        }
        if (StringUtils.isNotBlank(tokenValue)) {
            userParameters.put("token_value", tokenValue);
        }

        return userConfiguration.getRequest(userParameters);
    }

    @Override
    protected AuthenticationUserDetailsDTO convertUser(CustomUser user) {
        return new AuthenticationUserDetailsDTO()
                .withId(user.getId())
                .withLogin(user.getLogin())
                .withName(user.getName())
                .withEmail(user.getEmail())
                .withPicture(user.getPicture());
    }

    @Override
    protected CustomUser getUser(CustomToken token) throws AuthenticationConfigurationNotFoundException, AuthenticationUserNotFetchedException {
        String userUri = getUserUri();
        AuthenticationCustomUserFieldsConfiguration userFieldsConfiguration = userConfiguration.getFields();
        if (userFieldsConfiguration == null) {
            String errorMessage = "No custom user configuration fields found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }

        HttpEntity<MultiValueMap<String, String>> userRequest = getUserRequest(token);
        HttpMethod userMethod = getUserMethod();
        ResponseEntity<Object> userResponseEntity;
        try {
            userResponseEntity = restTemplate.exchange(userUri, userMethod, userRequest, Object.class);
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
        return new CustomUser()
                .withId(userId)
                .withLogin(userLogin)
                .withName(userName)
                .withEmail(userEmail)
                .withPicture(userPictureUrl);
    }

    @Override
    protected String getTokenValidationUri(String token) throws AuthenticationConfigurationNotFoundException {
        String url = tokenValidationConfiguration.getUri();
        if (StringUtils.isBlank(url)) {
            String errorMessage = "To validate the token, a validation url is required but was not found in the configuration";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        return url;
    }

    @Override
    protected HttpMethod getTokenValidationMethod() {
        return tokenValidationConfiguration.getHttpMethod();
    }

    @Override
    protected HttpEntity getTokenValidationRequest(String token) {
        Map<String, String> parameters = Map.ofEntries(
                entry("token_value", token)
        );
        return tokenValidationConfiguration.getRequest(parameters);
    }

    @Override
    protected Optional<Pair<String, Optional<Object>>> getValueToCheck() {
        AuthenticationCustomTokenValidationFieldConfiguration validationField = tokenValidationConfiguration.getValidationField();
        if (validationField == null) {
            return Optional.empty();
        }
        String fieldName = validationField.getName();
        if (StringUtils.isBlank(fieldName)) {
            return Optional.empty();
        }
        Object expectedFieldValue = validationField.getExpectedValue();
        return Optional.of(Pair.of(fieldName, Optional.ofNullable(expectedFieldValue)));
    }

    @Override
    protected Optional<String> getTokenExpirationFieldName() {
        AuthenticationCustomTokenValidationFieldConfiguration validationField = tokenValidationConfiguration.getValidationField();
        if (validationField == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(validationField.getRemainingTime());
    }

    @Override
    protected Optional<String> getTokenExpirationTimestampFieldName() {
        AuthenticationCustomTokenValidationFieldConfiguration validationField = tokenValidationConfiguration.getValidationField();
        if (validationField == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(validationField.getExpirationTimestamp());
    }
}
