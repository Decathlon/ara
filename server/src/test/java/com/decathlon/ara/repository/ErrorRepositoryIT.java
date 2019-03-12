package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.firstPageOf10;
import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup({ "/dbunit/full-small-fake-dataset.xml" })
public class ErrorRepositoryIT {

    @Autowired
    private ErrorRepository cut;

    @Autowired
    private ProblemPatternRepository problemPatternRepository;

    private static List<Long> idsOf(Set<Error> errors) {
        return errors.stream().map(Error::getId).collect(Collectors.toList());
    }

    @Test
    public void findMatchingErrors_ShouldReturnTheOnlyMatchingError_WhenPatternCriteriaAreVeryRestrictive() {
        // GIVEN
        long projectId = 1;
        ProblemPattern pattern = new ProblemPattern();
        pattern.setFeatureFile("a.feature");
        pattern.setFeatureName("Feature A");
        pattern.setScenarioName("Scenario a");
        pattern.setStep("Step 1");
        pattern.setStepDefinition("^Step 1$");
        pattern.setException("Exception 1");
        pattern.setRelease("1711");
        pattern.setCountry(new Country().withCode("cn"));
        pattern.setType(new Type().withCode("firefox"));
        pattern.setTypeIsBrowser(Boolean.TRUE);
        pattern.setTypeIsMobile(Boolean.FALSE);
        pattern.setPlatform("euin");

        // WHEN
        Page<Error> errors = cut.findMatchingErrors(projectId, pattern, firstPageOf10());

        // THEN
        assertThat(errors.getNumberOfElements()).isEqualTo(1);
        assertThat(errors.getContent().get(0).getExecutedScenario().getFeatureName()).isEqualTo("Feature A");
        assertThat(errors.getContent().get(0).getExecutedScenario().getRun().getExecution().getJobUrl()).isEqualTo("http://execution.jobs.org/1/");
    }

    @Test
    @DatabaseSetup({ "/dbunit/new-errors.xml" })
    public void autoAssignProblemsToNewErrors_ShouldAttachExistingProblemPatternsToNewlyAddedErrors() {
        // GIVEN
        long projectId = 1;
        // For testing purpose, act the inverse way:
        // act as if the existing errors were just added in database AND the new problem was existing in database
        List<Long> newErrorIds = Arrays.asList(longs(
                211, 212, 221, 222, 223, 231, 232, 233,
                111, 112, 113, 121, 122, 123, 124,
                311, 312, 313, 314
        ));

        // WHEN
        cut.autoAssignProblemsToNewErrors(projectId, newErrorIds);

        // THEN
        Set<Error> affectedErrors = problemPatternRepository.getOne(Long.valueOf(1041)).getErrors();
        assertThat(affectedErrors).hasSize(3);
        assertThat(idsOf(affectedErrors)).contains(longs(124, 313, 314));
    }

}
