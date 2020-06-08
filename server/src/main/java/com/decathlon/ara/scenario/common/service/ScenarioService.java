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

package com.decathlon.ara.scenario.common.service;

import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.service.SeverityService;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreCountDTO;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreFeatureDTO;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreSeverityDTO;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreSourceDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioSummaryDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.mapper.ScenarioSummaryMapper;
import com.decathlon.ara.service.mapper.SourceMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.*;

/**
 * Service for managing Scenario.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScenarioService {

    @NonNull
    private final ScenarioRepository scenarioRepository;

    @NonNull
    private final SourceMapper sourceMapper;

    @NonNull
    private final ScenarioSummaryMapper scenarioSummaryMapper;

    @NonNull
    private final SeverityService severityService;

    /**
     * @param projectId the ID of the project in which to work
     * @return all scenarios that have no associated functionalities or have wrong or nonexistent functionality identifier
     */
    public List<ScenarioSummaryDTO> findAllWithFunctionalityErrors(long projectId) {
        return scenarioSummaryMapper.toDto(scenarioRepository.findAllWithFunctionalityErrors(projectId));
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return for each source (API, Web...) and severity couples, a count of ignored&amp;total scenarios and a list of ignored scenarios by feature file
     */
    public List<ScenarioIgnoreSourceDTO> getIgnoredScenarioCounts(long projectId) {
        // All database severities + special Severity.ALL
        List<SeverityDTO> severitiesWithAll = severityService.getSeveritiesWithAll(projectId);
        String defaultSeverityCode = severityService.getDefaultSeverityCode(severitiesWithAll);

        List<ScenarioIgnoreSourceDTO> resultList = new ArrayList<>();
        fillCountsInto(projectId, resultList, severitiesWithAll, defaultSeverityCode);
        fillScenariosInto(projectId, resultList, severitiesWithAll, defaultSeverityCode);
        addGlobalSource(resultList);
        sort(resultList);
        return resultList;
    }

    private void addGlobalSource(List<ScenarioIgnoreSourceDTO> resultList) {
        ScenarioIgnoreSourceDTO global = new ScenarioIgnoreSourceDTO();
        global.setSource(SourceDTO.ALL);

        // Add all counts
        for (ScenarioIgnoreSourceDTO sourceDTO : resultList) {
            for (ScenarioIgnoreSeverityDTO severityDTO : sourceDTO.getSeverities()) {
                final ScenarioIgnoreCountDTO globalCounts = getOrCreate(global, severityDTO.getSeverity()).getCounts();
                globalCounts.setIgnored(globalCounts.getIgnored() + severityDTO.getCounts().getIgnored());
                globalCounts.setTotal(globalCounts.getTotal() + severityDTO.getCounts().getTotal());
            }
        }

        // Global special source has no feature list (null will not export it in JSON)
        for (ScenarioIgnoreSeverityDTO severityDTO : global.getSeverities()) {
            severityDTO.setFeatures(null);
        }

        resultList.add(global);
    }

    /**
     * Sort a source[].severity[] tree by source code first, and then for each source, all of their severities by severity position.
     *
     * @param resultList the tree to sort
     */
    private void sort(List<ScenarioIgnoreSourceDTO> resultList) {
        // Order all sources
        resultList.sort((result1, result2) -> {
            Comparator<ScenarioIgnoreSourceDTO> comparator = comparing(ScenarioIgnoreSourceDTO::getSource, nullsFirst(naturalOrder()));
            return nullsFirst(comparator).compare(result1, result2);
        });

        // For each source, sort all severities
        for (ScenarioIgnoreSourceDTO result : resultList) {
            result.getSeverities().sort((severity1, severity2) -> {
                Comparator<ScenarioIgnoreSeverityDTO> comparator = comparing(s -> s.getSeverity().getPosition(), nullsFirst(naturalOrder()));
                return nullsFirst(comparator).compare(severity1, severity2);
            });
        }
    }

    /**
     * Compute a source[].severity[] tree, counting total and ignored scenarios per severity.
     *
     * @param projectId           the ID of the project in which to work
     * @param resultList          the list in which to populate counts: for each source, a list of severities (with ignored and
     *                            total scenario counts)
     * @param severitiesWithAll   all actual severities + the ALL special-severity
     * @param defaultSeverityCode the code of the severity to use for scenarios with no assigned severity
     */
    private void fillCountsInto(long projectId, List<ScenarioIgnoreSourceDTO> resultList, List<SeverityDTO> severitiesWithAll, String defaultSeverityCode) {
        // For each [ source, severityCode, ignoredOrNot ] triple, we have the count of scenarios that match these criteria
        for (ScenarioIgnoreCount ignoreCount : scenarioRepository.findIgnoreCounts(projectId)) {
            // Scenarios without severity will be counted as having the default severity
            final String effectiveSeverityCode = (StringUtils.isEmpty(ignoreCount.getSeverityCode()) ? defaultSeverityCode : ignoreCount.getSeverityCode());
            // Add the counts of scenarios to the triple' severity, as well as the the ALL-SEVERITIES count
            final List<SeverityDTO> severitiesToIncrement = severitiesWithAll.stream()
                    .filter(severity -> Severity.ALL.getCode().equals(severity.getCode()) || severity.getCode().equals(effectiveSeverityCode))
                    .collect(Collectors.toList());
            for (SeverityDTO severity : severitiesToIncrement) {
                ScenarioIgnoreSeverityDTO result = getOrCreate(resultList, ignoreCount.getSource(), severity);
                // Increment the count of TOTAL scenarios and the count of IGNORED scenarios (if the count is counting ignored scenarios)
                result.getCounts().setTotal(result.getCounts().getTotal() + ignoreCount.getCount());
                if (ignoreCount.isIgnored()) {
                    result.getCounts().setIgnored(result.getCounts().getIgnored() + ignoreCount.getCount());
                }
            }
        }
    }

    /**
     * Compute a source[].severity[].feature[].scenario[] tree, appending all ignored scenario names in this tree.
     *
     * @param projectId           the ID of the project in which to work
     * @param resultList          the list in which to populate scenario names: for each source, a list of severities,
     *                            and for each severity, a list of features (with name and file name)... and for each feature, a
     *                            list of scenario names (only ignored scenarios are appended)
     * @param severities          all actual severities (with or without the ALL special-severity: this one will not be used)
     * @param defaultSeverityCode the code of the severity to use for scenarios with no assigned severity
     */
    private void fillScenariosInto(long projectId, List<ScenarioIgnoreSourceDTO> resultList, List<SeverityDTO> severities, String defaultSeverityCode) {
        for (IgnoredScenario ignoredScenario : scenarioRepository.findIgnoredScenarios(projectId)) {
            // Scenarios without severity will be counted as having the default severity
            final String effectiveSeverityCode = (StringUtils.isEmpty(ignoredScenario.getSeverity()) ? defaultSeverityCode : ignoredScenario.getSeverity());
            // Append the ignored scenario to the feature&scenario list of its source&severity
            severities.stream()
                    .filter(severity -> severity.getCode().equals(effectiveSeverityCode))
                    .findFirst()
                    .ifPresent(severity -> {
                        ScenarioIgnoreSeverityDTO result = getOrCreate(resultList, ignoredScenario.getSource(), severity);
                        getOrCreateFeature(result.getFeatures(), ignoredScenario).getScenarios().add(ignoredScenario.getName());
                    });
        }
    }

    /**
     * @param features        the list of features in which to find or create the featureFile of the ignoredScenario;
     *                        if created, it is appended to the list
     * @param ignoredScenario search its featureFile in the list, or create it with its featureFile and featureName
     * @return the found or created DTO from/into the list
     */
    private ScenarioIgnoreFeatureDTO getOrCreateFeature(List<ScenarioIgnoreFeatureDTO> features, IgnoredScenario ignoredScenario) {
        return features.stream()
                .filter(f -> f.getFile().equals(ignoredScenario.getFeatureFile()))
                .findFirst()
                .orElseGet(() -> {
                    ScenarioIgnoreFeatureDTO feature = new ScenarioIgnoreFeatureDTO();
                    feature.setFile(ignoredScenario.getFeatureFile());
                    feature.setName(ignoredScenario.getFeatureName());
                    features.add(feature);
                    return feature;
                });
    }

    /**
     * @param list     the list of DTOs in which to find or create the requested one (matching both source and severity);
     *                 if created, it is appended to the list
     * @param source   the requested source of the DTO to find or create
     * @param severity the requested severity of the DTO to find or create
     * @return the found or created DTO from/into the list
     */
    private ScenarioIgnoreSeverityDTO getOrCreate(List<ScenarioIgnoreSourceDTO> list, Source source, SeverityDTO severity) {
        final ScenarioIgnoreSourceDTO ignoredSourceDTO = list.stream()
                .filter(r -> r.getSource().getCode().equals(source.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    ScenarioIgnoreSourceDTO result = new ScenarioIgnoreSourceDTO();
                    result.setSource(sourceMapper.toDto(source));
                    list.add(result);
                    return result;
                });
        return getOrCreate(ignoredSourceDTO, severity);
    }

    private ScenarioIgnoreSeverityDTO getOrCreate(ScenarioIgnoreSourceDTO ignoredSourceDTO, SeverityDTO severity) {
        return ignoredSourceDTO.getSeverities().stream()
                .filter(r -> r.getSeverity().getCode().equals(severity.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    ScenarioIgnoreSeverityDTO result = new ScenarioIgnoreSeverityDTO();
                    result.setSeverity(severity);
                    result.setFeatures(severity.getCode().equals(Severity.ALL.getCode()) ? null : new ArrayList<>());
                    ignoredSourceDTO.getSeverities().add(result);
                    return result;
                });
    }

}
