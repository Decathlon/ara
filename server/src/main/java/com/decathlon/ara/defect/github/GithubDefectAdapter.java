package com.decathlon.ara.defect.github;

import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.support.Settings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Implementation of {@link DefectAdapter} for Github issues system.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GithubDefectAdapter implements DefectAdapter {
    private static final String UNABLE_TO_ACCESS_GITHUB = "Unable to access Github.";
    @Autowired
    private GithubRestClient restClient;

    @Autowired
    private SettingService settingService;

    @Autowired
    private SettingProviderService settingProviderService;


    @Override
    public List<Defect> getStatuses(long projectId, List<String> ids) throws FetchException {
        String repositoryOwner = this.settingService.get(projectId, Settings.DEFECT_GITHUB_OWNER);
        String repositoryName = this.settingService.get(projectId, Settings.DEFECT_GITHUB_REPONAME);
        String authorizationToken = this.settingService.get(projectId, Settings.DEFECT_GITHUB_TOKEN);
        List<Long> issueIds = ids.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        try {
            return this.restClient
                    .forOwnerAndRepository(repositoryOwner, repositoryName)
                    .withToken(authorizationToken)
                    .requestIssues(issueIds)
                    .stream()
                    .map(this::toDefect)
                    .collect(Collectors.toList());
        } catch (IOException | URISyntaxException ex) {
            log.error(UNABLE_TO_ACCESS_GITHUB, ex);
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
                    .collect(Collectors.toList());
        } catch (IOException | URISyntaxException ex) {
            log.error(UNABLE_TO_ACCESS_GITHUB, ex);
            throw new FetchException(UNABLE_TO_ACCESS_GITHUB, ex);
        }
    }

    @Override
    public boolean isValidId(long projectId, String id) {
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
