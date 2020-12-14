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

package com.decathlon.ara.service.authentication.provider.google;

import com.decathlon.ara.configuration.authentication.clients.google.AuthenticationGoogleConfiguration;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleToken;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleUser;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationTokenDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationUserDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
public class GoogleAuthenticator extends Authenticator {

    private final AuthenticationGoogleConfiguration googleConfiguration;

    private final RestTemplate restTemplate;

    @Autowired
    public GoogleAuthenticator(
            JwtTokenAuthenticationService jwtTokenAuthenticationService,
            AuthenticationGoogleConfiguration googleConfiguration,
            RestTemplate restTemplate
    ) {
        super(jwtTokenAuthenticationService);
        this.googleConfiguration = googleConfiguration;
        this.restTemplate = restTemplate;
    }

    @Override
    protected AuthenticationTokenDTO getToken(UserAuthenticationRequestDTO request) throws AuthenticationTokenNotFetchedException, AuthenticationConfigurationNotFoundException {
        String redirectUri = request.getRedirectUri();
        if (StringUtils.isBlank(redirectUri)) {
            String errorMessage = "The Google token cannot be fetched without the redirect uri";
            log.error(errorMessage);
            throw new AuthenticationTokenNotFetchedException(errorMessage);
        }

        String clientSecret = googleConfiguration.getClientSecret();
        if (StringUtils.isBlank(clientSecret)) {
            String errorMessage = "Google client secret not found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }

        String clientId = request.getClientId();
        String code = request.getCode();
        String grantType = "authorization_code";

        String tokenBaseUrl = "https://oauth2.googleapis.com/token";
        String tokenParameters = String.format("client_id=%s&" +
                        "client_secret=%s&" +
                        "redirect_uri=%s&" +
                        "grant_type=%s&" +
                        "code=%s",
                clientId,
                clientSecret,
                redirectUri,
                grantType,
                code
        );
        String tokenFinalUrl = String.format("%s?%s", tokenBaseUrl, tokenParameters);

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GoogleToken token;
        try {
            token = restTemplate.postForObject(tokenFinalUrl, tokenRequest, GoogleToken.class);
        } catch (RestClientException exception) {
            String errorMessage = String.format("Google token not fetched because an error occurred while calling the API (%s)", tokenFinalUrl);
            log.error(errorMessage, exception);
            throw new AuthenticationTokenNotFetchedException(errorMessage, exception);
        }

        return new AuthenticationTokenDTO()
                .withAccessToken(token.getAccessToken())
                .withExpirationDuration(token.getExpiration())
                .withScope(token.getScope())
                .withType(token.getType());
    }

    @Override
    protected AuthenticationUserDetailsDTO getUser(AuthenticationTokenDTO token) throws AuthenticationUserNotFetchedException {
        String accessToken = token.getAccessToken();
        String userUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);

        ResponseEntity<GoogleUser> userResponseEntity;
        try {
            userResponseEntity = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, GoogleUser.class);
        } catch (RestClientException exception) {
            String errorMessage = String.format("Google user not fetched because an error occurred while calling the API (%s)", userUrl);
            log.error(errorMessage, exception);
            throw new AuthenticationUserNotFetchedException(errorMessage, exception);
        }

        HttpStatus userHttpStatus = userResponseEntity.getStatusCode();
        if (userHttpStatus.isError()) {
            String errorMessage = String.format("Google user not fetched because the user API (%s) returned an error code (%s)", userUrl, userHttpStatus);
            log.error(errorMessage);
            throw new AuthenticationUserNotFetchedException(errorMessage);
        }

        GoogleUser user = userResponseEntity.getBody();
        Boolean userEmailIsNotVerified = user.getVerifiedEmail() != null && !user.getVerifiedEmail();
        if (userEmailIsNotVerified) {
            String errorMessage = String.format("The authentication has failed because this Google user account is not verified. Please login with a verified account.");
            log.error(errorMessage);
            throw new AuthenticationUserNotFetchedException(errorMessage);
        }

        return new AuthenticationUserDetailsDTO()
                .withId(user.getAccountId())
                .withLogin(user.getName())
                .withName(user.getName())
                .withEmail(user.getEmail())
                .withPicture(user.getPicture());
    }

    @Override
    protected Boolean isAValidToken(String token) {
        String url = String.format("https://oauth2.googleapis.com/tokeninfo?access_token=%s", token);
        ResponseEntity<Object> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.GET, null, Object.class);
        } catch (RestClientException exception) {
            return false;
        }

        HttpStatus status = response.getStatusCode();
        if (status.isError()) {
            return false;
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> allValues = mapper.convertValue(response.getBody(), Map.class);
        Object emailVerifiedValue = allValues.get("email_verified");
        Boolean isEmailVerified =
                emailVerifiedValue != null &&
                ((emailVerifiedValue instanceof Boolean && (Boolean) emailVerifiedValue) ||
                        (emailVerifiedValue instanceof String && Boolean.parseBoolean((String) emailVerifiedValue)));
        return isEmailVerified;
    }
}
