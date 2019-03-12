package com.decathlon.ara.ci.service;

import com.decathlon.ara.configuration.AraConfiguration;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.EmailService;
import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.decathlon.ara.service.dto.run.ExecutedScenarioHandlingCountsDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class QualityEmailService {

    @NonNull
    private final AraConfiguration araConfiguration;

    @NonNull
    private final ExecutionHistoryService executionHistoryService;

    @NonNull
    private final TeamRepository teamRepository;

    @NonNull
    private final EmailService emailService;

    @NonNull
    private final ProjectRepository projectRepository;

    @NonNull
    private final SettingService settingService;

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public void sendQualityEmail(long projectId, long executionId) throws NotFoundException {
        ExecutionHistoryPointDTO execution = executionHistoryService.getExecution(projectId, executionId);

        final String from = settingService.get(projectId, Settings.EMAIL_FROM);
        Optional<String> to = getRecipient(projectId, execution);
        if (from != null && to.isPresent()) {
            final List<Team> teamsAssignableToProblems = teamRepository.findAllByProjectIdOrderByName(projectId).stream()
                    .filter(Team::isAssignableToProblems)
                    .collect(Collectors.toList());

            Map<String, Object> variables = new HashMap<>();
            variables.put("execution", execution);
            variables.put("teamsAssignableToProblems", teamsAssignableToProblems);
            variables.put("qualitiesPerTeamAndSeverity", aggregateQualitiesPerTeamAndSeverity(execution));
            variables.put("NO_TEAM", new Team().withId(Long.valueOf(-404)).withName("(No team)"));

            variables.put("buildDate", formatDate(execution.getBuildDateTime()));
            variables.put("testDate", formatDate(execution.getTestDateTime()));

            variables.put("eligibilityMessage", getEligibilityMessage(execution));

            final String clientBaseUrl = araConfiguration.getClientBaseUrl();
            final Long projectIdLong = Long.valueOf(projectId);
            final Project project = projectRepository.findById(projectIdLong)
                    .orElse(new Project().withCode(projectIdLong.toString()).withName("Project " + projectId));
            final String url = clientBaseUrl + "#/projects/" + project.getCode() + "/executions/" + execution.getId();
            variables.put("executionUrl", url);
            variables.put("projectName", project.getName());

            Map<String, Resource> inlineResources = new HashMap<>();
            addInlineResource(inlineResources, "favicon.png", "templates/mail/html/favicon.png");

            emailService.sendHtmlMessage(
                    from,
                    to.get(),
                    getSubject(project.getName(), execution),
                    "execution-quality-status",
                    variables,
                    inlineResources);
        }
    }

    void addInlineResource(Map<String, Resource> inlineResources, String name, String path) {
        try (final InputStream stream = EmailService.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                log.error("Cannot find the inline resource in classpath to include it in the email: {}", path);
            } else {
                inlineResources.put(name, new ByteArrayResource(IOUtils.toByteArray(stream)));
            }
        } catch (IOException e) {
            log.error("Cannot include the resource in the email: {}", path, e);
        }
    }

    String formatDate(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        return new SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.US).format(date);
    }

    Optional<String> getRecipient(long projectId, ExecutionDTO execution) {
        if (crashed(execution)) {
            return configuredEmail(projectId, Settings.EMAIL_TO_EXECUTION_CRASHED);
        } else if (!execution.isBlockingValidation()) {
            return configuredEmail(projectId, Settings.EMAIL_TO_EXECUTION_RAN);
        } else if (execution.getQualityStatus() == QualityStatus.WARNING) {
            return configuredEmail(projectId, Settings.EMAIL_TO_EXECUTION_ELIGIBLE_WARNING);
        } else if (execution.getQualityStatus() == QualityStatus.PASSED) {
            return configuredEmail(projectId, Settings.EMAIL_TO_EXECUTION_ELIGIBLE_PASSED);
        } else {
            return configuredEmail(projectId, Settings.EMAIL_TO_EXECUTION_NOT_ELIGIBLE);
        }
    }

    private Optional<String> configuredEmail(long projectId, String settingCode) {
        String value = settingService.get(projectId, settingCode);
        if (StringUtils.isEmpty(value)) {
            log.info("Not sending email report because the project setting {} has not been provided", settingCode);
            return Optional.empty();
        }
        return Optional.of(value);
    }

    String getSubject(String projectName, ExecutionDTO execution) {
        boolean crashed = crashed(execution);

        String eligibility;
        if (crashed) {
            eligibility = "\uD83D\uDCA5 CRASHED,"; // COLLISION http://www.fileformat.info/info/unicode/char/1f4a5/index.htm
        } else if (!execution.isBlockingValidation()) {
            eligibility = "RAN";
        } else if (execution.getQualityStatus() == QualityStatus.WARNING) {
            eligibility = "\u26A0\uFE0F ELIGIBLE"; // WARNING https://www.fileformat.info/info/unicode/char/26a0/index.htm
            // + VARIATION SELECTOR to have the emoji version https://www.fileformat.info/info/unicode/char/fe0f/index.htm
        } else if (execution.getQualityStatus() == QualityStatus.PASSED) {
            eligibility = "\u2705 ELIGIBLE"; // WHITE HEAVY CHECK MARK http://www.fileformat.info/info/unicode/char/2705/index.htm
        } else {
            eligibility = "\u274C NOT ELIGIBLE"; // CROSS MARK http://www.fileformat.info/info/unicode/char/274c/index.htm
        }

        StringBuilder subject = new StringBuilder();
        subject.append(projectName).append(' ')
                .append(execution.getBranch()).append('/').append(execution.getName())
                .append(" NRT FOR ").append(execution.getRelease()).append(": ")
                .append(eligibility);

        if (!crashed) {
            subject.append(" (");
            if (execution.getQualityStatus() == QualityStatus.INCOMPLETE) {
                subject.append("INCOMPLETE");
            } else {
                appendSeverityPercentages(execution, subject);
            }
            subject.append(")");
        }

        subject.append(" TESTED ON ").append(formatDate(execution.getTestDateTime()));

        return subject.toString().toUpperCase();
    }

    private boolean crashed(ExecutionDTO execution) {
        return execution.getQualitySeverities() == null || execution.getQualitySeverities().isEmpty() || execution.getQualityThresholds() == null || execution.getQualityThresholds().isEmpty();
    }

    private void appendSeverityPercentages(ExecutionDTO execution, StringBuilder builder) {
        for (int i = 0; i < execution.getQualitySeverities().size(); i++) {
            QualitySeverityDTO qualitySeverity = execution.getQualitySeverities().get(i);
            boolean isLast = (i == execution.getQualitySeverities().size() - 1);

            final String name = qualitySeverity.getSeverity().getInitials();
            final int percent = qualitySeverity.getPercent();
            builder.append(name).append(' ').append(percent).append(" %").append(isLast ? StringUtils.EMPTY : " | ");
        }
    }

    String getEligibilityMessage(ExecutionDTO execution) {
        if (!execution.isBlockingValidation()) {
            return "Quality for this build:";
        } else if (execution.getQualityStatus().isAcceptable()) {
            return "The build is <b style='background-color: #19be6b; color: white; padding: 1px 2px; border-radius: 2px;'>eligible to deploy</b>" +
                    (execution.getQualityStatus() == QualityStatus.WARNING ?
                            " but <b style='background-color: #ffcc30; padding: 1px 2px; border-radius: 2px;'>without much margin regarding the thresholds</b>" :
                            "") +
                    ":";
        } else {
            return "The build is <b style='background-color: #ed3f14; color: white; padding: 1px 2px; border-radius: 2px;'>not eligible to deploy</b>:";
        }
    }

    Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> aggregateQualitiesPerTeamAndSeverity(ExecutionHistoryPointDTO execution) {
        Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> globalRunQualities = new HashMap<>();
        execution.getRuns().forEach(run ->
                run.getQualitiesPerTeamAndSeverity().forEach((teamId, runQualities) ->
                        runQualities.forEach((severityCode, counts) -> {
                            final Map<String, ExecutedScenarioHandlingCountsDTO> foundCounts = globalRunQualities.computeIfAbsent(teamId, k -> new HashMap<>());
                            final ExecutedScenarioHandlingCountsDTO foundCount = foundCounts.computeIfAbsent(severityCode, k -> new ExecutedScenarioHandlingCountsDTO());
                            foundCount.add(counts);
                        })
                )
        );
        return globalRunQualities;
    }

}
