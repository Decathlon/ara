/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
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

package com.decathlon.ara.service.dto.functionality;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class FunctionalityDTO {

    private Long id;

    private Long parentId;

    private double order;

    private String type;

    // NOTE: For this DTO, most validations are done in the Service, as it can represents a functionality OR folder
    // (folders have far fewer required fields)

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 512, message = "The name is required and must not exceed {max} characters.")
    private String name;

    @Size(max = 128)
    private String countryCodes;

    private Long teamId;

    private String severity;

    @Size(max = 10, message = "The created date or release must not exceed {max} characters.")
    private String created;

    private Boolean started;

    private Boolean notAutomatable;

    private Integer coveredScenarios;

    private String coveredCountryScenarios;

    private Integer ignoredScenarios;

    private String ignoredCountryScenarios;

    private String comment;
}
