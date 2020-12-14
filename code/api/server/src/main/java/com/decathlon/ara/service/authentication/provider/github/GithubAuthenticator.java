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
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubToken;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubUser;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationTokenDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationUserDetailsDTO;
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
public class GithubAuthenticator extends Authenticator {

    private final AuthenticationGithubConfiguration githubConfiguration;

    private final RestTemplate restTemplate;

    @Autowired
    public GithubAuthenticator(
            JwtTokenAuthenticationService jwtTokenAuthenticationService,
            AuthenticationGithubConfiguration githubConfiguration,
            RestTemplate restTemplate
    ) {
        super(jwtTokenAuthenticationService);
        this.githubConfiguration = githubConfiguration;
        this.restTemplate = restTemplate;
    }

    @Override
    protected AuthenticationTokenDTO getToken(UserAuthenticationRequestDTO request) throws AuthenticationConfigurationNotFoundException, AuthenticationTokenNotFetchedException {
        String clientSecret = githubConfiguration.getClientSecret();
        if (StringUtils.isBlank(clientSecret)) {
            String errorMessage = "Github client secret not found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        String clientId = request.getClientId();
        String code = request.getCode();

        String scope = "user:email%20read:user";

        String tokenBaseUrl = "https://github.com/login/oauth/access_token";
        String tokenParameters = String.format("client_id=%s&scope=%s&client_secret=%s&code=%s", clientId, scope, clientSecret, code);
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
                .withName(user.getName())
                .withLogin(user.getLogin())
                .withEmail(user.getEmail())
                .withPicture(user.getPicture());
    }

    @Override
    protected Boolean isAValidToken(String token) {
        String url = "https://api.github.com";
        HttpHeaders header = new HttpHeaders();
        String authorization = String.format("token %s", token);
        header.set("Authorization", authorization);
        HttpEntity<Object> request = new HttpEntity<>(header);

        ResponseEntity<Object> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, request, Object.class);
        } catch (RestClientException exception) {
            return false;
        }

        HttpStatus status = response.getStatusCode();
        return status.is2xxSuccessful();
    }
}
