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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.configuration.authentication.provider.AuthProvidersConf;
import com.decathlon.ara.configuration.authentication.provider.AuthProvidersConf.Oauth2ProvidersInfos;

@Service
@Transactional
public class AuthenticationService {

    private final AuthProvidersConf authDetailsConf;

    @Value("${ara.loginStartingUrl}")
    private String loginStartingUrl;

    @Value("${ara.logoutProcessingUrl}")
    private String logoutProcessingUrl;

    @Autowired
    public AuthenticationService(AuthProvidersConf authDetailsConf) {
        this.authDetailsConf = authDetailsConf;
    }

    public AuthenticationConf getAuthenticationConf() {
        return new AuthenticationConf(
                this.authDetailsConf.getConf(),
                this.loginStartingUrl,
                this.logoutProcessingUrl);
    }

    public static class AuthenticationConf {

        private List<Oauth2ProvidersInfos> providers;
        private String loginStartingUrl;
        private String logoutProcessingUrl;

        public AuthenticationConf(List<Oauth2ProvidersInfos> providers, String loginStartingUrl, String logoutProcessingUrl) {
            this.providers = providers;
            this.loginStartingUrl = loginStartingUrl;
            this.logoutProcessingUrl = logoutProcessingUrl;
        }

        public List<Oauth2ProvidersInfos> getProviders() {
            return providers;
        }

        public void setProviders(List<Oauth2ProvidersInfos> providers) {
            this.providers = providers;
        }

        public String getLoginStartingUrl() {
            return loginStartingUrl;
        }

        public void setLoginStartingUrl(String loginStartingUrl) {
            this.loginStartingUrl = loginStartingUrl;
        }

        public String getLogoutProcessingUrl() {
            return logoutProcessingUrl;
        }

        public void setLogoutProcessingUrl(String logoutProcessingUrl) {
            this.logoutProcessingUrl = logoutProcessingUrl;
        }
    }

}
