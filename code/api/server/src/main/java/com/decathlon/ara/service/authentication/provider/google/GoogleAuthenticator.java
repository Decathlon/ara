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

import com.decathlon.ara.configuration.AraConfiguration;
import com.decathlon.ara.configuration.authentication.provider.google.AuthenticationGoogleConfiguration;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationConfigurationNotFoundException;
import com.decathlon.ara.service.authentication.exception.AuthenticationTokenNotFetchedException;
import com.decathlon.ara.service.authentication.exception.AuthenticationUserNotFetchedException;
import com.decathlon.ara.service.authentication.provider.ProviderAuthenticator;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleToken;
import com.decathlon.ara.service.dto.authentication.provider.google.GoogleUser;
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
public class GoogleAuthenticator extends ProviderAuthenticator<GoogleToken, GoogleUser, AuthenticationGoogleConfiguration> {

    private final AuthenticationGoogleConfiguration googleConfiguration;

    private final AraConfiguration araConfiguration;

    @Autowired
    public GoogleAuthenticator(
            JwtTokenAuthenticationService jwtTokenAuthenticationService,
            AuthenticationGoogleConfiguration googleConfiguration,
            RestTemplate restTemplate,
            AraConfiguration araConfiguration
    ) {
        super(GoogleToken.class, GoogleUser.class, AuthenticationGoogleConfiguration.class, jwtTokenAuthenticationService, restTemplate);
        this.googleConfiguration = googleConfiguration;
        this.araConfiguration = araConfiguration;
    }

    /**
     * Get Google redirect uri
     * @return redirect uri
     * @throws AuthenticationConfigurationNotFoundException thrown if no client base url found
     */
    private String getRedirectUri() throws AuthenticationConfigurationNotFoundException {
        String frontBaseUrl = araConfiguration.getClientBaseUrl();
        if (StringUtils.isBlank(frontBaseUrl)) {
            String errorMessage = "The Google token cannot be fetched without the redirect uri: require the client base url";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }
        String separator = frontBaseUrl.endsWith("/") ? "" : "/";
        return String.format("%s%slogin/google", frontBaseUrl, separator);
    }

    @Override
    protected String getTokenUri(UserAuthenticationRequestDTO request) throws AuthenticationTokenNotFetchedException, AuthenticationConfigurationNotFoundException {
        String redirectUri = getRedirectUri();

        String clientSecret = googleConfiguration.getClientSecret();
        if (StringUtils.isBlank(clientSecret)) {
            String errorMessage = "Google client secret not found";
            log.error(errorMessage);
            throw new AuthenticationConfigurationNotFoundException(errorMessage);
        }

        String clientId = googleConfiguration.getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new AuthenticationConfigurationNotFoundException("The Google token cannot be fetched without a client id");
        }
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
        return tokenFinalUrl;
    }

    @Override
    protected HttpMethod getTokenMethod() {
        return HttpMethod.POST;
    }

    @Override
    protected HttpEntity getTokenRequest(UserAuthenticationRequestDTO request) {
        HttpHeaders tokenHeader = new HttpHeaders();
        tokenHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GoogleToken> tokenRequest = new HttpEntity<>(tokenHeader);
        return tokenRequest;
    }

    @Override
    protected String getUserUri() {
        return "https://www.googleapis.com/oauth2/v3/userinfo";
    }

    @Override
    protected HttpMethod getUserMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected HttpEntity<GoogleUser> getUserRequest(GoogleToken token) {
        String accessToken = token.getAccessToken();
        HttpHeaders userHeader = new HttpHeaders();
        String authorization = String.format("Bearer %s", accessToken);
        userHeader.set("Authorization", authorization);
        HttpEntity<GoogleUser> userRequest = new HttpEntity<>(userHeader);
        return userRequest;
    }

    @Override
    protected AuthenticationUserDetailsDTO convertUser(GoogleUser user) {
        return new AuthenticationUserDetailsDTO()
                .withId(user.getAccountId())
                .withLogin(user.getName())
                .withName(user.getName())
                .withEmail(user.getEmail())
                .withPicture(user.getPicture());
    }

    @Override
    protected AuthenticationGoogleConfiguration getAuthenticatorConfiguration() {
        return googleConfiguration;
    }

    @Override
    protected GoogleUser getUser(GoogleToken token) throws AuthenticationUserNotFetchedException, AuthenticationConfigurationNotFoundException {
        GoogleUser user = super.getUser(token);

        boolean userEmailIsNotVerified = user.getVerifiedEmail() != null && !user.getVerifiedEmail();
        if (userEmailIsNotVerified) {
            String errorMessage = String.format("The authentication has failed because this Google user account is not verified. Please login with a verified account.");
            log.error(errorMessage);
            throw new AuthenticationUserNotFetchedException(errorMessage);
        }

        return user;
    }

    @Override
    protected String getTokenValidationUri(String token) {
        return String.format("https://oauth2.googleapis.com/tokeninfo?access_token=%s", token);
    }

    @Override
    protected HttpMethod getTokenValidationMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected HttpEntity getTokenValidationRequest(String token) {
        return null;
    }

    @Override
    protected Optional<Pair<String, Optional<Object>>> getValueToCheck() {
        return Optional.of(Pair.of("email_verified", Optional.empty()));
    }

    @Override
    protected Optional<String> getTokenExpirationFieldName() {
        return Optional.of("expires_in");
    }

    @Override
    protected Optional<String> getTokenExpirationTimestampFieldName() {
        return Optional.of("exp");
    }
}
