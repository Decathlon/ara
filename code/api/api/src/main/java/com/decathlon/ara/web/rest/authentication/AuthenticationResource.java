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


import com.decathlon.ara.configuration.authentication.provider.AuthenticationDetailsConf;
import com.decathlon.ara.service.authentication.AuthenticationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.decathlon.ara.web.rest.util.RestConstants.AUTH_PATH;

@Slf4j
@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationResource {

    @NonNull
    private final AuthenticationService authenticationService;

    @GetMapping("/status")
    public Boolean getStatus(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated= authentication != null &&
                authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());


        log.debug("Is logged ?" + isAuthenticated);
        log.debug("Is anonymous ?" + authentication.getName());
        //return new ResponseEntity(isAuthenticated ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
        return isAuthenticated;
    }

    @GetMapping("/configuration")
    public List<AuthenticationDetailsConf.Oauth2ProvidersInfos> getAuthenticationConfiguration() {
        List<AuthenticationDetailsConf.Oauth2ProvidersInfos> providersInfos = this.authenticationService.getProvidersInfos();
        log.debug("providers null?" + (providersInfos == null) );
        return providersInfos;
    }
}
