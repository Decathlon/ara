package com.decathlon.ara.service;

import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.run.RunWithQualitiesDTO;
import com.decathlon.ara.service.dto.run.ExecutedScenarioHandlingCountsDTO;
import com.decathlon.ara.service.mapper.ExecutionHistoryPointMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionHistoryServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private ExecutionHistoryPointMapper executionHistoryPointMapper;

    @Mock
    private ExecutedScenarioRepository executedScenarioRepository;

    @Mock
    private FunctionalityRepository functionalityRepository;

    @Mock
    private SeverityService severityService;

    @InjectMocks
    private ExecutionHistoryService cut;

    @Test
    public void fillQualities_should_fill_qualitiesPerSeverity_and_qualitiesPerTeamAndSeverity() {
        // GIVEN
        final Long runId = Long.valueOf(8);
        RunWithQualitiesDTO run = new RunWithQualitiesDTO();
        run.setId(runId);
        String defaultSeverityCode = "medium";
        Map<Long, Long> functionalityTeamIds = new HashMap<>();
        functionalityTeamIds.put(Long.valueOf(1), Long.valueOf(11));
        List<ExecutedScenarioWithErrorAndProblemJoin> allErrorCounts = Arrays.asList(
                new ExecutedScenarioWithErrorAndProblemJoin(111, runId.longValue(), "medium", "Functionality 1: A Title", 1, 0),
                new ExecutedScenarioWithErrorAndProblemJoin(112, runId.longValue(), "medium", "No functionality => no team", 1, 0),
                new ExecutedScenarioWithErrorAndProblemJoin(111, 404, "medium", "From another run", 1, 0));

        // WHEN
        cut.fillQualities(allErrorCounts, run, functionalityTeamIds, defaultSeverityCode);

        // THEN
        assertThat(run.getQualitiesPerSeverity().get("medium").getTotal()).isEqualTo(2);
        assertThat(run.getQualitiesPerSeverity().get("*").getTotal()).isEqualTo(2);

        assertThat(run.getQualitiesPerTeamAndSeverity().get("11").get("medium").getTotal()).isEqualTo(1);
        assertThat(run.getQualitiesPerTeamAndSeverity().get("11").get("*").getTotal()).isEqualTo(1);

        assertThat(run.getQualitiesPerTeamAndSeverity().get("-404").get("medium").getTotal()).isEqualTo(1);
        assertThat(run.getQualitiesPerTeamAndSeverity().get("-404").get("*").getTotal()).isEqualTo(1);
    }

    @Test
    public void addScenario_should_increment_severity_and_global() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin()
                .withSeverity("medium");
        Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity = new HashMap<>();

        // WHEN
        cut.addScenario(executedScenarioJoin, qualitiesPerSeverity, "medium");

        // THEN
        assertThat(qualitiesPerSeverity.size()).isEqualTo(2);
        assertThat(qualitiesPerSeverity.get("medium").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getPassed()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("*").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("*").getPassed()).isEqualTo(1);
    }

    @Test
    public void addScenario_should_increment_severity_and_global_for_default_severity() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin()
                .withSeverity("");
        Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity = new HashMap<>();

        // WHEN
        cut.addScenario(executedScenarioJoin, qualitiesPerSeverity, "medium");

        // THEN
        assertThat(qualitiesPerSeverity.size()).isEqualTo(2);
        assertThat(qualitiesPerSeverity.get("medium").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getPassed()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("*").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("*").getPassed()).isEqualTo(1);
    }

    @Test
    public void addScenarioForSeverity_should_increment_total_and_passed_for_succeed_scenario() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin();
        Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity = new HashMap<>();

        // WHEN
        cut.addScenarioForSeverity(executedScenarioJoin, qualitiesPerSeverity, "medium");

        // THEN
        assertThat(qualitiesPerSeverity.size()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getPassed()).isEqualTo(1);
    }

    @Test
    public void addScenarioForSeverity_should_increment_total_and_unhandled_for_failed_scenario_without_problem() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin()
                .withUnhandledCount(1);
        Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity = new HashMap<>();

        // WHEN
        cut.addScenarioForSeverity(executedScenarioJoin, qualitiesPerSeverity, "medium");

        // THEN
        assertThat(qualitiesPerSeverity.size()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getUnhandled()).isEqualTo(1);
    }

    @Test
    public void addScenarioForSeverity_should_increment_total_and_handled_for_failed_scenario_with_problem() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin()
                .withUnhandledCount(2)
                .withHandledCount(1);
        Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity = new HashMap<>();

        // WHEN
        cut.addScenarioForSeverity(executedScenarioJoin, qualitiesPerSeverity, "medium");

        // THEN
        assertThat(qualitiesPerSeverity.size()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getHandled()).isEqualTo(1);
    }

}
