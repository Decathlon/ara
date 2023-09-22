package com.decathlon.ara.purge.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import com.decathlon.ara.service.util.DateService;

@Service
@Transactional
public class PurgeService {

    private static final Logger LOG = LoggerFactory.getLogger(PurgeService.class);

    private final ExecutionRepository executionRepository;

    private final ProjectService projectService;

    private final SettingService settingService;

    private final DateService dateService;

    public PurgeService(ExecutionRepository executionRepository, ProjectService projectService,
            SettingService settingService, DateService dateService) {
        this.executionRepository = executionRepository;
        this.projectService = projectService;
        this.settingService = settingService;
        this.dateService = dateService;
    }

    /**
     * Purge executions by project code
     * @param projectCode the project code
     */
    public void purgeExecutionsByProjectCode(String projectCode) {
        long projectId;
        try {
            projectId = projectService.toId(projectCode);
        } catch (NotFoundException e) {
            LOG.warn("No purge because, the project code '{}' is unknown", projectCode);
            return;
        }
        LOG.info("Preparing purge for project '{}'", projectCode);
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
            LOG.warn("No correct period unit found ({}). Purge aborted", durationValue);
            return;
        }

        if (durationValueAsInt < 0) {
            LOG.info("No purge because the duration was negative ({})", durationValueAsInt);
            return;
        }
        var valuePlural = durationValueAsInt > 1 ? "s" : "";
        LOG.info("Retrieved duration value: {} unit{}", durationValue, valuePlural);

        var durationType = settingService.get(projectId, Settings.EXECUTION_PURGE_DURATION_TYPE);
        if (StringUtils.isBlank(durationType)) {
            LOG.warn("No period found. Purge aborted");
            return;
        }
        LOG.info("Retrieved duration period: {}{}", durationType.toLowerCase(), valuePlural);

        var periodDescription = (durationValueAsInt > 1 ? durationValueAsInt + " " : "") + durationType.toLowerCase() + valuePlural;
        LOG.info("Preparing to delete all execution older than the last {}", periodDescription);
        var purgeThresholdDate = dateService.getTodayDateMinusPeriod(durationValueAsInt, durationType);
        if (purgeThresholdDate.isEmpty()) {
            LOG.warn("Purge aborted, because the period was incorrect");
            return;
        }

        var executionsToDelete = executionRepository.findByCycleDefinitionProjectIdAndTestDateTimeBefore(projectId, purgeThresholdDate.get());

        var numberOfDeletedExecutions = executionsToDelete.size();
        var executionsPlural = numberOfDeletedExecutions > 1 ? "s" : "";

        var simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        LOG.info("Preparing to delete {} execution{} (older than {})...", numberOfDeletedExecutions, executionsPlural, simpleDateFormat.format(purgeThresholdDate.get()));
        var executionIdsToDelete = executionsToDelete.stream().map(Execution::getId).toList();
        var purgeRunStartDate = LocalDateTime.now();
        executionRepository.deleteAllByIdInBatch(executionIdsToDelete);
        var purgeRunEndDate = LocalDateTime.now();
        var purgeDurationDescription = dateService.getFormattedDurationBetween2Dates(purgeRunStartDate, purgeRunEndDate);
        DateTimeFormatter detailedDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
        LOG.info("Purge ran from {} to {} - ({})", purgeRunStartDate.format(detailedDateFormat), purgeRunEndDate.format(detailedDateFormat), purgeDurationDescription);
        LOG.info("{} execution{} successfully deleted", numberOfDeletedExecutions, executionsPlural);
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
                    LOG.info("Preparing purge for project '{}' ({}/{})", project.getCode(), currentProjectPosition, projectsSize);
                    purgeExecutionsByProjectId(project.getId());
                });
    }
}
