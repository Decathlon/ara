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


import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.AuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.configuration.AuthenticationConfigurationDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.decathlon.ara.web.rest.util.RestConstants.AUTH_PATH;

@Slf4j
@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationResource {

    @NonNull
    private final AuthenticationService authenticationService;

    @NonNull
    private final JwtTokenAuthenticationService jwtTokenAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<UserAuthenticationDetailsDTO> authenticate(@Valid @RequestBody UserAuthenticationRequestDTO request) {
        try {
            return authenticationService.authenticate(request);
        } catch (AuthenticationException e) {
            log.error(String.format("Error while authenticating to ARA (via %s)", request.getProvider()), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        HttpHeaders headers = jwtTokenAuthenticationService.deleteAuthenticationCookie();
        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping
    public ResponseEntity<AppAuthenticationDetailsDTO> authenticate(@Valid @RequestBody AppAuthenticationRequestDTO request) {
        try {
            return authenticationService.authenticate(request);
        } catch (AuthenticationException e) {
            log.error(String.format("Error while authenticating your application to ARA (via %s)", request.getProvider()), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/configuration")
    public ResponseEntity<AuthenticationConfigurationDTO> getAuthenticationConfiguration() {
        AuthenticationConfigurationDTO configuration = authenticationService.getAuthenticationConfiguration();
        return ResponseEntity.ok(configuration);
    }
}
