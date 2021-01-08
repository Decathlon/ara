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

package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.configuration.security.jwt.JwtTokenAuthenticationService;
import com.decathlon.ara.service.authentication.AuthenticationService;
import com.decathlon.ara.service.authentication.exception.AuthenticationException;
import com.decathlon.ara.service.dto.authentication.request.AppAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.request.UserAuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.app.AppAuthenticationDetailsDTO;
import com.decathlon.ara.service.dto.authentication.response.user.UserAuthenticationDetailsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationResource {

    @NonNull
    private final AuthenticationService authenticationService;

    @NonNull
    private final JwtTokenAuthenticationService jwtTokenAuthenticationService;

    @PostMapping("/login")
    @Timed
    public ResponseEntity<UserAuthenticationDetailsDTO> authenticate(@Valid @RequestBody UserAuthenticationRequestDTO request) {
        try {
            UserAuthenticationDetailsDTO authenticationDetails = authenticationService.authenticate(request);
            HttpHeaders headers = jwtTokenAuthenticationService.createAuthenticationResponseCookieHeader();
            return ResponseEntity.ok().headers(headers).body(authenticationDetails);
        } catch (AuthenticationException e) {
            log.error(String.format("Error while authenticating to ARA (via %s)", request.getProvider()), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    @Timed
    public ResponseEntity<Void> logout() {
        HttpHeaders headers = jwtTokenAuthenticationService.deleteAuthenticationCookie();
        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping
    @Timed
    public ResponseEntity<AppAuthenticationDetailsDTO> authenticate(@Valid @RequestBody AppAuthenticationRequestDTO request) {
        try {
            AppAuthenticationDetailsDTO authenticationDetails = authenticationService.authenticate(request);
            return ResponseEntity.ok().body(authenticationDetails);
        } catch (AuthenticationException e) {
            log.error(String.format("Error while authenticating your application to ARA (via %s)", request.getProvider()), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
