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

package com.decathlon.ara.configuration.authentication;

import com.decathlon.ara.configuration.authentication.provider.AuthenticationProviderConfiguration;
import com.decathlon.ara.configuration.authentication.provider.custom.AuthenticationCustomConfiguration;
import com.decathlon.ara.configuration.authentication.provider.github.AuthenticationGithubConfiguration;
import com.decathlon.ara.configuration.authentication.provider.google.AuthenticationGoogleConfiguration;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties("authentication.provider")
public class AuthenticationConfiguration {

    private AuthenticationGithubConfiguration github;

    private AuthenticationGoogleConfiguration google;

    private AuthenticationCustomConfiguration custom;

    /**
     * Show if the authentication is enabled, i.e. at least one authentication provider is enabled
     *
     * @return true iff authentication is enabled
     */
    public boolean isEnabled() {
        return List.of(
                    Optional.ofNullable(github),
                    Optional.ofNullable(google),
                    Optional.ofNullable(custom))
                .stream().anyMatch(this::isProviderEnabled);
    }

    private boolean isProviderEnabled(Optional<? extends AuthenticationProviderConfiguration> providerConfiguration) {
        return providerConfiguration.isPresent() && providerConfiguration.get().isEnabled();
    }
}
