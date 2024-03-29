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

package com.decathlon.ara.defect.jira.api.model.project;

import com.decathlon.ara.defect.jira.api.model.avatar.JiraAvatar;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JiraProject {

    private String self;

    private String id;

    private String key;

    private String name;

    private String projectTypeKey;

    private Boolean simplified;

    @JsonProperty("avatarUrls")
    private JiraAvatar avatar;

    public String getSelf() {
        return self;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getProjectTypeKey() {
        return projectTypeKey;
    }

    public Boolean getSimplified() {
        return simplified;
    }

    public JiraAvatar getAvatar() {
        return avatar;
    }
}
