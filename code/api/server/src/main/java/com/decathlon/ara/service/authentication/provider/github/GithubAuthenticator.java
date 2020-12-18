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
import com.decathlon.ara.service.authentication.provider.ProviderAuthenticator;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubToken;
import com.decathlon.ara.service.dto.authentication.provider.github.GithubUser;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.user.AuthenticationUserDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
public class GithubAuthenticator extends ProviderAuthenticator<GithubToken, GithubUser> {

    private final AuthenticationGithubConfiguration githubConfiguration;

    @Autowired
    public GithubAuthenticator(
            JwtTokenAuthenticationService jwtTokenAuthenticationService,
            AuthenticationGithubConfiguration githubConfiguration,
            RestTemplate restTemplate
    ) {
        super(GithubToken.class, GithubUser.class, jwtTokenAuthenticationService, restTemplate);
        this.githubConfiguration = githubConfiguration;
    }

    @Override
    protected String getTokenUri(UserAuthenticationRequestDTO request) throws AuthenticationConfigurationNotFoundException {
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
        return tokenFinalUrl;
    }

    @Override
    protected HttpMethod getTokenMethod() {
        return HttpMethod.POST;
    }

    @Override
    protected HttpEntity<GithubToken> getTokenRequest(UserAuthenticationRequestDTO request) {
        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GithubToken> tokenRequest = new HttpEntity<>(tokenHeader);
        return tokenRequest;
    }

    @Override
    protected String getUserUri() {
        return "https://api.github.com/user";
    }

    @Override
    protected HttpMethod getUserMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected HttpEntity<GithubUser> getUserRequest(GithubToken token) {
        String accessToken = token.getAccessToken();
        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("token %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GithubUser> userRequest = new HttpEntity<>(userHeader);
        return userRequest;
    }

    @Override
    protected AuthenticationUserDetailsDTO convertUser(GithubUser user) {
        return new AuthenticationUserDetailsDTO()
                .withId(user.getId() != null ? String.valueOf(user.getId()) : null)
                .withName(user.getName())
                .withLogin(user.getLogin())
                .withEmail(user.getEmail())
                .withPicture(user.getPicture());
    }

    @Override
    protected String getTokenValidationUri(String token) {
        return "https://api.github.com";
    }

    @Override
    protected HttpMethod getTokenValidationMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected HttpEntity<Object> getTokenValidationRequest(String token) {
        HttpHeaders header = new HttpHeaders();
        String authorization = String.format("token %s", token);
        header.set("Authorization", authorization);
        HttpEntity<Object> request = new HttpEntity<>(header);
        return request;
    }

    @Override
    protected Optional<Pair<String, Optional<Object>>> getValueToCheck() {
        return Optional.empty();
    }
}
