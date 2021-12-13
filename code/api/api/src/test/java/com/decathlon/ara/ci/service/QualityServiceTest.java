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

package com.decathlon.ara.ci.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.decathlon.ara.service.dto.quality.ScenarioCountDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.mapper.GenericMapper;
import com.decathlon.ara.util.TestUtil;
import com.decathlon.ara.util.builder.RunBuilder;
import com.decathlon.ara.util.factory.CountryFactory;
import com.decathlon.ara.util.factory.CycleDefinitionFactory;
import com.decathlon.ara.util.factory.ExecutionBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class QualityServiceTest {

    private static final long PROJECT_ID = 1;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SeverityRepository severityRepository;

    @Mock
    private GenericMapper mapper;

    @InjectMocks
    private QualityService cut;

    @Captor
    private ArgumentCaptor<List<QualitySeverityDTO>> qualitySeverityListArgument;

    /**
     * For two new runs or scenarios to not equal to each other, and be all added to a Set.
     */
    private int lastId;

    @Test
    void computeQuality_should_set_quality_status_INCOMPLETE_when_qualityThresholds_are_malformed() throws IOException {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withQualityThresholds("some").build();
        when(objectMapper.readValue(anyString(), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD)))
                .thenThrow(new JsonProcessingException("") {
                });
        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("any");

        // WHEN
        cut.computeQuality(execution);

        // THEN
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(qualitySeverityListArgument.getValue().get(0).getStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

    @Test
    void computeQuality_should_set_quality_status_INCOMPLETE_when_one_qualityThreshold_is_missing() throws IOException {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withQualityThresholds("some")
                .withRuns(Collections.singleton(new RunBuilder().withIncludeInThresholds(Boolean.TRUE).build())).build();
        when(objectMapper.readValue(anyString(), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD))).thenReturn(ImmutableMap.of());
        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("any");
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Collections.singletonList(
                severity("1", 0, true)));

        // WHEN
        cut.computeQuality(execution);

        // THEN
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(qualitySeverityListArgument.getValue().get(1).getStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

    @Test
    void computeQuality_should_set_quality_status_INCOMPLETE_when_one_run_has_no_result() throws IOException {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withQualityThresholds("some")
                .withRuns(Collections.singleton(new RunBuilder().withIncludeInThresholds(Boolean.TRUE).build())).build();
        when(objectMapper.readValue(anyString(), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD))).thenReturn(ImmutableMap.of());
        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("any");

        // WHEN
        cut.computeQuality(execution);

        // THEN
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(qualitySeverityListArgument.getValue().get(0).getStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void computeQuality_should_set_the_quality_fields() throws IOException {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withQualityThresholds("the_ones")
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                                        passedScenario(null),
                                        failedScenario(null),
                                        passedScenario("2")))).build()))).build();

        Map<String, QualityThreshold> qualityThresholds = new HashMap<>();
        qualityThresholds.put("1", new QualityThreshold(1, 99));
        qualityThresholds.put("2", new QualityThreshold(42, 0));
        when(objectMapper.readValue(eq("the_ones"), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD))).thenReturn(qualityThresholds);

        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                severity("1", 1, true), // Will be WARNING
                severity("2", 2, false) // Will be PASSED, but should not override the WARNING for global quality
        ));

        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("result");

        SeverityDTO severityAllDto = new SeverityDTO();
        doReturn(severityAllDto).when(mapper).map(Severity.ALL, SeverityDTO.class);

        // WHEN
        cut.computeQuality(execution);

        // THEN
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.WARNING);
        assertThat(execution.getQualitySeverities()).isEqualTo("result");

        List<QualitySeverityDTO> qualitySeverities = qualitySeverityListArgument.getValue();

        assertThat(qualitySeverities).hasSize(3);
        assertThat(qualitySeverities.get(0).getPercent()).isEqualTo(50);
        assertThat(qualitySeverities.get(2).getSeverity()).isSameAs(severityAllDto);
        assertThat(qualitySeverities.get(2).getPercent()).isEqualTo(66);
        assertThat(qualitySeverities.get(2).getStatus()).isEqualTo(QualityStatus.WARNING);
    }

    @Test
    void getActiveSeverities_should_return_all_if_one_run_has_all() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("all").build(),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("1,2").build()))).build();

        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                severity("1", 1, false),
                severity("2", 2, false),
                severity("3", 3, false)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2", "3");
    }

    @Test
    void getActiveSeverities_should_return_all_if_one_run_has_no_severity_tag() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags(null).build(),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("1,2").build()))).build();
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                severity("1", 1, false),
                severity("2", 2, false),
                severity("3", 3, false)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2", "3");
    }

    @Test
    void getActiveSeverities_should_return_all_if_no_run_is_included_in_thresholds() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withSeverityTags(null).build(),
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withSeverityTags("1,2").build()))).build();
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                severity("1", 1, false),
                severity("2", 2, false),
                severity("3", 3, false)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2", "3");
    }

    @Test
    void getActiveSeverities_should_sort_returned_severities_by_position_instead_of_by_code() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("a,b").build()))).build();
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                severity("b", 1, false),
                severity("a", 2, false)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("b", "a");
    }

    @Test
    void getActiveSeverities_should_return_only_active_severities() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("1,2").build()))).build();
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                severity("1", 1, false),
                severity("2", 2, false),
                severity("3", 3, false)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2");
    }

    @Test
    void getActiveSeverities_should_return_severity_ordered_by_position() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("3,2").build()))).build();
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                severity("1", 2, false),
                severity("2", 1, false),
                severity("3", 3, false)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("2", "3");
    }

    @Test
    void getActiveSeverities_should_throw_IllegalArgumentException_on_unknown_severity() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withCycleDefinition(CycleDefinitionFactory.get(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("any").build()))).build();
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Collections.emptyList());

        // WHEN
        Assertions.assertThrows(IllegalArgumentException.class, () -> cut.getActiveSeverities(execution));
    }

    @Test
    void computeQualityOfSeverity_should_map_severity_to_a_dto() {
        // GIVEN
        Execution execution = new Execution();
        Severity someSeverity = Severity.ALL;
        SeverityDTO someSeverityDto = new SeverityDTO();
        doReturn(someSeverityDto).when(mapper).map(someSeverity, SeverityDTO.class);

        // WHEN
        QualitySeverityDTO quality = cut.computeQualityOfSeverity(execution, someSeverity, null);

        // THEN
        assertThat(quality.getSeverity()).isSameAs(someSeverityDto);
    }

    @Test
    void computeQualityOfSeverity_should_compute_well() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Arrays.asList(
                        // Count it
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(
                                        passedScenario("some")))).build(),
                        // Not the same severity
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(
                                        passedScenario("OTHER")))).build(),
                        // Not included in quality computation
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(
                                        passedScenario("some")))).build()))).build();
        Severity someSeverity = severity("some", 0, false);
        QualityThreshold threshold = new QualityThreshold(1, 1);

        // WHEN
        QualitySeverityDTO quality = cut.computeQualityOfSeverity(execution, someSeverity, threshold);

        // THEN
        assertThat(quality.getScenarioCounts().getTotal()).isEqualTo(1);
        assertThat(quality.getPercent()).isEqualTo(100);
        assertThat(quality.getStatus()).isEqualTo(QualityStatus.PASSED);
    }

    @Test
    void getRunsToIncludeInQuality_should_return_all_runs_to_include() {
        // GIVEN
        final Run run1 = run().withIncludeInThresholds(Boolean.TRUE).build();
        final Run run2 = run().withIncludeInThresholds(Boolean.TRUE).build();
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Arrays.asList(
                        run1,
                        run2))).build();

        // WHEN
        Set<Run> runs = cut.getRunsToIncludeInQuality(execution);

        // THEN
        assertThat(runs).containsOnly(run1, run2);
    }

    @Test
    void getRunsToIncludeInQuality_should_return_only_runs_to_include() {
        // GIVEN
        final Run run1 = run().withIncludeInThresholds(Boolean.TRUE).build();
        final Run run2 = run().withIncludeInThresholds(Boolean.FALSE).build();
        final Run run3 = run().withIncludeInThresholds(null).build();
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Arrays.asList(
                        run1,
                        run2,
                        run3))).build();

        // WHEN
        Set<Run> runs = cut.getRunsToIncludeInQuality(execution);

        // THEN
        assertThat(runs).containsOnly(run1);
    }

    @Test
    void isComplete_should_return_true_if_all_runs_are_DONE() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.singleton(new ExecutedScenario())).build(),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.singleton(new ExecutedScenario())).build()))).build();

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isTrue();
    }

    @Test
    void isComplete_should_return_false_if_any_run_is_not_DONE() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Arrays.asList(
                        run().withIncludeInThresholds(Boolean.TRUE).withStatus(JobStatus.DONE).build(),
                        run().withIncludeInThresholds(Boolean.TRUE).withStatus(JobStatus.PENDING).build()))).build();

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isFalse();
    }

    @Test
    void isComplete_should_return_false_if_any_run_is_not_DONE_even_if_it_has_executedScenarios() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.PENDING)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(new ExecutedScenario()))).build()))).build();

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isFalse();
    }

    @Test
    void isComplete_should_return_false_if_any_run_has_no_executed_scenario() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(new ExecutedScenario()))).build(),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.emptySet()).build()))).build();

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isFalse();
    }

    @Test
    void isComplete_should_return_true_if_all_runs_to_include_are_DONE() {
        // GIVEN
        Execution execution = new ExecutionBuilder()
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.singleton(new ExecutedScenario())).build(),
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withStatus(JobStatus.PENDING).build()))).build();

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isTrue();
    }

    @Test
    void countScenariosOfSeverity_should_count_all() {
        // GIVEN
        final Set<Run> runs = Collections.singleton(run()
                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                        passedScenario(null),
                        failedScenario(null)))).build());

        // WHEN
        ScenarioCountDTO counts = cut.countScenariosOfSeverity(runs, Severity.ALL);

        // THEN
        assertThat(counts.getTotal()).isEqualTo(2);
        assertThat(counts.getPassed()).isEqualTo(1);
        assertThat(counts.getFailed()).isEqualTo(1);
    }

    @Test
    void countScenariosOfSeverity_should_count_default() {
        // GIVEN
        final Set<Run> runs = Collections.singleton(run()
                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                        passedScenario(""),
                        failedScenario("some"),
                        failedScenario("other")))).build());
        Severity someSeverity = severity("some", 0, true);

        // WHEN
        ScenarioCountDTO counts = cut.countScenariosOfSeverity(runs, someSeverity);

        // THEN
        assertThat(counts.getTotal()).isEqualTo(2);
        assertThat(counts.getPassed()).isEqualTo(1);
        assertThat(counts.getFailed()).isEqualTo(1);
    }

    @Test
    void countScenariosOfSeverity_should_count_only_the_given_severity() {
        // GIVEN
        final Set<Run> runs = Collections.singleton(run()
                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                        passedScenario(""),
                        failedScenario("some")))).build());
        Severity someSeverity = severity("some", 0, false);

        // WHEN
        ScenarioCountDTO counts = cut.countScenariosOfSeverity(runs, someSeverity);

        // THEN
        assertThat(counts.getTotal()).isEqualTo(1);
        assertThat(counts.getPassed()).isEqualTo(0);
        assertThat(counts.getFailed()).isEqualTo(1);
    }

    @Test
    void countScenario_should_increment_total_count() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO();

        // WHEN
        cut.countScenario(counts, passedScenario(null));

        // THEN
        assertThat(counts.getTotal()).isEqualTo(1);
    }

    @Test
    void countScenario_should_increment_passed_count() {
        // GIVEN
        ScenarioCountDTO counts = scenarioCountDTO(0, 7, 2);
        ExecutedScenario executedScenario = new ExecutedScenario();

        // WHEN
        cut.countScenario(counts, executedScenario);

        // THEN
        assertThat(counts.getFailed()).isEqualTo(7);
        assertThat(counts.getPassed()).isEqualTo(3);
    }

    @Test
    void countScenario_should_increment_failed_count() {
        // GIVEN
        ScenarioCountDTO counts = scenarioCountDTO(0, 7, 2);

        // WHEN
        cut.countScenario(counts, failedScenario(null));

        // THEN
        assertThat(counts.getFailed()).isEqualTo(8);
        assertThat(counts.getPassed()).isEqualTo(2);
    }

    @Test
    void getQualityPercentage_should_return_100_when_no_scenario() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO();

        // WHEN
        final int percentage = cut.getQualityPercentage(counts);

        // THEN
        assertThat(percentage).isEqualTo(100);
    }

    @Test
    void getQualityPercentage_should_return_50_when_half_scenarios_succeed() {
        // GIVEN
        ScenarioCountDTO counts = scenarioCountDTO(2, 0, 1);

        // WHEN
        final int percentage = cut.getQualityPercentage(counts);

        // THEN
        assertThat(percentage).isEqualTo(50);
    }

    @Test
    void getQualityPercentage_should_truncate_instead_of_round_to_not_return_100_when_not_fully_passed() {
        // GIVEN
        ScenarioCountDTO counts = scenarioCountDTO(200, 0, 199);

        // WHEN
        final int percentage = cut.getQualityPercentage(counts);

        // THEN
        assertThat(percentage).isEqualTo(99);
    }

    private RunBuilder run() {
        return new RunBuilder()
                .withCountry(CountryFactory.get(String.valueOf(lastId++)));
    }

    private ExecutedScenario passedScenario(String severity) {
        ExecutedScenario executedScenario = new ExecutedScenario();
        executedScenario.setLine(lastId++);
        executedScenario.setSeverity(severity);
        return executedScenario;
    }

    private ExecutedScenario failedScenario(String severity) {
        ExecutedScenario executedScenario = passedScenario(null);
        executedScenario.getErrors().add(new Error());
        executedScenario.setSeverity(severity);
        return executedScenario;
    }

    private Severity severity(String code, int position, boolean defaultOnMissing) {
        Severity severity = new Severity();
        TestUtil.setField(severity, "code", code);
        TestUtil.setField(severity, "position", position);
        TestUtil.setField(severity, "defaultOnMissing", defaultOnMissing);
        return severity;
    }

    private ScenarioCountDTO scenarioCountDTO(int total, int failed, int passed) {
        ScenarioCountDTO scenarioCountDTO = new ScenarioCountDTO();
        scenarioCountDTO.setTotal(total);
        scenarioCountDTO.setFailed(failed);
        scenarioCountDTO.setPassed(passed);
        return scenarioCountDTO;
    }

}
