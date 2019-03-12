package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.service.ProblemPatternService;
import com.decathlon.ara.service.ProblemService;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.DeletePatternDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutionDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.firstPageOf10;
import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class ProblemPatternResourceIT {

    private static final String PROJECT_CODE = "p";
    private static final long PROJECT_ID = 1;

    @Autowired
    private ProblemPatternService problemPatternService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ProblemPatternResource cut;

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testDeleteOkOnSinglePatternProblem() {
        // GIVEN
        long patternIdToDelete = 1011;
        long problemIdOfPatternToDelete = 1001;

        // WHEN
        ResponseEntity<DeletePatternDTO> response = cut.delete(PROJECT_CODE, patternIdToDelete);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.problem-pattern.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo(String.valueOf(patternIdToDelete));
        assertThat(response.getBody().getDeletedProblem().getId()).isEqualTo(problemIdOfPatternToDelete);
        assertThat(response.getBody().getDeletedProblem().getName()).isEqualTo("Step 2 needs rework");
        assertThat(catchThrowable(() -> problemPatternService.findOne(PROJECT_ID, patternIdToDelete))).isInstanceOf(NotFoundException.class);
        assertThat(catchThrowable(() -> problemService.findOne(PROJECT_ID, problemIdOfPatternToDelete))).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testDeleteOkOnTwoPatternsProblem() throws BadRequestException {
        // GIVEN
        long patternIdToDelete = 1011;
        long problemIdOfPatternToDelete = 1002;
        // Move pattern 1011 into problem 1002, so that problem 1002 has two patterns, and deleting pattern 1011 will not delete problem 1002
        problemService.pickUpPattern(PROJECT_ID, problemIdOfPatternToDelete, patternIdToDelete);

        // WHEN
        ResponseEntity<DeletePatternDTO> response = cut.delete(PROJECT_CODE, patternIdToDelete);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.problem-pattern.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo(String.valueOf(patternIdToDelete));
        assertThat(response.getBody().getDeletedProblem()).isNull();
        assertThat(catchThrowable(() -> problemPatternService.findOne(PROJECT_ID, patternIdToDelete))).isInstanceOf(NotFoundException.class);
        assertThat(problemService.findOne(PROJECT_ID, problemIdOfPatternToDelete)).isNotNull();
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testDeleteNonexistent() {
        // WHEN
        ResponseEntity<DeletePatternDTO> response = cut.delete(PROJECT_CODE, NONEXISTENT.longValue());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The pattern does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem-pattern");
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testGetProblemPatternErrors() {
        int pageIndex = 0;
        int pageSize = 2;

        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> response =
                cut.getProblemPatternErrors(PROJECT_CODE, 1011, PageRequest.of(pageIndex, pageSize));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> page = response.getBody();
        assertThat(page.hasContent()).isTrue();
        assertThat(page.getNumber()).isEqualTo(pageIndex);
        assertThat(page.getSize()).isEqualTo(pageSize);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(6);

        // Errors are ordered by Error.id
        List<ErrorWithExecutedScenarioAndRunAndExecutionDTO> content = page.getContent();
        assertThat(content.size()).isEqualTo(2);

        ErrorWithExecutedScenarioAndRunAndExecutionDTO error = content.get(0);
        assertThat(error.getId()).isEqualTo(121);
        assertThat(error.getStep()).isEqualTo("Step 2");
        assertThat(error.getStepDefinition()).isEqualTo("^Step 2$");
        assertThat(error.getStepLine()).isEqualTo(43);
        assertThat(error.getException()).isEqualTo("Exception 2");

        final ExecutedScenarioWithRunAndExecutionDTO executedScenario = error.getExecutedScenario();
        assertThat(executedScenario.getFeatureFile()).isEqualTo("a.feature");
        assertThat(executedScenario.getFeatureName()).isEqualTo("Feature A");
        assertThat(executedScenario.getSeverity()).isEqualTo("high");
        assertThat(executedScenario.getName()).isEqualTo("Scenario a");
        assertThat(executedScenario.getLine()).isEqualTo(41);
        assertThat(executedScenario.getScreenshotUrl()).isEqualTo("http://screenshot.org/");
        assertThat(executedScenario.getVideoUrl()).isEqualTo("http://video.org/");
        assertThat(executedScenario.getContent()).isEqualTo("42:failed:Step 1\n43:failed:Step 2\n44:failed:Step 3");

        RunWithExecutionDTO run = executedScenario.getRun();
        assertThat(run.getId()).isEqualTo(12);
        assertThat(run.getCountry().getCode()).isEqualTo("cn");
        assertThat(run.getCountry().getName()).isEqualTo("China");
        assertThat(run.getType().getCode()).isEqualTo("api");
        assertThat(run.getType().getName()).isEqualTo("API");
        assertThat(run.getPlatform()).isEqualTo("euin");
        assertThat(run.getJobUrl()).isEqualTo("http://run.jobs.org/12/");

        ExecutionDTO execution = run.getExecution();
        assertThat(execution.getId()).isEqualTo(1);
        assertThat(execution.getName()).isEqualTo("day");
        assertThat(execution.getBranch()).isEqualTo("develop");
        assertThat(execution.getRelease()).isEqualTo("1711");
        assertThat(execution.getVersion()).isEqualTo("1711.1709245958");
        assertThat(execution.getBuildDateTime()).isEqualTo(timestamp(2017, Calendar.SEPTEMBER, 24, 11, 59, 58));
        assertThat(execution.getTestDateTime()).isEqualTo(timestamp(2017, Calendar.SEPTEMBER, 25, 11, 59, 58));
        assertThat(execution.getJobUrl()).isEqualTo("http://execution.jobs.org/1/");
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(execution.getDiscardReason()).isNull();
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testGetProblemPatternErrorsNonexistent() {
        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> response =
                cut.getProblemPatternErrors(PROJECT_CODE, NONEXISTENT.longValue(), firstPageOf10());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testUpdate() throws NotFoundException {
        // GIVEN
        Long problemId = Long.valueOf(1001);
        long problemPatternId = 1011;
        ProblemPatternDTO pattern = new ProblemPatternDTO();
        pattern.setFeatureFile("c.feature");
        pattern.setStep("Step 7");

        // WHEN
        ResponseEntity<ProblemPatternDTO> response = cut.update(PROJECT_CODE, problemPatternId, pattern);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(problemPatternId);
        assertThat(response.getBody().getFeatureFile()).isEqualTo("c.feature");
        assertThat(response.getBody().getStep()).isEqualTo("Step 7");
        assertThat(response.getBody().getScenarioName()).isNull(); // Previously contained a value
        assertThat(response.getBody().getException()).isNull(); // Previously contained a value

        List<ProblemPattern> patterns = problemRepository.findById(problemId)
                .orElseThrow(() -> new AssertionError("Problem should have existed by now!"))
                .getPatterns();
        assertThat(patterns).hasSize(1);
        assertThat(patterns.get(0).getId()).isEqualTo(problemPatternId);
        assertThat(patterns.get(0).getFeatureFile()).isEqualTo("c.feature");
        assertThat(patterns.get(0).getStep()).isEqualTo("Step 7");
        assertThat(patterns.get(0).getScenarioName()).isNull(); // Previously contained a value
        assertThat(patterns.get(0).getException()).isNull(); // Previously contained a value

        List<ErrorWithExecutedScenarioAndRunAndExecutionDTO> errors =
                problemService.getProblemErrors(PROJECT_ID, problemId.longValue(), firstPageOf10()).getContent();
        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).getId()).isEqualTo(124);
        assertThat(errors.get(1).getId()).isEqualTo(313);
    }

    @Test
    @DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
    public void testUpdateNonexistent() {
        ResponseEntity<ProblemPatternDTO> response = cut.update(PROJECT_CODE, NONEXISTENT.longValue(), new ProblemPatternDTO());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The pattern does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem-pattern");
    }

    @Test
    @DatabaseSetup("/dbunit/ProblemPatternResourceIT-update.xml")
    public void update_ShouldDoNothing_WhenUpdatingAPatternWithoutChangingIt() {
        // GIVEN
        long problemPatternId = 12; // Already is pattern with step="Step 2"
        ProblemPatternDTO problemPattern = new ProblemPatternDTO().withStep("Step 2");

        // WHEN
        ResponseEntity<ProblemPatternDTO> response = cut.update(PROJECT_CODE, problemPatternId, problemPattern);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DatabaseSetup("/dbunit/ProblemPatternResourceIT-update.xml")
    public void update_ShouldThrowNotUnique_WhenUpdatingPatternWithSameCriteriaAsAnotherPatternOfTheSameProblem() {
        // GIVEN
        long problemPatternId = 11; // is currently step="Step 1" & its problem already has a pattern with step="Step 2"
        ProblemPatternDTO problemPattern = new ProblemPatternDTO().withStep("Step 2");

        // WHEN
        ResponseEntity<ProblemPatternDTO> response = cut.update(PROJECT_CODE, problemPatternId, problemPattern);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A pattern with the same criterion already exists for this problem.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem-pattern");
    }

}
