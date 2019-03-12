package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.request.ExecutedScenarioHistoryInputDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/scenario-history.xml")
public class ExecutedScenarioResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private ExecutedScenarioResource cut;

    private static ExecutedScenarioHistoryInputDTO input(String cucumberId) {
        return new ExecutedScenarioHistoryInputDTO().withCucumberId(cucumberId);
    }

    @Test
    public void testGetHistoryWithoutFilter() {
        final ResponseEntity<List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO>> response = cut.getHistory(
                PROJECT_CODE,
                input("a;scenario-a"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(18); // Do not check order: it will be checked in tests below
    }

    @Test
    public void testGetHistoryWithFilterCombinationAndResultHasAllInformationFilled() {
        final ResponseEntity<List<ExecutedScenarioWithRunAndTeamIdsAndExecutionAndErrorsAndProblemsDTO>> response = cut.getHistory(
                PROJECT_CODE,
                input("a;scenario-a")
                        .withCycleName("night")
                        .withBranch("stab")
                        .withCountryCode("be")
                        .withRunTypeCode("api"));

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
    public void testGetHistoryWithCycleNameFilter() {
        assertOkWithSize(
                input("a;scenario-a").withCycleName("day"),
                longs(2131, 2121, 2111, 1131, 1111));
    }

    @Test
    public void testGetHistoryWithBranchFilter() {
        assertOkWithSize(
                input("a;scenario-a").withBranch("stab"),
                longs(2331, 2333, 2321, 2311, 1331, 1321, 1311));
    }

    @Test
    public void testGetHistoryWithCountryCodeFilter() {
        assertOkWithSize(
                input("a;scenario-a").withCountryCode("be"),
                longs(2331, 2333, 2231, 2131, 1331, 1231, 1131));
    }

    @Test
    public void testGetHistoryWithRunTypeCodeFilter() {
        assertOkWithSize(
                input("a;scenario-a").withRunTypeCode("firefox"),
                longs(2311, 2211, 2111, 1311, 1211, 1111));
    }

    @Test
    public void testGetHistoryWithTooRestrictiveFilters() {
        assertOkWithSize(
                input("a;scenario-a").withCountryCode("404"),
                longs());
    }

    @Test
    public void testGetHistoryWithNullCucumberId() {
        assertCucumberIdIsMandatory(input(null));
    }

    @Test
    public void testGetHistoryWithEmptyCucumberId() {
        assertCucumberIdIsMandatory(input(""));
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
