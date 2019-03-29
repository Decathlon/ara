package com.decathlon.ara.web.rest;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.BuildToIndex;
import com.decathlon.ara.ci.service.ExecutionCrawlerService;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.service.dto.error.ErrorDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.execution.ExecutionWithHandlingCountsDTO;
import com.decathlon.ara.service.dto.run.RunDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.longs;
import static com.decathlon.ara.util.TestUtil.timestamp;
import static com.decathlon.ara.web.rest.ProblemResourceIT.assertProblem1001;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class ExecutionResourceIT {

    private static final String PROJECT_CODE = "p";

    @MockBean
    private ExecutionCrawlerService executionCrawlerService;

    @Autowired
    private ExecutionResource cut;

    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    private static void assertExecutionId2(ExecutionDTO execution) {
        assertThat(execution.getId()).isEqualTo(2);
        assertThat(execution.getName()).isEqualTo("day");
        assertThat(execution.getBranch()).isEqualTo("develop");
        assertThat(execution.getRelease()).isEqualTo("1711");
        assertThat(execution.getVersion()).isEqualTo("1711.1709245756");
        assertThat(execution.getBuildDateTime()).isEqualTo(timestamp(2017, Calendar.SEPTEMBER, 24, 12, 57, 56));
        assertThat(execution.getTestDateTime()).isEqualTo(timestamp(2017, Calendar.SEPTEMBER, 25, 12, 57, 56));
        assertThat(execution.getJobUrl()).isEqualTo("http://execution.jobs.org/2/");
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(execution.getDiscardReason()).isNull();
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testGetPage() {
        int pageIndex = 0;
        int pageSize = 2;

        ResponseEntity<Page<ExecutionWithHandlingCountsDTO>> response = cut.getPage(PROJECT_CODE, PageRequest.of(pageIndex, pageSize));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final Page<ExecutionWithHandlingCountsDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.hasContent()).isTrue();
        assertThat(page.getNumber()).isEqualTo(pageIndex);
        assertThat(page.getSize()).isEqualTo(pageSize);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(3);

        // Cycle Runs are ordered by test-date&time descending
        List<ExecutionWithHandlingCountsDTO> content = page.getContent();
        assertThat(content.size()).isEqualTo(2);

        ExecutionWithHandlingCountsDTO execution = content.get(0);
        assertExecutionId2(execution);
        assertThat(execution.getScenarioCounts().getPassed()).isEqualTo(0);
        assertThat(execution.getScenarioCounts().getUnhandled()).isEqualTo(0);
        assertThat(execution.getScenarioCounts().getHandled()).isEqualTo(5);

        // Only check the second execution is the expected one, depending on sorting order; but no need to check individual values again
        assertThat(content.get(1).getId()).isEqualTo(1);
        // And check aggregates too, as they could be tricky, and they differ in their executed business rules
        assertThat(content.get(1).getScenarioCounts().getPassed()).isEqualTo(0);
        assertThat(content.get(1).getScenarioCounts().getUnhandled()).isEqualTo(3);
        assertThat(content.get(1).getScenarioCounts().getHandled()).isEqualTo(2);
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testGetOne() {
        ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> response = cut.getOne(PROJECT_CODE, 2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO execution = response.getBody();
        assertThat(execution).isNotNull();
        assertExecutionId2(execution);

        assertThat(execution.getCountryDeployments().size()).isEqualTo(2);

        // CountryDeployments are ordered by country.code ASC
        assertThat(execution.getCountryDeployments().get(0).getCountry().getCode()).isEqualTo("cn");
        assertThat(execution.getCountryDeployments().get(1).getCountry().getCode()).isEqualTo("nl");

        assertThat(execution.getRuns().size()).isEqualTo(3);

        // Runs are ordered by country.code ASC, type.code ASC
        RunDTO run = execution.getRuns().get(0);
        assertThat(run.getId()).isEqualTo(23);
        assertThat(run.getCountry().getCode()).isEqualTo("cn");
        assertThat(run.getCountry().getName()).isEqualTo("China");
        assertThat(run.getType().getCode()).isEqualTo("firefox");
        assertThat(run.getType().getName()).isEqualTo("Desktop");
        assertThat(run.getPlatform()).isEqualTo("euin");
        assertThat(run.getJobUrl()).isEqualTo("http://run.jobs.org/23/");

        // Only check the second execution is the expected one, depending on sorting order; but no need to check individual values again
        assertThat(execution.getRuns().get(1).getId()).isEqualTo(22);
        assertThat(execution.getRuns().get(2).getId()).isEqualTo(21);
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    @DatabaseSetup("/dbunit/full-small-fake-dataset-defect-settings.xml")
    public void testGetOneWithProblems() {
        ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> response = cut.getOne(PROJECT_CODE, 3);
        ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO execution = response.getBody();

        assertThat(execution).isNotNull();
        assertThat(execution.getRuns()).isNotNull();
        RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO run = execution.getRuns().get(0);
        assertThat(run.getId()).isEqualTo(31);
        assertThat(run.getCountry().getCode()).isEqualTo("cn");
        assertThat(run.getCountry().getName()).isEqualTo("China");
        assertThat(run.getType().getCode()).isEqualTo("api");
        assertThat(run.getType().getName()).isEqualTo("API");
        assertThat(run.getPlatform()).isEqualTo("euin");
        assertThat(run.getJobUrl()).isEqualTo("http://run.jobs.org/31/");

        final List<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO> executedScenarios = run.getExecutedScenarios();
        assertThat(executedScenarios).hasSize(2);
        assertThat(executedScenarios.get(0).getErrors()).hasSize(2);
        assertThat(executedScenarios.get(1).getErrors()).hasSize(2);

        // Errors are ordered by id
        assertThat(executedScenarios.stream().flatMap(e -> e.getErrors().stream()).map(ErrorDTO::getId)).containsExactly(longs(311, 312, 313, 314));

        // Assert only a few values
        assertThat(executedScenarios.get(0).getFeatureName()).isEqualTo("Feature B");
        assertThat(executedScenarios.get(0).getName()).isEqualTo("Functionalities 42, 666: Scenario e");
        assertThat(executedScenarios.get(0).getTeamIds()).containsExactly(Long.valueOf(6)); // Functionality 666 is for team 6
        assertThat(executedScenarios.get(1).getTeamIds()).isEmpty();

        // Only index 1 has a problem associated to it
        assertThat(executedScenarios.get(0).getErrors().get(0).getProblems()).isEmpty();
        assertThat(executedScenarios.get(0).getErrors().get(1).getProblems()).hasSize(1);
        assertThat(executedScenarios.get(1).getErrors().get(0).getProblems()).isEmpty();
        assertThat(executedScenarios.get(1).getErrors().get(1).getProblems()).isEmpty();
        assertProblem1001(executedScenarios.get(0).getErrors().get(1).getProblems().get(0));
    }

    @Test
    public void testGetOneNonexistent() {
        ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> response = cut.getOne(PROJECT_CODE, NONEXISTENT);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testDiscard() {
        ResponseEntity<ExecutionDTO> response = cut.discard(PROJECT_CODE, 1, "Discard Reason");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(response.getBody().getAcceptance()).isEqualTo(ExecutionAcceptance.DISCARDED);
        assertThat(response.getBody().getDiscardReason()).isEqualTo("Discard Reason");

        ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> responseGet = cut.getOne(PROJECT_CODE, 1);
        assertThat(responseGet.getBody()).isNotNull();
        assertThat(responseGet.getBody().getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(responseGet.getBody().getAcceptance()).isEqualTo(ExecutionAcceptance.DISCARDED);
        assertThat(responseGet.getBody().getDiscardReason()).isEqualTo("Discard Reason");
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testDiscardWithoutReason() {
        ResponseEntity<ExecutionDTO> response = cut.discard(PROJECT_CODE, NONEXISTENT, "");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.reason_mandatory_for_discarded_executions");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("execution");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A reason is mandatory when discarding an execution.");
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testDiscardNonexistent() {
        ResponseEntity<ExecutionDTO> response = cut.discard(PROJECT_CODE, NONEXISTENT, "Discard Reason");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testUnDiscard() {
        ResponseEntity<ExecutionDTO> response = cut.unDiscard(PROJECT_CODE, 3);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(response.getBody().getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(response.getBody().getDiscardReason()).isNull();

        ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> responseGet = cut.getOne(PROJECT_CODE, 3);
        assertThat(responseGet.getBody()).isNotNull();
        assertThat(responseGet.getBody().getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(responseGet.getBody().getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(responseGet.getBody().getDiscardReason()).isNull();
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testUnDiscardNonexistent() {
        ResponseEntity<ExecutionDTO> response = cut.unDiscard(PROJECT_CODE, NONEXISTENT);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DatabaseSetup("/dbunit/ExecutionRepository-getLatestEligibleVersions.xml")
    public void testGetLatestEligibleVersions() {
        // WHEN
        final ResponseEntity<List<ExecutionDTO>> response = cut.getLatestEligibleVersions(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ExecutionDTO> execution = response.getBody();
        assertThat(execution).isNotNull();
        assertThat(execution.size()).isEqualTo(2);

        // develop first (ordered by branch)
        assertThat(execution.get(0).getId()).isEqualTo(25072);
        assertThat(execution.get(0).getName()).isEqualTo("day");
        assertThat(execution.get(0).getBranch()).isEqualTo("develop");
        assertThat(execution.get(0).getRelease()).isEqualTo("1808");
        assertThat(execution.get(0).getVersion()).isEqualTo("1808.1807201757");
        assertThat(execution.get(0).getBuildDateTime()).isEqualTo(timestamp(2018, Calendar.AUGUST, 8, 20, 57, 0));
        assertThat(execution.get(0).getTestDateTime()).isEqualTo(timestamp(2018, Calendar.AUGUST, 23, 7, 7, 48));
        assertThat(execution.get(0).getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.get(0).getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);

        // Then stab
        assertThat(execution.get(1).getId()).isEqualTo(2502);
        assertThat(execution.get(1).getName()).isEqualTo("day");
        assertThat(execution.get(1).getBranch()).isEqualTo("stab");
        assertThat(execution.get(1).getRelease()).isEqualTo("1808");
        assertThat(execution.get(1).getVersion()).isEqualTo("1808.1807201757");
        assertThat(execution.get(1).getBuildDateTime()).isEqualTo(timestamp(2018, Calendar.AUGUST, 8, 20, 57, 0));
        assertThat(execution.get(1).getTestDateTime()).isEqualTo(timestamp(2018, Calendar.AUGUST, 23, 24, 0, 4));
        assertThat(execution.get(1).getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.get(1).getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
    }

    @Test
    @DatabaseSetup("/dbunit/brand-new-project-dataset.xml")
    public void testUploadPostman() throws IOException {
        // Given
        InputStream zipContent = getClass().getResourceAsStream("/postman/ara-reports-upload.zip");
        MultipartFile file = new MockMultipartFile("zip", zipContent);
        String projectCode = "prj";
        String branchName = "develop";
        String cycleName = "day";
        File incomingPath = new File("/tmp/ara/executions/custom/" + projectCode
                + "/" + branchName + "/" + cycleName + "/incoming/1547216212139");
        Build build = new Build().withLink(incomingPath.getAbsolutePath() + File.separator);
        CycleDefinition cycle = new CycleDefinition(1L, 2, branchName, cycleName, 1);
        BuildToIndex buildToIndex = new BuildToIndex(cycle, build);
        try {
            // When
            ResponseEntity<Void> responseEntity = cut.upload(projectCode, branchName, cycleName, file);
            // Then
            Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            Mockito.verify(executionCrawlerService, Mockito.timeout(1000)).crawl(buildToIndex);
        } finally {
            if (':' == incomingPath.getAbsolutePath().charAt(1)) { // Windows like path
                FileUtils.deleteQuietly(new File("/tmp"));
            } else {
                FileUtils.deleteQuietly(new File("/tmp/ara"));
            }
        }
    }

}
