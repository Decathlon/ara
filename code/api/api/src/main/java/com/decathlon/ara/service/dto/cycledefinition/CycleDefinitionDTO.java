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

package com.decathlon.ara.service.dto.cycledefinition;

import static com.decathlon.ara.service.support.DtoConstants.CODE_NAME_MESSAGE;
import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CycleDefinitionDTO {

    private Long id;

    @NotNull(message = "The branch name is required.")
    @Size(min = 1, max = 16, message = "The branch is required and must not exceed {max} characters.")
    private String branch;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 16, message = "The name is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_NAME_MESSAGE)
    private String name;

    @NotNull(message = "The branch position is required.")
    private Integer branchPosition;

    public CycleDefinitionDTO() {
    }

    public CycleDefinitionDTO(Long id,
            String branch,
            String name,
            Integer branchPosition) {
        this.id = id;
        this.branch = branch;
        this.name = name;
        this.branchPosition = branchPosition;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBranch() {
        return branch;
    }

    public String getName() {
        return name;
    }

    public Integer getBranchPosition() {
        return branchPosition;
    }

}
