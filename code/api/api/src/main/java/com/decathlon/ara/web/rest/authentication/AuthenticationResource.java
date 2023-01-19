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

package com.decathlon.ara.web.rest.authentication;

import com.decathlon.ara.security.dto.authentication.provider.AuthenticationProviders;
import com.decathlon.ara.security.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.AUTH_PATH;

@RestController
@RequestMapping(AuthenticationResource.PATH)
public class AuthenticationResource {

    static final String PATH = AUTH_PATH;

    public static final String PATHS = PATH + "/**";

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

    private final AuthenticationService authenticationService;

    public AuthenticationResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/status")
    public Boolean getStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());

        LOG.debug("Is logged ? {}", isAuthenticated);
        return isAuthenticated;
    }

    @GetMapping("/configuration")
    public AuthenticationProviders getAuthenticationConfiguration() {
        return this.authenticationService.getAuthenticationConfiguration();
    }
}
