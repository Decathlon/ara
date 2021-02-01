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

package com.decathlon.ara.service.dto.authentication.provider.custom;

import com.decathlon.ara.service.dto.authentication.provider.AuthenticatorToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;
import java.util.Optional;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class CustomToken extends AuthenticatorToken {

    private String id;

    private String accessToken;

    private String refreshToken;

    private Integer expirationDuration;

    private Timestamp expirationTimestamp;

    private String type;

    private String scope;

    @Override
    public Optional<Integer> getAccessTokenDurationInSeconds() {
        return Optional.ofNullable(expirationDuration);
    }
}