package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.decathlon.ara.service.dto.quality.ScenarioCountDTO;
import com.decathlon.ara.service.mapper.SeverityMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class QualityService {

    static final TypeReference<Map<String, QualityThreshold>> TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD = new TypeReference<Map<String, QualityThreshold>>() {
    };

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final SeverityRepository severityRepository;

    @NonNull
    private final SeverityMapper severityMapper;

    /**
     * Compute and set {@link Execution#qualityStatus} (the global quality status from which to make the execution eligible or not) and {@link Execution#qualitySeverities} (the status and details of each individual + the Global severities).
     *
     * @param execution the execution to study in order to compute its quality(ies) and in which to store the result
     */
    void computeQuality(Execution execution) {
        // This method will downgrade the quality at any time requiring it: the quality cannot upgrade at any place here
        QualityStatus globalQualityStatus = QualityStatus.PASSED;

        Map<String, QualityThreshold> qualityThresholds;
        try {
            qualityThresholds = objectMapper.readValue(execution.getQualityThresholds(), TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD);
        } catch (IOException e) {
            log.error("Cannot parse qualityThresholds, doing without them but marking the execution as incomplete: {}", execution.getQualityThresholds(), e);
            qualityThresholds = null;
            globalQualityStatus = QualityStatus.INCOMPLETE;
        }

        List<QualitySeverityDTO> qualitySeverities = new ArrayList<>();

        // Compute quality for each active severity
        for (Severity severity : getActiveSeverities(execution)) {
            final QualityThreshold threshold = (qualityThresholds == null ? null : qualityThresholds.get(severity.getCode()));

            QualitySeverityDTO qualitySeverity = computeQualityOfSeverity(execution, severity, threshold);
            qualitySeverities.add(qualitySeverity);

            if (threshold == null) {
                log.error("No qualityThresholds for {}: doing without them but marking the severity and execution as incomplete", severity.getCode());
                qualitySeverity.setStatus(QualityStatus.INCOMPLETE);
                globalQualityStatus = QualityStatus.INCOMPLETE;
            }

            if (globalQualityStatus.ordinal() > qualitySeverity.getStatus().ordinal()) {
                globalQualityStatus = qualitySeverity.getStatus();
            }
        }

        // The quality of all scenarios included
        QualitySeverityDTO globalQualitySeverity = computeQualityOfSeverity(execution, Severity.ALL, null);
        globalQualitySeverity.setStatus(globalQualityStatus);
        qualitySeverities.add(globalQualitySeverity);

        // If one run to includeInThresholds went OK but did not execute any scenario, something went wrong: the execution is incomplete
        if (!isComplete(execution)) {
            globalQualitySeverity.setStatus(QualityStatus.INCOMPLETE);
            globalQualityStatus = QualityStatus.INCOMPLETE;
        }

        try {
            execution.setQualitySeverities(objectMapper.writeValueAsString(qualitySeverities));
        } catch (JsonProcessingException e) {
            throw new NotGonnaHappenException("JSON serializing should not have failed when serializing to a String", e);
        }

        // globalQualitySeverity will be serialized as JSON for screen needs: only keep the global quality to be able to filter the Execution table
        execution.setQualityStatus(globalQualityStatus);
    }

    /**
     * @param execution with a list of runs, some may have includeInQuality, others not
     * @return the sorted set of active severities (in Run#severityTags, could be equal to "all" or empty to mean "all") for all active runs of this execution, depending on
     */
    SortedSet<Severity> getActiveSeverities(Execution execution) {
        final List<Severity> allSeverities = severityRepository.findAllByProjectIdOrderByPosition(execution.getCycleDefinition().getProjectId());

        SortedSet<Severity> activeSeverities = new TreeSet<>(new Severity.SeverityPositionComparator());
        final Set<Run> runsToIncludeInQuality = getRunsToIncludeInQuality(execution);
        if (runsToIncludeInQuality.isEmpty()) {
            activeSeverities.addAll(allSeverities);
        }
        for (Run run : runsToIncludeInQuality) {
            if (StringUtils.isEmpty(run.getSeverityTags()) || "all".equals(run.getSeverityTags())) {
                activeSeverities.addAll(allSeverities);
            } else {
                for (final String severityCode : run.getSeverityTags().split(Run.SEVERITY_TAGS_SEPARATOR)) {
                    activeSeverities.add(allSeverities.stream()
                            .filter(s -> severityCode.equals(s.getCode()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Cannot find severity \"" + severityCode + "\" in " + allSeverities)));
                }
            }
        }

        return activeSeverities;
    }

    /**
     * @param execution with a list of runs, some may have includeInQuality, others not
     * @param severity  the severity for which to count scenarios, compute a percentage and a quality status
     * @param threshold the thresholds to use to compute the percentage and the quality status
     * @return a fully initialized QualitySeverityDTO (but with null status if threshold is null)
     */
    QualitySeverityDTO computeQualityOfSeverity(Execution execution, Severity severity, QualityThreshold threshold) {
        ScenarioCountDTO scenarioCounts = countScenariosOfSeverity(getRunsToIncludeInQuality(execution), severity);
        int percent = getQualityPercentage(scenarioCounts);
        QualityStatus severityQualityStatus = (threshold == null ? null : threshold.toStatus(percent));

        QualitySeverityDTO qualitySeverity = new QualitySeverityDTO();
        qualitySeverity.setSeverity(severityMapper.toDto(severity));
        qualitySeverity.setScenarioCounts(scenarioCounts);
        qualitySeverity.setPercent(percent);
        qualitySeverity.setStatus(severityQualityStatus);
        return qualitySeverity;
    }

    /**
     * @param execution with a list of runs, some may have includeInQuality, others not
     * @return all runs with the includeInQuality flag
     */
    Set<Run> getRunsToIncludeInQuality(Execution execution) {
        return execution.getRuns().stream()
                .filter(r -> r.getIncludeInThresholds() == Boolean.TRUE)
                .collect(Collectors.toSet());
    }

    /**
     * @param execution with a list of runs, some may have includeInQuality, others not
     * @return true if all runs with the includeInQuality flag have status DONE and have at least one executed scenario
     */
    boolean isComplete(Execution execution) {
        for (Run run : getRunsToIncludeInQuality(execution)) {
            if (run.getStatus() != JobStatus.DONE || run.getExecutedScenarios().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param runs     list of runs to include in thresholds
     * @param severity the severity for which the method will count scenarios (count is correct if the severity is ALL or the default severity)
     * @return the counted passed, failed & total scenarios
     */
    ScenarioCountDTO countScenariosOfSeverity(Set<Run> runs, Severity severity) {
        ScenarioCountDTO counts = new ScenarioCountDTO();

        for (Run run : runs) {
            for (ExecutedScenario executedScenario : run.getExecutedScenarios()) {
                if (Severity.ALL.equals(severity) ||
                        (StringUtils.isEmpty(executedScenario.getSeverity()) && severity.isDefaultOnMissing()) ||
                        severity.getCode().equals(executedScenario.getSeverity())) {
                    countScenario(counts, executedScenario);
                }
            }
        }

        return counts;
    }

    /**
     * Increment the total by 1, and passed or failed by 1, depending on the presence of scenario errors.
     *
     * @param counts           the counts to increment
     * @param executedScenario the scenario to increment as PASSED or FAILED
     */
    void countScenario(ScenarioCountDTO counts, ExecutedScenario executedScenario) {
        counts.setTotal(counts.getTotal() + 1);
        if (executedScenario.getErrors().isEmpty()) {
            counts.setPassed(counts.getPassed() + 1);
        } else {
            counts.setFailed(counts.getFailed() + 1);
        }
    }

    /**
     * Compute the percentage of passed scenarios over all scenarios.<br>
     * Truncate, and not round. Eg. 1 failed test out of 200 will truncate to 99% and not round to 100%.<br>
     * Will return 100% if total is zero or negative, because one severity can have no scenario at all, without making the execution failed
     * (however, if all severities of a run have no scenario, the execution would be marked as INCOMPLETE, but this is handled in a higher-level code).
     *
     * @param counts scenario counts (passed and total)
     * @return the quality percentage
     */
    int getQualityPercentage(ScenarioCountDTO counts) {
        if (counts.getTotal() <= 0) {
            return 100;
        }
        return 100 * counts.getPassed() / counts.getTotal();
    }

}
