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

package com.decathlon.ara.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.run.ExecutedScenarioHandlingCountsDTO;
import com.decathlon.ara.service.dto.run.RunDTO;
import com.decathlon.ara.service.dto.run.RunWithQualitiesDTO;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class ExecutionHistoryServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private ExecutedScenarioRepository executedScenarioRepository;

    @Mock
    private FunctionalityRepository functionalityRepository;

    @Mock
    private SeverityService severityService;

    @InjectMocks
    private ExecutionHistoryService cut;

    @Test
    void fillQualities_should_fill_qualitiesPerSeverity_and_qualitiesPerTeamAndSeverity() {
        // GIVEN
        final Long runId = Long.valueOf(8);
        RunWithQualitiesDTO run = new RunWithQualitiesDTO();
        TestUtil.setField(run, RunDTO.class, "id", runId);
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
    void addScenario_should_increment_severity_and_global() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin(0, 0, "medium", null, 0, 0);
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
    void addScenario_should_increment_severity_and_global_for_default_severity() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin(0, 0, "", null, 0, 0);
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
    void addScenarioForSeverity_should_increment_total_and_passed_for_succeed_scenario() {
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
    void addScenarioForSeverity_should_increment_total_and_unhandled_for_failed_scenario_without_problem() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin(0, 0, null, null, 1, 0);
        Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity = new HashMap<>();

        // WHEN
        cut.addScenarioForSeverity(executedScenarioJoin, qualitiesPerSeverity, "medium");

        // THEN
        assertThat(qualitiesPerSeverity.size()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getUnhandled()).isEqualTo(1);
    }

    @Test
    void addScenarioForSeverity_should_increment_total_and_handled_for_failed_scenario_with_problem() {
        // GIVEN
        ExecutedScenarioWithErrorAndProblemJoin executedScenarioJoin = new ExecutedScenarioWithErrorAndProblemJoin(0, 0, null, null, 2, 1);
        Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity = new HashMap<>();

        // WHEN
        cut.addScenarioForSeverity(executedScenarioJoin, qualitiesPerSeverity, "medium");

        // THEN
        assertThat(qualitiesPerSeverity.size()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getTotal()).isEqualTo(1);
        assertThat(qualitiesPerSeverity.get("medium").getHandled()).isEqualTo(1);
    }

}
