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

package com.decathlon.ara.defect.jira.api.model;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import com.decathlon.ara.defect.jira.api.model.attachment.JiraAttachment;
import com.decathlon.ara.defect.jira.api.model.comment.JiraCommentResults;
import com.decathlon.ara.defect.jira.api.model.priority.JiraPriority;
import com.decathlon.ara.defect.jira.api.model.progress.JiraProgress;
import com.decathlon.ara.defect.jira.api.model.project.JiraProject;
import com.decathlon.ara.defect.jira.api.model.resolution.JiraResolution;
import com.decathlon.ara.defect.jira.api.model.status.JiraStatus;
import com.decathlon.ara.defect.jira.api.model.type.JiraIssueType;
import com.decathlon.ara.defect.jira.api.model.user.JiraUser;
import com.decathlon.ara.defect.jira.api.model.vote.JiraVotes;
import com.decathlon.ara.defect.jira.api.model.worklog.JiraWorklog;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JiraFields {

    private List<String> labels;

    private JiraUser assignee;

    private JiraUser reporter;

    private JiraProgress progress;

    private JiraVotes votes;

    private JiraWorklog worklog;

    private JiraIssueType issueType;

    private JiraProject project;

    private String description;

    private String summary;

    @JsonProperty("duedate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    private JiraCommentResults comment;

    private JiraPriority priority;

    private JiraStatus status;

    private JiraUser creator;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime creationDate;

    @JsonProperty("attachment")
    private List<JiraAttachment> attachments;

    private JiraResolution resolution;

    @JsonProperty("resolutiondate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime resolutionDate;

    public List<String> getLabels() {
        return labels;
    }

    public JiraUser getAssignee() {
        return assignee;
    }

    public JiraUser getReporter() {
        return reporter;
    }

    public JiraProgress getProgress() {
        return progress;
    }

    public JiraVotes getVotes() {
        return votes;
    }

    public JiraWorklog getWorklog() {
        return worklog;
    }

    public JiraIssueType getIssueType() {
        return issueType;
    }

    public JiraProject getProject() {
        return project;
    }

    public String getDescription() {
        return description;
    }

    public String getSummary() {
        return summary;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public JiraCommentResults getComment() {
        return comment;
    }

    public JiraPriority getPriority() {
        return priority;
    }

    public JiraStatus getStatus() {
        return status;
    }

    public JiraUser getCreator() {
        return creator;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public List<JiraAttachment> getAttachments() {
        return attachments;
    }

    public JiraResolution getResolution() {
        return resolution;
    }

    public ZonedDateTime getResolutionDate() {
        return resolutionDate;
    }
}
