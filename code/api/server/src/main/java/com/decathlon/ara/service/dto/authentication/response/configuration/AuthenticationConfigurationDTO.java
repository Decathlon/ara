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

package com.decathlon.ara.service.dto.authentication.response.configuration;

import com.decathlon.ara.service.dto.authentication.response.configuration.provider.AuthenticationProvidersConfigurationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

@Data
@AllArgsConstructor
@With
public class AuthenticationConfigurationDTO {

    public AuthenticationConfigurationDTO(Boolean enabled) {
        this.enabled = enabled;
    }

    private Boolean enabled;

    private AuthenticationProvidersConfigurationDTO providers;
}