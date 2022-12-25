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

package com.decathlon.ara.service.dto.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

import static com.decathlon.ara.service.support.DtoConstants.CODE_MESSAGE;
import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;
import static com.decathlon.ara.service.util.DateService.DATE_FORMAT_YEAR_TO_SECOND;

public class ProjectDTO {

    private Long id;

    @NotNull(message = "The code is required.")
    @Size(min = 1, max = 32, message = "The code is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_MESSAGE)
    private String code;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 64, message = "The name is required and must not exceed {max} characters.")
    private String name;

    /**
     * True to use that project as the default one appearing at ARA's client startup when no project code is present in
     * URL. Only one project can be declared as the default.
     */
    private boolean defaultAtStartup;

    @JsonProperty("creation_date")
    @JsonFormat(pattern = DATE_FORMAT_YEAR_TO_SECOND)
    private Date creationDate;

    @JsonProperty("creation_user")
    private String creationUserLogin;

    @JsonProperty("update_date")
    @JsonFormat(pattern = DATE_FORMAT_YEAR_TO_SECOND)
    private Date updateDate;

    @JsonProperty("update_user")
    private String updateUserLogin;

    public ProjectDTO() {
    }

    public ProjectDTO(Long id,
            String code,
            String name,
            boolean defaultAtStartup) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.defaultAtStartup = defaultAtStartup;
    }

    public ProjectDTO(String code, String name, boolean defaultAtStartup) {
        this.code = code;
        this.name = name;
        this.defaultAtStartup = defaultAtStartup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultAtStartup() {
        return defaultAtStartup;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationUserLogin() {
        return creationUserLogin;
    }

    public void setCreationUserLogin(String creationUserLogin) {
        this.creationUserLogin = creationUserLogin;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUserLogin() {
        return updateUserLogin;
    }

    public void setUpdateUserLogin(String updateUserLogin) {
        this.updateUserLogin = updateUserLogin;
    }
}
