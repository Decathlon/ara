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

package com.decathlon.ara.web.rest;

import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.request.ExecutedScenarioHistoryInputDTO;
import com.decathlon.ara.util.TestUtil;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@Disabled
@SpringBootTest
@TestExecutionListeners({
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@TestPropertySource(
        locations = "classpath:application-db-h2.properties")
@Transactional
@DatabaseSetup("/dbunit/scenario-history.xml")
class ExecutedScenarioResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private ExecutedScenarioResource cut;

    private static ExecutedScenarioHistoryInputDTO input(String cucumberId, String cycleName, String branch, String countryCode, String runTypeCode) {
        ExecutedScenarioHistoryInputDTO executedScenarioHistoryInputDTO = new ExecutedScenarioHistoryInputDTO();
        TestUtil.setField(executedScenarioHistoryInputDTO, "cucumberId", cucumberId);
        return executedScenarioHistoryInputDTO;
    }

    @Test
    void testGetHistoryWithoutFilter() {
        final ResponseEntity<List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO>> response = cut.getHistory(
                PROJECT_CODE,
                input("a;scenario-a", null, null, null, null));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(18); // Do not check order: it will be checked in tests below
    }

    @Test
    void testGetHistoryWithFilterCombinationAndResultHasAllInformationFilled() {
        final ResponseEntity<List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO>> response = cut.getHistory(
                PROJECT_CODE,
                input("a;scenario-a", "night", "stab", "be", "api"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);

        {
            final ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO scenario = response.getBody().get(0);
            assertThat(scenario.getId()).isEqualTo(2331);
            // Team IDs are assigned by code: make sure it is always run
            assertThat(scenario.getTeamIds()).containsExactly(Long.valueOf(6));
            // Ascendant run and execution are filled with data (no need to check all scenarios)
            assertThat(scenario.getRun().getCountry().getCode()).isEqualTo("be");
            assertThat(scenario.getRun().getType().getCode()).isEqualTo("api");
            assertThat(scenario.getRun().getExecution().getName()).isEqualTo("night");
            assertThat(scenario.getRun().getExecution().getBranch()).isEqualTo("stab");
            // Descendant error exists and is correctly filled (with problem)
            assertThat(scenario.getErrors()).hasSize(1);
            assertThat(scenario.getErrors().get(0).getStep()).isEqualTo("Step 1");
            assertThat(scenario.getErrors().get(0).getProblems()).hasSize(1);
            assertThat(scenario.getErrors().get(0).getProblems().get(0).getName()).isEqualTo("Step 1 needs rework");
        }

        {
            final ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO scenario = response.getBody().get(1);
            // This is a duplicate-name scenario: it must be last as it has higher line number than the other one
            assertThat(scenario.getId()).isEqualTo(2333);
            // Has no error
            assertThat(scenario.getErrors()).isEmpty();
        }

        {
            final ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO scenario = response.getBody().get(2);
            assertThat(scenario.getId()).isEqualTo(1331);
            // Descendant error exists and is correctly filled (without problem)
            assertThat(scenario.getErrors()).hasSize(1);
            assertThat(scenario.getErrors().get(0).getStep()).isEqualTo("Step 1");
            assertThat(scenario.getErrors().get(0).getProblems()).hasSize(0);
        }
    }

    @Test
    void testGetHistoryWithCycleNameFilter() {
        assertOkWithSize(
                input("a;scenario-a", "day", null, null, null),
                longs(2131, 2121, 2111, 1131, 1111));
    }

    @Test
    void testGetHistoryWithBranchFilter() {
        assertOkWithSize(
                input("a;scenario-a", null, "stab", null, null),
                longs(2331, 2333, 2321, 2311, 1331, 1321, 1311));
    }

    @Test
    void testGetHistoryWithCountryCodeFilter() {
        assertOkWithSize(
                input("a;scenario-a", null, null, null, null),
                longs(2331, 2333, 2231, 2131, 1331, 1231, 1131));
    }

    @Test
    void testGetHistoryWithRunTypeCodeFilter() {
        assertOkWithSize(
                input("a;scenario-a", null, null, null, "firefox"),
                longs(2311, 2211, 2111, 1311, 1211, 1111));
    }

    @Test
    void testGetHistoryWithTooRestrictiveFilters() {
        assertOkWithSize(
                input("a;scenario-a", null, null, "404", null),
                longs());
    }

    @Test
    void testGetHistoryWithNullCucumberId() {
        assertCucumberIdIsMandatory(input(null, null, null, null, null));
    }

    @Test
    void testGetHistoryWithEmptyCucumberId() {
        assertCucumberIdIsMandatory(input("", null, null, null, null));
    }

    private void assertCucumberIdIsMandatory(ExecutedScenarioHistoryInputDTO input) {
        final ResponseEntity<List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO>> response =
                cut.getHistory(PROJECT_CODE, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_cucumber_id");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The cucumber ID of the scenario to get history is mandatory.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("executed-scenario");
    }

    private void assertOkWithSize(ExecutedScenarioHistoryInputDTO input, Long[] expectedIds) {
        final ResponseEntity<List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO>> response =
                cut.getHistory(PROJECT_CODE, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Executed scenarios are ordered by testDate ASC
        assertThat(response.getBody().stream().map(ExecutedScenarioDTO::getId)).containsExactly(expectedIds);
    }

}
