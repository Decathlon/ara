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

package com.decathlon.ara.defect.github;

import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.support.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link DefectAdapter} for Github issues system.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.1.0
 */
@Service
public class GithubDefectAdapter implements DefectAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GithubDefectAdapter.class);

    private static final String UNABLE_TO_ACCESS_GITHUB = "DEFECT|Unable to access Github.";
    private GithubRestClient restClient;

    private SettingService settingService;

    private SettingProviderService settingProviderService;

    public GithubDefectAdapter(GithubRestClient restClient, SettingService settingService,
            SettingProviderService settingProviderService) {
        this.restClient = restClient;
        this.settingService = settingService;
        this.settingProviderService = settingProviderService;
    }

    @Override
    public List<Defect> getStatuses(long projectId, List<String> ids) throws FetchException {
        String repositoryOwner = this.settingService.get(projectId, Settings.DEFECT_GITHUB_OWNER);
        String repositoryName = this.settingService.get(projectId, Settings.DEFECT_GITHUB_REPONAME);
        String authorizationToken = this.settingService.get(projectId, Settings.DEFECT_GITHUB_TOKEN);
        List<Long> issueIds = ids.stream()
                .map(Long::valueOf)
                .toList();

        try {
            return this.restClient
                    .forOwnerAndRepository(repositoryOwner, repositoryName)
                    .withToken(authorizationToken)
                    .requestIssues(issueIds)
                    .stream()
                    .map(this::toDefect)
                    .toList();
        } catch (IOException | URISyntaxException ex) {
            LOG.error(UNABLE_TO_ACCESS_GITHUB, ex);
            throw new FetchException(UNABLE_TO_ACCESS_GITHUB, ex);
        }
    }

    @Override
    public List<Defect> getChangedDefects(long projectId, Date since) throws FetchException {
        String repositoryOwner = this.settingService.get(projectId, Settings.DEFECT_GITHUB_OWNER);
        String repositoryName = this.settingService.get(projectId, Settings.DEFECT_GITHUB_REPONAME);
        String authorizationToken = this.settingService.get(projectId, Settings.DEFECT_GITHUB_TOKEN);
        try {
            return this.restClient
                    .forOwnerAndRepository(repositoryOwner, repositoryName)
                    .withToken(authorizationToken)
                    .getIssuesUpdatedSince(since)
                    .stream()
                    .map(this::toDefect)
                    .toList();
        } catch (IOException | URISyntaxException ex) {
            LOG.error(UNABLE_TO_ACCESS_GITHUB, ex);
            throw new FetchException(UNABLE_TO_ACCESS_GITHUB, ex);
        }
    }

    @Override
    public boolean isValidId(String id) {
        try {
            Integer value = Integer.valueOf(id);
            return value > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    public String getIdFormatHint(long projectId) {
        return "The ID must be a positive integer.";
    }

    @Override
    public String getCode() {
        return "github";
    }

    @Override
    public String getName() {
        return "GitHub";
    }

    @Override
    public List<SettingDTO> getSettingDefinitions() {
        return this.settingProviderService.getDefectGithubDefinitions();
    }

    private Defect toDefect(GithubIssue issue) {
        String id = String.valueOf(issue.getNumber());
        ProblemStatus status = ProblemStatus.CLOSED;
        if ("open".equals(issue.getState())) {
            status = ProblemStatus.OPEN;
        }
        return new Defect(id, status, issue.getClosedAt());
    }
}
