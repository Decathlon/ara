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

package com.decathlon.ara.service.dto.team;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TeamDTO {

    private Long id;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 128, message = "The name is required and must not exceed {max} characters.")
    private String name;

    private boolean assignableToProblems;

    private boolean assignableToFunctionalities;

    public TeamDTO() {
    }

    public TeamDTO(Long id,
            String name,
            boolean assignableToProblems, boolean assignableToFunctionalities) {
        this.id = id;
        this.name = name;
        this.assignableToProblems = assignableToProblems;
        this.assignableToFunctionalities = assignableToFunctionalities;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public boolean isAssignableToProblems() {
        return assignableToProblems;
    }

    public boolean isAssignableToFunctionalities() {
        return assignableToFunctionalities;
    }

}
