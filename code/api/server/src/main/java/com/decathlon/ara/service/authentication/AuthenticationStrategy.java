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

import com.decathlon.ara.service.authentication.provider.Authenticator;
import com.decathlon.ara.service.authentication.provider.custom.CustomAuthenticator;
import com.decathlon.ara.service.authentication.provider.github.GithubAuthenticator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AuthenticationStrategy {

    @NonNull
    private final CustomAuthenticator customAuthenticator;

    @NonNull
    private final GithubAuthenticator githubAuthenticator;

    /**
     * If found, return an authenticator matching the provider name given
     * @param providerName the provider name (e.g. "custom", "google", "github", etc.)
     *                     Note that it is case insensitive
     * @return the matching authenticator, if any
     */
    public Optional<Authenticator> getAuthenticator(String providerName) {
        if (StringUtils.isBlank(providerName)) {
            return Optional.empty();
        }

        Map<String, Authenticator> authenticatorsByProviderName = Map.ofEntries(
                entry("custom", customAuthenticator),
                entry("github", githubAuthenticator)
        );

        Authenticator authenticator = authenticatorsByProviderName.get(providerName.toLowerCase());
        return Optional.ofNullable(authenticator);
    }
}
