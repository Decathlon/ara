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

import com.decathlon.ara.configuration.authentication.AuthenticationConfiguration;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationTokenDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationUserDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.github.GithubToken;
import com.decathlon.ara.service.dto.authentication.response.github.GithubUser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationService {

    @NonNull
    private final AuthenticationConfiguration authenticationConfiguration;

    @NonNull
    private final RestTemplate restTemplate;

    public AuthenticationDetailsDTO authenticate(AuthenticationRequestDTO request) {
        String githubClientSecret = authenticationConfiguration.getGithub().getClientSecret();
        String clientSecret = githubClientSecret;
        String clientId = request.getClientId();
        String code = request.getCode();
        String provider = request.getProvider();

        String tokenBaseUrl = "https://github.com/login/oauth/access_token";
        String tokenFinalUrl = String.format("%s?client_id=%s&client_secret=%s&code=%s", tokenBaseUrl, clientId, clientSecret, code);

        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GithubToken> tokenRequest = new HttpEntity<>(tokenHeader);

        GithubToken githubTokenDetails = restTemplate.postForObject(tokenFinalUrl, tokenRequest, GithubToken.class);

        String githubToken = githubTokenDetails.getAccessToken();

        String userUrl = "https://api.github.com/user";
        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("token %s", githubToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GithubUser> userRequest = new HttpEntity<>(userHeader);
        final ParameterizedTypeReference<GithubUser> userResponseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<GithubUser> githubUserResponseEntity = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, userResponseType);

        GithubUser user = githubUserResponseEntity.getBody();

        AuthenticationDetailsDTO authenticationDetails = new AuthenticationDetailsDTO()
                .withProvider(provider)
                .withToken(
                        new AuthenticationTokenDTO()
                                .withAccessToken(githubTokenDetails.getAccessToken())
                                .withType(githubTokenDetails.getTokenType())
                                .withScope(githubTokenDetails.getScope())
                )
                .withUser(
                        new AuthenticationUserDetailsDTO()
                                .withId(user.getId() != null ? String.valueOf(user.getId()) : null)
                                .withLogin(user.getLogin())
                                .withEmail(user.getEmail())
                                .withPicture(user.getPicture())
                );
        return authenticationDetails;
    }
}
