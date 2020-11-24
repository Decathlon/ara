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

package com.decathlon.ara.service.authentication.provider.github;

import com.decathlon.ara.configuration.authentication.clients.github.AuthenticationGithubConfiguration;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubToken;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubUser;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationTokenDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationUserDetailsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GithubAuthenticator extends Authenticator {

    @NonNull
    private final AuthenticationGithubConfiguration githubConfiguration;

    @NonNull
    private final RestTemplate restTemplate;

    @Override
    protected AuthenticationTokenDTO getToken(AuthenticationRequestDTO request) throws AuthenticationConfigurationNotFoundException, AuthenticationTokenNotFetchedException {
        String clientSecret = githubConfiguration.getClientSecret();
        if (StringUtils.isBlank(clientSecret)) {
            String errorMessage = "Github client secret not found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        String clientId = request.getClientId();
        String code = request.getCode();

        String tokenBaseUrl = "https://github.com/login/oauth/access_token";
        String tokenParameters = String.format("client_id=%s&client_secret=%s&code=%s", clientId, clientSecret, code);
        String tokenFinalUrl = String.format("%s?%s", tokenBaseUrl, tokenParameters);

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GithubToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GithubToken token;
        try {
            token = restTemplate.postForObject(tokenFinalUrl, tokenRequest, GithubToken.class);
        } catch (RestClientException exception) {
            String errorMessage = String.format("Github token not fetched because an error occurred while calling the API (%s)", tokenFinalUrl);
            log.error(errorMessage, exception);
            throw new AuthenticationTokenNotFetchedException(errorMessage, exception);
        }

        return new AuthenticationTokenDTO()
                .withAccessToken(token.getAccessToken())
                .withType(token.getTokenType())
                .withScope(token.getScope());
    }

    @Override
    protected AuthenticationUserDetailsDTO getUser(AuthenticationTokenDTO token) throws AuthenticationUserNotFetchedException {
        String accessToken = token.getAccessToken();
        String userUrl = "https://api.github.com/user";
        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("token %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GithubUser> userRequest = new HttpEntity<>(userHeader);
        final ParameterizedTypeReference<GithubUser> userResponseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<GithubUser> responseEntity;
        try {
            responseEntity = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, userResponseType);
        } catch (RestClientException exception) {
            String errorMessage = String.format("Github user not fetched because an error occurred while calling the API (%s)", userUrl);
            log.error(errorMessage, exception);
            throw new AuthenticationUserNotFetchedException(errorMessage, exception);
        }

        HttpStatus userHttpStatus = responseEntity.getStatusCode();
        if (userHttpStatus.isError()) {
            String errorMessage = String.format("Github user not fetched because the user API (%s) returned an error code (%s)", userUrl, userHttpStatus);
            log.error(errorMessage);
            throw new AuthenticationUserNotFetchedException(errorMessage);
        }

        GithubUser user = responseEntity.getBody();
        return new AuthenticationUserDetailsDTO()
                .withId(user.getId() != null ? String.valueOf(user.getId()) : null)
                .withLogin(user.getLogin())
                .withEmail(user.getEmail())
                .withPicture(user.getPicture());
    }
}
