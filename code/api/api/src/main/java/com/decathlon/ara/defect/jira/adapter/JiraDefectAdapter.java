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

package com.decathlon.ara.defect.jira.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.atlassian.jira.issue.IssueKey;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.defect.jira.api.JiraRestClient;
import com.decathlon.ara.defect.jira.api.mapper.JiraMapper;
import com.decathlon.ara.defect.jira.api.model.JiraIssue;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.exception.BadRequestException;

@Service
public class JiraDefectAdapter implements DefectAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(JiraDefectAdapter.class);

    private SettingProviderService settingProviderService;

    private JiraRestClient jiraRestClient;

    private JiraMapper jiraMapper;

    public JiraDefectAdapter(SettingProviderService settingProviderService, JiraRestClient jiraRestClient,
            JiraMapper jiraMapper) {
        this.settingProviderService = settingProviderService;
        this.jiraRestClient = jiraRestClient;
        this.jiraMapper = jiraMapper;
    }

    @Override
    public List<Defect> getStatuses(long projectId, List<String> ids) throws FetchException {
        try {
            List<String> validJiraKeys = ids.stream()
                    .filter(id -> isValidId(id))
                    .collect(Collectors.toList());
            List<JiraIssue> jiraIssues = jiraRestClient.getIssuesFromKeys(projectId, validJiraKeys);
            List<Defect> defects = jiraMapper.toDefects(jiraIssues);
            return defects;
        } catch (BadRequestException e) {
            String jiraIds = String.join(", ", ids);
            String errorMessage = String.format("DEFECT|jira|Error while fetching the following ids from Jira: [%s]", jiraIds);
            LOG.error(errorMessage, e);
            throw new FetchException(errorMessage, e);
        }
    }

    @Override
    public List<Defect> getChangedDefects(long projectId, Date startDate) throws FetchException {
        try {
            List<JiraIssue> jiraIssues = jiraRestClient.getUpdatedIssues(projectId, startDate);
            List<Defect> defects = jiraMapper.toDefects(jiraIssues);
            return defects;
        } catch (BadRequestException e) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String formattedDate = dateFormat.format(startDate);
            String errorMessage = String.format("DEFECT|jira|An error occurred while fetching the updated issues after %s", formattedDate);
            LOG.warn(errorMessage, e);
            throw new FetchException(errorMessage, e);
        }
    }

    @Override
    public boolean isValidId(String id) {
        return IssueKey.isValidKey(id);
    }

    @Override
    public String getIdFormatHint(long projectId) {
        return "The id (issue key) should match the Jira issue code validation: " +
                "i.e. It must contain a dash ('-'), and the substring following the final dash must be a valid number. " +
                "It validates that there is at least one character preceding the dash, " +
                "but it DOES NOT VALIDATE that this is an existing key on this instance of JIRA. " +
                "For instance, \"ABC_X20-1193\" is a valid issue key, whereas \"XXX\" or \"123\" are not." +
                "For more details, please check the Jira documentation.";
    }

    @Override
    public String getCode() {
        return "jira";
    }

    @Override
    public String getName() {
        return "Jira";
    }

    @Override
    public List<SettingDTO> getSettingDefinitions() {
        return this.settingProviderService.getDefectJiraDefinitions();
    }
}
