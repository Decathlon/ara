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

package com.decathlon.ara.defect.jira.api.mapper;

import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.defect.jira.api.model.JiraFields;
import com.decathlon.ara.defect.jira.api.model.JiraIssue;
import com.decathlon.ara.defect.jira.api.model.status.JiraStatus;
import com.decathlon.ara.defect.jira.api.model.status.JiraStatusCategory;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JiraMapper {

    /**
     * Convert a {@link JiraIssue} to a {@link Defect}
     * @param jiraIssue the Jira issue to convert
     * @return the defect
     */
    public Defect toDefect(JiraIssue jiraIssue) {
        String defectId = jiraIssue.getKey();
        Date closeDateTime = null;
        ProblemStatus problemStatus = ProblemStatus.OPEN;

        JiraFields issueFields = jiraIssue.getFields();
        if (issueFields != null) {
            JiraStatus status = issueFields.getStatus();
            if (status != null) {
                JiraStatusCategory category = status.getCategory();
                if (category != null) {
                    String categoryKey = category.getKey();
                    if (StringUtils.isNotBlank(categoryKey) && JiraStatusCategory.DONE.equals(categoryKey.toLowerCase())) {
                        problemStatus = ProblemStatus.CLOSED;
                    }
                }
            }

            ZonedDateTime resolutionZonedDateTime = issueFields.getResolutionDate();
            if (resolutionZonedDateTime != null) {
                closeDateTime = Date.from(resolutionZonedDateTime.toInstant());
            }
        }

        return new Defect(defectId, problemStatus, closeDateTime);
    }

    /**
     * Convert a list of {@link JiraIssue} to a list of {@link Defect}
     * @param jiraIssues the Jira issues to convert
     * @return the defects
     */
    public List<Defect> toDefects(List<JiraIssue> jiraIssues) {
        if (CollectionUtils.isEmpty(jiraIssues)) {
            return new ArrayList<>();
        }
        return jiraIssues.stream()
                .map(this::toDefect)
                .toList();
    }
}
