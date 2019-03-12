package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.CycleDefinition;
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
import com.decathlon.ara.service.mapper.SeverityMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QualityServiceTest {

    private static final long PROJECT_ID = 1;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SeverityRepository severityRepository;

    @Mock
    private SeverityMapper severityMapper;

    @InjectMocks
    private QualityService cut;

    @Captor
    private ArgumentCaptor<List<QualitySeverityDTO>> qualitySeverityListArgument;

    /**
     * For two new runs or scenarios to not equal to each other, and be all added to a Set.
     */
    private int lastId;

    @Test
    public void computeQuality_should_set_quality_status_INCOMPLETE_when_qualityThresholds_are_malformed() throws IOException {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withQualityThresholds("some");
        when(objectMapper.readValue(anyString(), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD)))
                .thenThrow(new IOException());
        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("any");

        // WHEN
        cut.computeQuality(execution);

        // THEN
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(qualitySeverityListArgument.getValue().get(0).getStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

    @Test
    public void computeQuality_should_set_quality_status_INCOMPLETE_when_one_qualityThreshold_is_missing() throws IOException {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withQualityThresholds("some")
                .withRuns(Collections.singleton(new Run().withIncludeInThresholds(Boolean.TRUE)));
        when(objectMapper.readValue(anyString(), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD))).thenReturn(ImmutableMap.of());
        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("any");
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Collections.singletonList(
                new Severity().withCode("1").withDefaultOnMissing(true)));

        // WHEN
        cut.computeQuality(execution);

        // THEN
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(qualitySeverityListArgument.getValue().get(1).getStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

    @Test
    public void computeQuality_should_set_quality_status_INCOMPLETE_when_one_run_has_no_result() throws IOException {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withQualityThresholds("some")
                .withRuns(Collections.singleton(new Run().withIncludeInThresholds(Boolean.TRUE)));
        when(objectMapper.readValue(anyString(), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD))).thenReturn(ImmutableMap.of());
        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("any");

        // WHEN
        cut.computeQuality(execution);

        // THEN
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(qualitySeverityListArgument.getValue().get(0).getStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

    @Test
    public void computeQuality_should_set_the_quality_fields() throws IOException {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withQualityThresholds("the_ones")
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                                        passedScenario(),
                                        failedScenario(),
                                        passedScenario().withSeverity("2")))))));

        Map<String, QualityThreshold> qualityThresholds = new HashMap<>();
        qualityThresholds.put("1", new QualityThreshold().withFailure(1).withWarning(99));
        qualityThresholds.put("2", new QualityThreshold().withFailure(42));
        when(objectMapper.readValue(eq("the_ones"), eq(QualityService.TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD))).thenReturn(qualityThresholds);

        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                new Severity().withCode("1").withPosition(1).withDefaultOnMissing(true), // Will be WARNING
                new Severity().withCode("2").withPosition(2) // Will be PASSED, but should not override the WARNING for global quality
        ));

        when(objectMapper.writeValueAsString(qualitySeverityListArgument.capture())).thenReturn("result");

        SeverityDTO severityAllDto = new SeverityDTO();
        when(severityMapper.toDto(Severity.ALL)).thenReturn(severityAllDto);

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
    public void getActiveSeverities_should_return_all_if_one_run_has_all() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("all"),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("1,2"))));

        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                new Severity().withCode("1").withPosition(1),
                new Severity().withCode("2").withPosition(2),
                new Severity().withCode("3").withPosition(3)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2", "3");
    }

    @Test
    public void getActiveSeverities_should_return_all_if_one_run_has_no_severity_tag() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags(null),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("1,2"))));
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                new Severity().withCode("1").withPosition(1),
                new Severity().withCode("2").withPosition(2),
                new Severity().withCode("3").withPosition(3)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2", "3");
    }

    @Test
    public void getActiveSeverities_should_return_all_if_no_run_is_included_in_thresholds() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withSeverityTags(null),
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withSeverityTags("1,2"))));
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                new Severity().withCode("1").withPosition(1),
                new Severity().withCode("2").withPosition(2),
                new Severity().withCode("3").withPosition(3)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2", "3");
    }

    @Test
    public void getActiveSeverities_should_sort_returned_severities_by_position_instead_of_by_code() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("a,b"))));
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                new Severity().withCode("b").withPosition(1),
                new Severity().withCode("a").withPosition(2)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("b", "a");
    }

    @Test
    public void getActiveSeverities_should_return_only_active_severities() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("1,2"))));
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                new Severity().withCode("1").withPosition(1),
                new Severity().withCode("2").withPosition(2),
                new Severity().withCode("3").withPosition(3)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("1", "2");
    }

    @Test
    public void getActiveSeverities_should_return_severity_ordered_by_position() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("3,2"))));
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Arrays.asList(
                new Severity().withCode("1").withPosition(2),
                new Severity().withCode("2").withPosition(1),
                new Severity().withCode("3").withPosition(3)));

        // WHEN
        SortedSet<Severity> activeSeverities = cut.getActiveSeverities(execution);

        // THEN
        assertThat(activeSeverities.stream().map(Severity::getCode)).containsExactly("2", "3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getActiveSeverities_should_throw_IllegalArgumentException_on_unknown_severity() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition().withProjectId(PROJECT_ID))
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withSeverityTags("any"))));
        when(severityRepository.findAllByProjectIdOrderByPosition(PROJECT_ID)).thenReturn(Collections.emptyList());

        // WHEN
        cut.getActiveSeverities(execution);
    }

    @Test
    public void computeQualityOfSeverity_should_map_severity_to_a_dto() {
        // GIVEN
        Execution execution = new Execution();
        Severity someSeverity = Severity.ALL;
        SeverityDTO someSeverityDto = new SeverityDTO();
        when(severityMapper.toDto(someSeverity)).thenReturn(someSeverityDto);

        // WHEN
        QualitySeverityDTO quality = cut.computeQualityOfSeverity(execution, someSeverity, null);

        // THEN
        assertThat(quality.getSeverity()).isSameAs(someSeverityDto);
    }

    @Test
    public void computeQualityOfSeverity_should_compute_well() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Arrays.asList(
                        // Count it
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(
                                        passedScenario().withSeverity("some")))),
                        // Not the same severity
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(
                                        passedScenario().withSeverity("OTHER")))),
                        // Not included in quality computation
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(
                                        passedScenario().withSeverity("some")))))));
        Severity someSeverity = new Severity().withCode("some");
        QualityThreshold threshold = new QualityThreshold().withFailure(1).withWarning(1);

        // WHEN
        QualitySeverityDTO quality = cut.computeQualityOfSeverity(execution, someSeverity, threshold);

        // THEN
        assertThat(quality.getScenarioCounts().getTotal()).isEqualTo(1);
        assertThat(quality.getPercent()).isEqualTo(100);
        assertThat(quality.getStatus()).isEqualTo(QualityStatus.PASSED);
    }

    @Test
    public void getRunsToIncludeInQuality_should_return_all_runs_to_include() {
        // GIVEN
        final Run run1 = run().withIncludeInThresholds(Boolean.TRUE);
        final Run run2 = run().withIncludeInThresholds(Boolean.TRUE);
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Arrays.asList(
                        run1,
                        run2)));

        // WHEN
        Set<Run> runs = cut.getRunsToIncludeInQuality(execution);

        // THEN
        assertThat(runs).containsOnly(run1, run2);
    }

    @Test
    public void getRunsToIncludeInQuality_should_return_only_runs_to_include() {
        // GIVEN
        final Run run1 = run().withIncludeInThresholds(Boolean.TRUE);
        final Run run2 = run().withIncludeInThresholds(Boolean.FALSE);
        final Run run3 = run().withIncludeInThresholds(null);
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Arrays.asList(
                        run1,
                        run2,
                        run3)));

        // WHEN
        Set<Run> runs = cut.getRunsToIncludeInQuality(execution);

        // THEN
        assertThat(runs).containsOnly(run1);
    }

    @Test
    public void isComplete_should_return_true_if_all_runs_are_DONE() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.singleton(new ExecutedScenario())),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.singleton(new ExecutedScenario())))));

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isTrue();
    }

    @Test
    public void isComplete_should_return_false_if_any_run_is_not_DONE() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Arrays.asList(
                        run().withIncludeInThresholds(Boolean.TRUE).withStatus(JobStatus.DONE),
                        run().withIncludeInThresholds(Boolean.TRUE).withStatus(JobStatus.PENDING))));

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isFalse();
    }

    @Test
    public void isComplete_should_return_false_if_any_run_is_not_DONE_even_if_it_has_executedScenarios() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Collections.singletonList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.PENDING)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(new ExecutedScenario()))))));

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isFalse();
    }

    @Test
    public void isComplete_should_return_false_if_any_run_has_no_executed_scenario() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(new HashSet<>(Collections.singletonList(new ExecutedScenario()))),
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.emptySet()))));

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isFalse();
    }

    @Test
    public void isComplete_should_return_true_if_all_runs_to_include_are_DONE() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(new HashSet<>(Arrays.asList(
                        run()
                                .withIncludeInThresholds(Boolean.TRUE)
                                .withStatus(JobStatus.DONE)
                                .withExecutedScenarios(Collections.singleton(new ExecutedScenario())),
                        run()
                                .withIncludeInThresholds(Boolean.FALSE)
                                .withStatus(JobStatus.PENDING))));

        // WHEN
        boolean complete = cut.isComplete(execution);

        // THEN
        assertThat(complete).isTrue();
    }

    @Test
    public void countScenariosOfSeverity_should_count_all() {
        // GIVEN
        final Set<Run> runs = Collections.singleton(run()
                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                        passedScenario(),
                        failedScenario()))));

        // WHEN
        ScenarioCountDTO counts = cut.countScenariosOfSeverity(runs, Severity.ALL);

        // THEN
        assertThat(counts.getTotal()).isEqualTo(2);
        assertThat(counts.getPassed()).isEqualTo(1);
        assertThat(counts.getFailed()).isEqualTo(1);
    }

    @Test
    public void countScenariosOfSeverity_should_count_default() {
        // GIVEN
        final Set<Run> runs = Collections.singleton(run()
                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                        passedScenario().withSeverity(""),
                        failedScenario().withSeverity("some"),
                        failedScenario().withSeverity("other")))));
        Severity someSeverity = new Severity().withCode("some").withDefaultOnMissing(true);

        // WHEN
        ScenarioCountDTO counts = cut.countScenariosOfSeverity(runs, someSeverity);

        // THEN
        assertThat(counts.getTotal()).isEqualTo(2);
        assertThat(counts.getPassed()).isEqualTo(1);
        assertThat(counts.getFailed()).isEqualTo(1);
    }

    @Test
    public void countScenariosOfSeverity_should_count_only_the_given_severity() {
        // GIVEN
        final Set<Run> runs = Collections.singleton(run()
                .withExecutedScenarios(new HashSet<>(Arrays.asList(
                        passedScenario().withSeverity(""),
                        failedScenario().withSeverity("some")))));
        Severity someSeverity = new Severity().withCode("some");

        // WHEN
        ScenarioCountDTO counts = cut.countScenariosOfSeverity(runs, someSeverity);

        // THEN
        assertThat(counts.getTotal()).isEqualTo(1);
        assertThat(counts.getPassed()).isEqualTo(0);
        assertThat(counts.getFailed()).isEqualTo(1);
    }

    @Test
    public void countScenario_should_increment_total_count() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO();

        // WHEN
        cut.countScenario(counts, passedScenario());

        // THEN
        assertThat(counts.getTotal()).isEqualTo(1);
    }

    @Test
    public void countScenario_should_increment_passed_count() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO()
                .withFailed(7)
                .withPassed(2);
        ExecutedScenario executedScenario = new ExecutedScenario();

        // WHEN
        cut.countScenario(counts, executedScenario);

        // THEN
        assertThat(counts.getFailed()).isEqualTo(7);
        assertThat(counts.getPassed()).isEqualTo(3);
    }

    @Test
    public void countScenario_should_increment_failed_count() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO()
                .withFailed(7)
                .withPassed(2);

        // WHEN
        cut.countScenario(counts, failedScenario());

        // THEN
        assertThat(counts.getFailed()).isEqualTo(8);
        assertThat(counts.getPassed()).isEqualTo(2);
    }

    @Test
    public void getQualityPercentage_should_return_100_when_no_scenario() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO().withTotal(0);

        // WHEN
        final int percentage = cut.getQualityPercentage(counts);

        // THEN
        assertThat(percentage).isEqualTo(100);
    }

    @Test
    public void getQualityPercentage_should_return_50_when_half_scenarios_succeed() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO().withPassed(1).withTotal(2);

        // WHEN
        final int percentage = cut.getQualityPercentage(counts);

        // THEN
        assertThat(percentage).isEqualTo(50);
    }

    @Test
    public void getQualityPercentage_should_truncate_instead_of_round_to_not_return_100_when_not_fully_passed() {
        // GIVEN
        ScenarioCountDTO counts = new ScenarioCountDTO().withPassed(199).withTotal(200);

        // WHEN
        final int percentage = cut.getQualityPercentage(counts);

        // THEN
        assertThat(percentage).isEqualTo(99);
    }

    private Run run() {
        return new Run()
                .withCountry(new Country()
                        .withCode(String.valueOf(lastId++)));
    }

    private ExecutedScenario passedScenario() {
        return new ExecutedScenario().withLine(lastId++);
    }

    private ExecutedScenario failedScenario() {
        ExecutedScenario executedScenario = passedScenario();
        executedScenario.getErrors().add(new Error());
        return executedScenario;
    }

}
