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

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.service.authentication.AuthenticationService;
import com.decathlon.ara.service.dto.authentication.request.AuthenticationRequestDTO;
import com.decathlon.ara.service.dto.authentication.response.AuthenticationDetailsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.decathlon.ara.web.rest.util.RestConstants.AUTH_PATH;

@Slf4j
@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationResource {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping
    @Timed
    public ResponseEntity<AuthenticationDetailsDTO> authenticate(@Valid @RequestBody AuthenticationRequestDTO request) {
        AuthenticationDetailsDTO authenticationDetails = authenticationService.authenticate(request);
        return ResponseEntity.ok(authenticationDetails);
    }
}
