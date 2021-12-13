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

package com.decathlon.ara.defect.jira.api.model.comment;

import java.time.ZonedDateTime;

import com.decathlon.ara.defect.jira.api.model.user.JiraUser;
import com.fasterxml.jackson.annotation.JsonFormat;

public class JiraComment {

    private String self;

    private String id;

    private JiraUser author;

    private String body;

    private JiraUser updateAuthor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime updated;

    private Boolean jsdPublic;

    public String getSelf() {
        return self;
    }

    public String getId() {
        return id;
    }

    public JiraUser getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public JiraUser getUpdateAuthor() {
        return updateAuthor;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public Boolean getJsdPublic() {
        return jsdPublic;
    }
}
