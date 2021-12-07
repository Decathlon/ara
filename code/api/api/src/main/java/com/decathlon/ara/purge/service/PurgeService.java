package com.decathlon.ara.purge.service;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import com.decathlon.ara.service.util.DateService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PurgeService {

    @NonNull
    private final ExecutionRepository executionRepository;

    @NonNull
    private final ProjectService projectService;

    @NonNull
    private final SettingService settingService;

    @NonNull
    private final DateService dateService;

    /**
     * Purge executions by project code
     * @param projectCode the project code
     */
    public void purgeExecutionsByProjectCode(String projectCode) {
        long projectId;
        try {
            projectId = projectService.toId(projectCode);
        } catch (NotFoundException e) {
            log.warn("No purge because, the project code '{}' is unknown", projectCode);
            return;
        }
        log.info("Preparing purge for project '{}'", projectCode);
        purgeExecutionsByProjectId(projectId);
    }

    /**
     * Purge executions by project id
     * @param projectId the project id
     */
    public void purgeExecutionsByProjectId(long projectId) {
        var durationValue = settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_VALUE);

        var durationValueAsInt = 0;
        try {
            durationValueAsInt = Integer.parseInt(durationValue);
        } catch (NumberFormatException exception) {
            log.warn("No correct period unit found ({}). Purge aborted", durationValue);
            return;
        }

        if (durationValueAsInt < 0) {
            log.info("No purge because the duration was negative ({})", durationValueAsInt);
            return;
        }
        var valuePlural = durationValueAsInt > 1 ? "s" : "";
        log.info("Retrieved duration value: {} unit{}", durationValue, valuePlural);

        var durationType = settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_TYPE);
        if (StringUtils.isBlank(durationType)) {
            log.warn("No period found. Purge aborted");
            return;
        }
        log.info("Retrieved duration period: {}{}", durationType.toLowerCase(), valuePlural);

        var periodDescription = (durationValueAsInt > 1 ? durationValueAsInt + " " : "") + durationType.toLowerCase() + valuePlural;
        log.info("Preparing to delete all execution older than the last {}", periodDescription);
        var purgeThresholdDate = dateService.getTodayDateMinusPeriod(durationValueAsInt, durationType);
        if (purgeThresholdDate.isEmpty()) {
            log.warn("Purge aborted, because the period was incorrect");
            return;
        }

        var executionsToDelete = executionRepository.findByCycleDefinitionProjectIdAndTestDateTimeBefore(projectId, purgeThresholdDate.get());

        var numberOfDeletedExecutions = executionsToDelete.size();
        var executionsPlural = numberOfDeletedExecutions > 1 ? "s" : "";

        var simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        log.info("Preparing to delete {} execution{} (older than {})...", numberOfDeletedExecutions, executionsPlural, simpleDateFormat.format(purgeThresholdDate.get()));
        var executionIdsToDelete = executionsToDelete.stream().map(Execution::getId).toList();
        var purgeRunStartDate = LocalDateTime.now();
        executionRepository.deleteAllByIdInBatch(executionIdsToDelete);
        var purgeRunEndDate = LocalDateTime.now();
        var purgeDurationDescription = dateService.getFormattedDurationBetween2Dates(purgeRunStartDate, purgeRunEndDate);
        DateTimeFormatter detailedDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
        log.info("Purge ran from {} to {} - ({})", purgeRunStartDate.format(detailedDateFormat), purgeRunEndDate.format(detailedDateFormat), purgeDurationDescription);
        log.info("{} execution{} successfully deleted", numberOfDeletedExecutions, executionsPlural);
    }

    /**
     * Purge all projects executions
     */
    public void purgeAllProjects() {
        var projects = projectService.findAll();
        var projectsSize = projects.size();
        IntStream
                .range(0, projectsSize)
                .mapToObj(index -> Pair.of(index, projects.get(index)))
                .forEach(pair -> {
                    var currentProjectPosition = pair.getFirst() + 1;
                    var project = pair.getSecond();
                    log.info("Preparing purge for project '{}' ({}/{})", project.getCode(), currentProjectPosition, projectsSize);
                    purgeExecutionsByProjectId(project.getId());
                });
    }
}
