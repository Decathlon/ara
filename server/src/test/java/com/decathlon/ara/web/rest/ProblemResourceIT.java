package com.decathlon.ara.web.rest;

import com.decathlon.ara.defect.TestDefectAdapter;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.service.dto.error.ErrorDTO;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemAggregateDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.problem.ProblemFilterDTO;
import com.decathlon.ara.service.dto.problem.ProblemWithAggregateDTO;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsAndAggregateTDO;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.PickUpPatternDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutionDTO;
import com.decathlon.ara.service.dto.stability.ExecutionStabilityDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.firstPageOf10;
import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.longs;
import static com.decathlon.ara.util.TestUtil.timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
@DatabaseSetup("/dbunit/full-small-fake-dataset-defect-settings.xml")
public class ProblemResourceIT {

    private static final String PROJECT_CODE = "p";

    @SpyBean
    private TestDefectAdapter defectAdapter;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProblemResource cut;

    private static ProblemDTO openProblem() {
        return new ProblemDTO().withStatus(ProblemStatus.OPEN);
    }

    private static void assertProblem1003(ProblemDTO problem) {
        assertThat(problem.getId()).isEqualTo(1003);
        assertThat(problem.getName()).isEqualTo("Scenario d is unstable on China desktop");
        assertThat(problem.getComment()).isEqualTo("Everything is in the title");
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problem.getBlamedTeam().getId()).isEqualTo(25);
        assertThat(problem.getBlamedTeam().getName()).isEqualTo("Search & Choose");
        assertThat(problem.getBlamedTeam().isAssignableToProblems()).isTrue();
        assertThat(problem.getBlamedTeam().isAssignableToFunctionalities()).isTrue();
        assertThat(problem.getDefectId()).isNull();
        assertThat(problem.getDefectUrl()).isNull();
        assertThat(problem.getRootCause()).isNull();
    }

    private static void assertProblem1003Aggregate(ProblemAggregateDTO aggregate) {
        // Errors matching "Scenario d" + "cn" + "firefox"
        assertThat(aggregate.getPatternCount()).isEqualTo(1);
        assertThat(aggregate.getErrorCount()).isEqualTo(2);
        assertThat(aggregate.getScenarioCount()).isEqualTo(1);
        assertThat(aggregate.getFirstScenarioName()).isEqualTo("Scenario d");
        assertThat(aggregate.getBranchCount()).isEqualTo(1);
        assertThat(aggregate.getFirstBranch()).isEqualTo("develop");
        assertThat(aggregate.getReleaseCount()).isEqualTo(1);
        assertThat(aggregate.getFirstRelease()).isEqualTo("1711");
        assertThat(aggregate.getVersionCount()).isEqualTo(1);
        assertThat(aggregate.getFirstVersion()).isEqualTo("1711.1709245958");
        assertThat(aggregate.getCountryCount()).isEqualTo(1);
        assertThat(aggregate.getFirstCountry().getCode()).isEqualTo("cn");
        assertThat(aggregate.getFirstCountry().getName()).isEqualTo("China");
        assertThat(aggregate.getTypeCount()).isEqualTo(1);
        assertThat(aggregate.getFirstType().getCode()).isEqualTo("firefox");
        assertThat(aggregate.getFirstType().getName()).isEqualTo("Desktop");
        assertThat(aggregate.getPlatformCount()).isEqualTo(1);
        assertThat(aggregate.getFirstPlatform()).isEqualTo("euin");
        assertThat(stabilityToString(aggregate.getCycleStabilities().get(0).getExecutionStabilities()))
                .isEqualTo("(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(E:1)(O:2)");
        assertThat(stabilityToString(aggregate.getCycleStabilities().get(1).getExecutionStabilities()))
                .isEqualTo("(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)");
        assertThat(stabilityToString(aggregate.getCycleStabilities().get(2).getExecutionStabilities()))
                .isEqualTo("(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(O:3)");
    }

    private static String stabilityToString(List<ExecutionStabilityDTO> executionStabilityDTOS) {
        return executionStabilityDTOS
                .stream()
                .map(s -> "(" + s.getStatus() + ":" + s.getExecutionId() + ")")
                .collect(Collectors.joining());
    }

    private static void assertProblem1003Patterns(ProblemWithPatternsAndAggregateTDO problem) {
        List<ProblemPatternDTO> patterns = problem.getPatterns();
        assertThat(patterns).hasSize(1);
        ProblemPatternDTO pattern = patterns.get(0);
        assertThat(pattern.getId()).isEqualTo(1031);
        assertThat(pattern.getScenarioName()).isEqualTo("Scenario d");
        assertThat(pattern.getCountry().getCode()).isEqualTo("cn");
        assertThat(pattern.getCountry().getName()).isEqualTo("China");
        assertThat(pattern.getType().getCode()).isEqualTo("firefox");
        assertThat(pattern.getType().getName()).isEqualTo("Desktop");
    }

    public static void assertProblem1001(ProblemDTO problem) {
        assertThat(problem.getId()).isEqualTo(1001);
        assertThat(problem.getName()).isEqualTo("Step 2 needs rework");
        assertThat(problem.getComment()).isEqualTo("Not working anymore");
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problem.getBlamedTeam().getId()).isEqualTo(2);
        assertThat(problem.getBlamedTeam().getName()).isEqualTo("Buy");
        assertThat(problem.getBlamedTeam().isAssignableToProblems()).isTrue();
        assertThat(problem.getBlamedTeam().isAssignableToFunctionalities()).isTrue();
        assertThat(problem.getDefectId()).isEqualTo("1");
        assertThat(problem.getDefectUrl()).isEqualTo("http://test.defects.org/1");
        assertThat(problem.getRootCause().getId()).isEqualTo(3); // Compared to problem 1003, a root cause is set
        assertThat(problem.getRootCause().getName()).isEqualTo("NRT was not updated");
    }

    private static void assertProblem1001Aggregate(ProblemWithAggregateDTO problem) {

        ProblemAggregateDTO aggregate = problem.getAggregate();
        assertThat(aggregate.getPatternCount()).isEqualTo(1);
        assertThat(aggregate.getErrorCount()).isEqualTo(6);
        assertThat(aggregate.getScenarioCount()).isEqualTo(3);
        // Don't care about FirstScenarioName
        assertThat(aggregate.getBranchCount()).isEqualTo(2);
        // Don't care about FirstBranch
        assertThat(aggregate.getReleaseCount()).isEqualTo(2);
        // Don't care about FirstRelease
        assertThat(aggregate.getVersionCount()).isEqualTo(3);
        // Don't care about FirstVersion
        assertThat(aggregate.getCountryCount()).isEqualTo(2);
        // Don't care about FirstCountry
        assertThat(aggregate.getTypeCount()).isEqualTo(2);
        // Don't care about FirstType
        assertThat(aggregate.getPlatformCount()).isEqualTo(2);
        // Don't care about FirstPlatform
        assertThat(stabilityToString(aggregate.getCycleStabilities().get(0).getExecutionStabilities()))
                .isEqualTo("(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(E:1)(E:2)");
        assertThat(stabilityToString(aggregate.getCycleStabilities().get(1).getExecutionStabilities()))
                .isEqualTo("(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)");
        assertThat(stabilityToString(aggregate.getCycleStabilities().get(2).getExecutionStabilities()))
                .isEqualTo("(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(-:null)(E:3)");
    }

    private static void assertProblem1001Patterns(ProblemWithPatternsAndAggregateTDO problem) {
        List<ProblemPatternDTO> patterns = problem.getPatterns();
        assertThat(patterns).hasSize(1);
        ProblemPatternDTO pattern = patterns.get(0);
        assertThat(pattern.getId()).isEqualTo(1011);
        assertThat(pattern.getStep()).isEqualTo("Step 2");
    }

    private static void assertUpdatedProblem1001(ProblemDTO problem) {
        assertThat(problem.getId()).isEqualTo(1001);
        assertThat(problem.getName()).isEqualTo("New name");
        assertThat(problem.getComment()).isEqualTo("New comment");
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.OPEN); // Not updated
        assertThat(problem.getBlamedTeam().getId()).isEqualTo(25);
        assertThat(problem.getBlamedTeam().getName()).isEqualTo("Search & Choose");
        assertThat(problem.getBlamedTeam().isAssignableToProblems()).isTrue();
        assertThat(problem.getBlamedTeam().isAssignableToFunctionalities()).isTrue();
        assertThat(problem.getDefectId()).isEqualTo("42");
        assertThat(problem.getDefectUrl()).isEqualTo("http://test.defects.org/42");
        assertThat(problem.getRootCause().getId()).isEqualTo(5);
        assertThat(problem.getRootCause().getName()).isEqualTo("Regression");
        assertThat(problem.getDefectUrl()).isEqualTo("http://test.defects.org/42"); // Generated
    }

    private static TeamDTO teamNotAssignableToProblems() {
        return new TeamDTO()
                .withId(Long.valueOf(102)) // This one is not assignable to problems
                .withAssignableToProblems(
                        true); // Check the server don't use client's provided configuration but uses server's one
    }

    @Test
    public void testGetOpenProblems() {
        // GIVEN
        final ProblemFilterDTO openStatusFilter = new ProblemFilterDTO().withStatus(ProblemStatusFilter.OPEN);

        // WHEN
        // There are 3 problems, and only 2 open: make sure the closed ones do not appear
        ResponseEntity<Page<ProblemWithAggregateDTO>> response = cut
                .getMatchingOnes(PROJECT_CODE, openStatusFilter, firstPageOf10());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Page<ProblemWithAggregateDTO> page = response.getBody();
        assertThat(page.hasContent()).isTrue();
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(10);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(2);

        // Problems are ordered by creationDateTime DESC (so: most recent first)
        List<ProblemWithAggregateDTO> content = page.getContent();
        assertThat(content.size()).isEqualTo(2);

        assertProblem1003(content.get(0));
        assertProblem1003Aggregate(content.get(0).getAggregate());
        assertProblem1001(content.get(1));
        assertProblem1001Aggregate(content.get(1));
    }

    @Test
    public void testGetProblemsOfTeam2() {
        // GIVEN
        ProblemFilterDTO filter = new ProblemFilterDTO()
                .withBlamedTeamId(Long.valueOf(2));

        // WHEN
        ResponseEntity<Page<ProblemWithAggregateDTO>> response = cut.getMatchingOnes(PROJECT_CODE, filter, firstPageOf10());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ProblemWithAggregateDTO> content = response.getBody().getContent();
        assertThat(content.size()).isEqualTo(1);
        assertThat(content.get(0).getId()).isEqualTo(1001);
    }

    @Test
    public void testGetProblemsWithTooRestrictiveFilters() {
        // GIVEN
        ProblemFilterDTO tooRestrictiveFilter = new ProblemFilterDTO()
                .withName("Nonexistent")
                .withDefectId("42");

        // WHEN
        ResponseEntity<Page<ProblemWithAggregateDTO>> response = cut.getMatchingOnes(PROJECT_CODE, tooRestrictiveFilter, firstPageOf10());

        // THEN
        List<ProblemWithAggregateDTO> content = response.getBody().getContent();
        assertThat(content.size()).isEqualTo(0);
    }

    @Test
    public void testGetOneWithRootCause() {
        ResponseEntity<ProblemWithPatternsAndAggregateTDO> response = cut.getOne(PROJECT_CODE, 1001);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertProblem1001(response.getBody());
        assertProblem1001Patterns(response.getBody());
    }

    @Test
    public void testGetOneWithSeveralPatternColumns() {
        ResponseEntity<ProblemWithPatternsAndAggregateTDO> response = cut.getOne(PROJECT_CODE, 1003);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertProblem1003(response.getBody());
        assertProblem1003Patterns(response.getBody());
        assertProblem1003Aggregate(response.getBody().getAggregate());
    }

    @Test
    public void testGetOneNonexistent() {
        ResponseEntity<ProblemWithPatternsAndAggregateTDO> response = cut.getOne(PROJECT_CODE, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetProblemErrors() {
        int pageIndex = 0;
        int pageSize = 2;

        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> response = cut
                .getProblemErrors(PROJECT_CODE, 1001, PageRequest.of(pageIndex, pageSize));
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

        ExecutedScenarioWithRunAndExecutionDTO executedScenario = error.getExecutedScenario();
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
    public void testGetProblemErrorsNonexistent() {
        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> response = cut
                .getProblemErrors(PROJECT_CODE, NONEXISTENT.longValue(), firstPageOf10());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testCreate() throws FetchException {
        // GIVEN
        Date closeDateTime = timestamp(2017, 12, 31, 23, 59, 59);
        when(Boolean.valueOf(defectAdapter.isValidId(anyLong(), any()))).thenReturn(Boolean.TRUE);
        doReturn(Collections.singletonList(new Defect("42", ProblemStatus.CLOSED, closeDateTime)))
                .when(defectAdapter).getStatuses(anyLong(), any());

        ProblemWithPatternsDTO problem = new ProblemWithPatternsDTO();
        problem.setName("  \t  New problem  \t  ");
        problem.setComment("  \t  Comment  \t  ");
        problem.setStatus(ProblemStatus.OPEN); // Is ignored and set to the defect's status (OPEN if no defect)
        problem.setBlamedTeam(new TeamDTO(Long.valueOf(2), null, false, false));
        problem.setDefectId("  \t  42  \t  ");
        problem.setDefectUrl("USELESS: SHOULD BE AUTOMATICALLY GENERATED");
        problem.setRootCause(null);

        ProblemPatternDTO pattern = new ProblemPatternDTO();
        pattern.setScenarioName("Scenario a");
        pattern.setStep("Step 2");
        problem.setPatterns(Collections.singletonList(pattern));

        // WHEN
        ResponseEntity<ProblemWithPatternsDTO> response = cut.create(PROJECT_CODE, problem);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/problems/" + response.getBody().getId());

        // Check new IDs, and quickly check a few returned values for them to match the values in the request
        ProblemWithPatternsDTO createdProblem = response.getBody();
        assertThat(createdProblem.getId()).isPositive();
        assertThat(createdProblem.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(createdProblem.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(createdProblem.getClosingDateTime()).isEqualTo(closeDateTime);
        assertThat(createdProblem.getName()).isEqualTo("New problem");
        assertThat(createdProblem.getDefectUrl()).isEqualTo("http://test.defects.org/42"); // Generated
        ProblemPatternDTO createdPattern = createdProblem.getPatterns().get(0);
        assertThat(createdPattern.getId()).isPositive();
        assertThat(createdPattern.getScenarioName()).isEqualTo("Scenario a");
        assertThat(createdPattern.getStep()).isEqualTo("Step 2");

        // Check the returned response it coherent with what's stored in database
        ResponseEntity<ProblemWithPatternsAndAggregateTDO> responseOfGet = cut.getOne(PROJECT_CODE, createdProblem.getId().longValue());
        assertThat(responseOfGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseOfGet.getBody().getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(responseOfGet.getBody().getName()).isEqualTo("New problem");
        assertThat(responseOfGet.getBody().getPatterns().get(0).getScenarioName()).isEqualTo("Scenario a");

        // Check the correct two errors have been assigned to the problem
        PageRequest pageable = PageRequest.of(0, 2);
        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> errorsResponse = cut
                .getProblemErrors(PROJECT_CODE, createdProblem.getId().longValue(), pageable);
        assertThat(errorsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(errorsResponse.getBody().getTotalElements()).isEqualTo(2);
        List<ErrorWithExecutedScenarioAndRunAndExecutionDTO> errors = errorsResponse.getBody().getContent();
        assertThat(errors.get(0).getId()).isEqualTo(121);
        assertThat(errors.get(1).getId()).isEqualTo(211);
    }

    @Test
    public void testCreateProblemWithAnId() {
        ProblemWithPatternsDTO problem = new ProblemWithPatternsDTO();
        problem.setId(NONEXISTENT);
        ResponseEntity<ProblemWithPatternsDTO> response = cut.create(PROJECT_CODE, problem);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.id_exists");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A new problem cannot already have an ID.");
    }

    @Test
    public void testCreateProblemWithExistingName() {
        ProblemWithPatternsDTO problem = new ProblemWithPatternsDTO();
        problem.setName("Step 2 needs rework");
        ResponseEntity<ProblemWithPatternsDTO> response = cut.create(PROJECT_CODE, problem);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo(
                "The name is already used by another problem. Consider appending the aggregation criteria to this existing problem.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1001");
    }

    @Test
    public void testCreateProblemWithExistingDefectId() {
        ProblemWithPatternsDTO problem = new ProblemWithPatternsDTO();
        problem.setDefectId("1");
        ResponseEntity<ProblemWithPatternsDTO> response = cut.create(PROJECT_CODE, problem);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo(
                "The defect ID is already assigned to another problem. Consider appending the aggregation criteria to this existing problem.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("defectId");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1001");
    }

    @Test
    public void testCreateWithNonexistentTeam() {
        ProblemWithPatternsDTO problem = new ProblemWithPatternsDTO();
        problem.setName("Any");
        problem.setBlamedTeam(new TeamDTO().withId(NONEXISTENT));

        ResponseEntity<ProblemWithPatternsDTO> response = cut.create(PROJECT_CODE, problem);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team does not exist: it has perhaps been removed.");
    }

    @Test
    public void testCreateWithNonAssignableTeam() {
        ProblemWithPatternsDTO problem = new ProblemWithPatternsDTO();
        problem.setName("New problem");
        problem.setBlamedTeam(teamNotAssignableToProblems());

        ResponseEntity<ProblemWithPatternsDTO> response = cut.create(PROJECT_CODE, problem);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_assignable_team");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team cannot be assigned to a problem.");
    }

    @Test
    public void testCreateWithNonexistentRootCause() {
        ProblemWithPatternsDTO problem = new ProblemWithPatternsDTO();
        problem.setName("Any");
        problem.setRootCause(new RootCauseDTO().withId(NONEXISTENT));

        ResponseEntity<ProblemWithPatternsDTO> response = cut.create(PROJECT_CODE, problem);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The root cause does not exist: it has perhaps been removed.");
    }

    @Test
    public void testDeleteOk() {
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, 1001);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.problem.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1001");

        ResponseEntity<ProblemWithPatternsAndAggregateTDO> checkResponse = cut.getOne(PROJECT_CODE, 1);
        assertThat(checkResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeleteNonexistent() {
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The problem does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");

        // Nothing should have changed
        testGetOpenProblems();
    }

    @Test
    public void testAppendPattern() {
        long problemId = 1003;
        ProblemPatternDTO newPattern = new ProblemPatternDTO().withScenarioName("Scenario c");

        ResponseEntity<ProblemPatternDTO> response = cut.appendPattern(PROJECT_CODE, problemId, newPattern);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath())
                .isEqualTo("/api/projects/p/problems/" + problemId + "/" + response.getBody().getId());

        ProblemPatternDTO createdPattern = response.getBody();
        assertThat(createdPattern.getId()).isPositive();
        assertThat(createdPattern.getScenarioName()).isEqualTo("Scenario c");

        entityManager.flush();

        List<ProblemPatternDTO> patterns = cut.getOne(PROJECT_CODE, problemId).getBody().getPatterns();
        assertThat(patterns).hasSize(2);
        assertThat(patterns.stream().map(ProblemPatternDTO::getId)).contains(Long.valueOf(1031), createdPattern.getId());

        PageRequest pageable = PageRequest.of(0, 10);
        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> errors = cut
                .getProblemErrors(PROJECT_CODE, problemId, pageable);
        Stream<Long> actualErrorIds = errors.getBody().getContent().stream().map(ErrorDTO::getId);
        assertThat(actualErrorIds).containsExactly(longs(112, 113, 123)); // Errors are ordered by id
    }

    @Test
    public void testAppendPatternNonexistent() {
        ProblemPatternDTO newPattern = new ProblemPatternDTO().withScenarioName("Not used");
        ResponseEntity<ProblemPatternDTO> response = cut.appendPattern(PROJECT_CODE, NONEXISTENT.longValue(), newPattern);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The problem does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
    }

    @Test
    public void testAppendDuplicatePattern() {
        ProblemPatternDTO newPattern = new ProblemPatternDTO().withStep("Step 2");
        ResponseEntity<ProblemPatternDTO> response = cut.appendPattern(PROJECT_CODE, 1001, newPattern);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("A pattern with the same criterion already exists for this problem.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem-pattern");
    }

    @Test
    public void testUpdateNonexistent() {
        ResponseEntity<ProblemDTO> response = cut.updateProperties(PROJECT_CODE, NONEXISTENT.longValue(), new ProblemDTO());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The problem does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
    }

    @Test
    public void testUpdateWithExistingName() {
        // Renaming 1002 with the name of 1001 must fail
        ResponseEntity<ProblemDTO> response = cut
                .updateProperties(PROJECT_CODE, 1002, new ProblemDTO().withName("Step 2 needs rework"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo(
                "The name is already used by another problem. Consider moving the aggregation criteria to this existing problem.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1001");
    }

    @Test
    public void testUpdateKeepingSameNameAndDefectId() {
        // Updating properties but keeping the same name should not throw not_unique!
        long problemId = 1001;
        ProblemDTO problem = new ProblemDTO();
        problem.setName("Step 2 needs rework");
        problem.setDefectId("1");
        ResponseEntity<ProblemDTO> response = cut.updateProperties(PROJECT_CODE, problemId, problem);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(problemId);
    }

    @Test
    public void testUpdateWithoutDefectId() {
        long problemId = 1001;
        ProblemDTO problem = new ProblemDTO();
        problem.setStatus(ProblemStatus.OPEN);
        problem.setName("Step 2 needs rework");
        problem.setCreationDateTime(timestamp(2018, Calendar.JANUARY, 1, 12, 0, 0));

        // Making problem 1001 AND problem 1003 having null defect ID
        cut.updateProperties(PROJECT_CODE, problemId, problem);

        // Updating properties without defect id should not throw an exception because several defects share a null defect id!
        ResponseEntity<ProblemDTO> response = cut.updateProperties(PROJECT_CODE, problemId, problem);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(problemId);
    }

    @Test
    public void testUpdate() throws FetchException {
        // GIVEN
        when(Boolean.valueOf(defectAdapter.isValidId(anyLong(), any()))).thenReturn(Boolean.TRUE);
        doReturn(Collections.singletonList(new Defect("42", ProblemStatus.OPEN, null)))
                .when(defectAdapter).getStatuses(anyLong(), any());
        long problemId = 1001;
        // We update EVERY fields
        ProblemDTO problem = new ProblemDTO();
        problem.setId(Long.valueOf(problemId));
        problem.setName("  \t  New name  \t  ");
        problem.setComment("  \t  New comment  \t  ");
        problem.setStatus(ProblemStatus.CLOSED); // Status change will be ignored
        problem.setBlamedTeam(new TeamDTO(Long.valueOf(25), "USELESS: SHOULD BECOME Search & Choose", false, false));
        problem.setDefectId("  \t  42  \t  ");
        problem.setDefectUrl("USELESS: SHOULD BE AUTOMATICALLY GENERATED");
        problem.setRootCause(new RootCauseDTO(Long.valueOf(5), "USELESS: SHOULD BECOME Regression"));

        // WHEN
        ResponseEntity<ProblemDTO> response = cut.updateProperties(PROJECT_CODE, problemId, problem);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertUpdatedProblem1001(response.getBody());

        ResponseEntity<ProblemWithPatternsAndAggregateTDO> responseOfGet = cut.getOne(PROJECT_CODE, problemId);
        assertThat(responseOfGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertUpdatedProblem1001(responseOfGet.getBody());
        assertProblem1001Patterns(responseOfGet.getBody());
    }

    @Test
    public void testUpdateWithNonexistentTeam() {
        ProblemDTO problem = new ProblemDTO().withBlamedTeam(new TeamDTO().withId(NONEXISTENT)).withName("Any");

        ResponseEntity<ProblemDTO> response = cut.updateProperties(PROJECT_CODE, 1001, problem);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team does not exist: it has perhaps been removed.");
    }

    @Test
    public void testUpdateWithNonAssignableTeam() {
        ProblemDTO problem = new ProblemDTO().withBlamedTeam(teamNotAssignableToProblems()).withName("Any name");

        ResponseEntity<ProblemDTO> response = cut.updateProperties(PROJECT_CODE, 1001, problem);

        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_assignable_team");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team cannot be assigned to a problem.");
    }

    @Test
    public void testUpdateWithNonexistentRootCause() {
        ProblemDTO problem = new ProblemDTO().withRootCause(new RootCauseDTO().withId(NONEXISTENT)).withName("Any");

        ResponseEntity<ProblemDTO> response = cut.updateProperties(PROJECT_CODE, 1001, problem);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The root cause does not exist: it has perhaps been removed.");
    }

    @Test
    public void testPickUpPattern() {
        // Initial state:
        // problem #1001: problem_pattern #1011
        // problem #1002: problem_pattern #1021
        // problem #1003: problem_pattern #1031
        long destinationProblemId;
        long sourcePatternId;
        ResponseEntity<PickUpPatternDTO> response;

        // First move:
        // problem #1001 [DELETED]
        // problem #1002: problem_pattern #1021, problem_pattern #1011
        // problem #1003: problem_pattern #1031
        destinationProblemId = 1002;
        sourcePatternId = 1011;
        response = cut.pickUpPattern(PROJECT_CODE, destinationProblemId, sourcePatternId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDestinationProblem().getId()).isEqualTo(destinationProblemId);
        assertThat(response.getBody().getDestinationProblem().getName()).isEqualTo("Scenario b throws exceptions");
        assertThat(response.getBody().getDeletedProblem().getId()).isEqualTo(1001);
        assertThat(response.getBody().getDeletedProblem().getName()).isEqualTo("Step 2 needs rework");
        assertThat(cut.getOne(PROJECT_CODE, destinationProblemId).getBody().getPatterns()).hasSize(2);
        assertThat(cut.getOne(PROJECT_CODE, 1001).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        entityManager.flush();

        // Second move:
        // problem #1002: problem_pattern #1021
        // problem #1003: problem_pattern #1031, problem_pattern #1011
        destinationProblemId = 1003;
        sourcePatternId = 1011;
        response = cut.pickUpPattern(PROJECT_CODE, destinationProblemId, sourcePatternId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDestinationProblem().getId()).isEqualTo(destinationProblemId);
        assertThat(response.getBody().getDestinationProblem().getName())
                .isEqualTo("Scenario d is unstable on China desktop");
        assertThat(response.getBody().getDeletedProblem()).isNull();
        assertThat(cut.getOne(PROJECT_CODE, 1002).getBody().getPatterns()).hasSize(1);
        assertThat(cut.getOne(PROJECT_CODE, 1003).getBody().getPatterns()).hasSize(2);

        entityManager.flush();
    }

    @Test
    public void testErroneousPickUpPattern() {
        long destinationProblemId = 1001;
        long sourcePatternId = 1011;
        ResponseEntity<PickUpPatternDTO> response;

        response = cut.pickUpPattern(PROJECT_CODE, NONEXISTENT.longValue(), sourcePatternId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The problem where to move the pattern does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");

        response = cut.pickUpPattern(PROJECT_CODE, destinationProblemId, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The pattern to move does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem-pattern");

        response = cut.pickUpPattern(PROJECT_CODE, destinationProblemId, sourcePatternId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.source_is_destination");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("You tried to move the pattern into its own problem.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
    }

    @Test
    public void testPickUpPatternAsDuplicate() {
        // Create data for this test
        long sourceProblemId = 1002;
        long destinationProblemId = 1001;
        ProblemPatternDTO patternExistingInDestinationProblem = new ProblemPatternDTO().withStep("Step 2");
        long sourcePatternId = cut.appendPattern(PROJECT_CODE, sourceProblemId, patternExistingInDestinationProblem)
                .getBody().getId().longValue();

        ResponseEntity<PickUpPatternDTO> response = cut.pickUpPattern(PROJECT_CODE, destinationProblemId, sourcePatternId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("A pattern with the same criterion already exists for this problem.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem-pattern");
    }

    @Test
    public void testClose() {
        long problemId = 1003; // It has no root cause

        ResponseEntity<ProblemDTO> response = cut.close(PROJECT_CODE, problemId, 5);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody().getId()).isEqualTo(problemId);
        assertThat(response.getBody().getName()).isEqualTo("Scenario d is unstable on China desktop");
        assertThat(response.getBody().getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(response.getBody().getBlamedTeam().getId()).isEqualTo(Long.valueOf(25));
        assertThat(response.getBody().getBlamedTeam().getName()).isEqualTo("Search & Choose");
        assertThat(response.getBody().getBlamedTeam().isAssignableToProblems()).isTrue();
        assertThat(response.getBody().getBlamedTeam().isAssignableToFunctionalities()).isTrue();
        assertThat(response.getBody().getRootCause().getId()).isEqualTo(Long.valueOf(5));
        assertThat(response.getBody().getRootCause().getName()).isEqualTo("Regression");

        ResponseEntity<ProblemWithPatternsAndAggregateTDO> responseOfGet = cut.getOne(PROJECT_CODE, problemId);
        assertThat(responseOfGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseOfGet.getBody().getStatus()).isEqualTo(ProblemStatus.CLOSED);
    }

    @Test
    public void testCloseNonExistent() {
        ResponseEntity<ProblemDTO> response = cut.close(PROJECT_CODE, NONEXISTENT.longValue(), 5);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The problem does not exist: it has perhaps been removed.");
    }

    @Test
    public void testCloseWithoutRootCause() {
        long problemIdWithRootCause = 1003;
        ResponseEntity<ProblemDTO> response = cut.close(PROJECT_CODE, problemIdWithRootCause, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The root cause does not exist: it has perhaps been removed.");
    }

    @Test
    public void testReopen() {
        long closedProblemId = 1002;

        ResponseEntity<ProblemDTO> response = cut.reopen(PROJECT_CODE, closedProblemId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody().getId()).isEqualTo(closedProblemId);
        assertThat(response.getBody().getName()).isEqualTo("Scenario b throws exceptions");
        assertThat(response.getBody().getStatus()).isEqualTo(ProblemStatus.OPEN);

        ResponseEntity<ProblemWithPatternsAndAggregateTDO> responseOfGet = cut.getOne(PROJECT_CODE, closedProblemId);
        assertThat(responseOfGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseOfGet.getBody().getStatus()).isEqualTo(ProblemStatus.OPEN);
    }

    @Test
    public void testReopenNonExistent() {
        ResponseEntity<ProblemDTO> response = cut.reopen(PROJECT_CODE, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE))
                .isEqualTo("The problem does not exist: it has perhaps been removed.");
    }

    @Test
    public void refreshDefectStatus_should_return_not_found_for_nonexistent_id() {
        // WHEN
        ResponseEntity<ProblemDTO> response = cut.refreshDefectStatus(PROJECT_CODE, NONEXISTENT.longValue());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The problem does not exist: it has perhaps been removed.");
    }

    @Test
    public void refreshDefectStatus_should_not_touch_problem_if_no_defect_id() {
        // WHEN
        ResponseEntity<ProblemDTO> response = cut.refreshDefectStatus(PROJECT_CODE, 1002);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(response.getBody().getDefectExistence()).isNull();
    }

    @Test
    public void refreshDefectStatus_should_set_NONEXISTENT_and_reopen_on_disappeared_defect() throws FetchException {
        // GIVEN
        doReturn(Collections.emptyList()).when(defectAdapter).getStatuses(anyLong(), any());

        // WHEN
        ResponseEntity<ProblemDTO> response = cut.refreshDefectStatus(PROJECT_CODE, 1001);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDefectExistence()).isEqualTo(DefectExistence.NONEXISTENT);
        assertThat(response.getBody().getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(response.getBody().getClosingDateTime()).isNull();
    }

    @Test
    public void refreshDefectStatus_should_set_EXISTS_and_status_OPEN_when_defect_is_open_in_tracking_system() throws FetchException {
        // GIVEN
        doReturn(Collections.singletonList(new Defect("1", ProblemStatus.OPEN, null)))
                .when(defectAdapter).getStatuses(anyLong(), any());

        // WHEN
        ResponseEntity<ProblemDTO> response = cut.refreshDefectStatus(PROJECT_CODE, 1001);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(response.getBody().getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(response.getBody().getClosingDateTime()).isNull();
    }

    @Test
    public void refreshDefectStatus_should_set_EXISTS_and_status_CLOSED_when_defect_is_closed_in_tracking_system() throws FetchException {
        // GIVEN
        Date newDate = new Date();
        doReturn(Collections.singletonList(new Defect("1", ProblemStatus.CLOSED, newDate)))
                .when(defectAdapter).getStatuses(anyLong(), any());

        // WHEN
        ResponseEntity<ProblemDTO> response = cut.refreshDefectStatus(PROJECT_CODE, 1001);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(response.getBody().getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(response.getBody().getClosingDateTime()).isEqualTo(newDate);
    }

    @Test
    public void refreshDefectStatus_should_return_bad_gateway_when_defect_tracking_system_do_not_respond() throws FetchException {
        // GIVEN
        doThrow(new FetchException("any")).when(defectAdapter).getStatuses(anyLong(), any());
        when(defectAdapter.getName()).thenReturn("SYSTEM");

        // WHEN
        ResponseEntity<ProblemDTO> response = cut.refreshDefectStatus(PROJECT_CODE, 1001);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.bad_gateway");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("problem");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A problem occurred while contacting SYSTEM.");
    }

}
