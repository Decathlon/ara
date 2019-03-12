package com.decathlon.ara.repository;

import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class ExecutedScenarioRepositoryIT {

    @Autowired
    private ExecutedScenarioRepository cut;

    @Test
    @DatabaseSetup({ "/dbunit/ExecutedScenarioRepositoryIT-findAllErrorCounts.xml" })
    public void testFindAllErrorCounts() {
        // GIVEN
        Long runId = 11L;

        // WHEN
        final List<ExecutedScenarioWithErrorAndProblemJoin> errorCounts = cut.findAllErrorAndProblemCounts(Collections.singleton(runId));

        // THEN
        assertThat(errorCounts).containsOnly(
                new ExecutedScenarioWithErrorAndProblemJoin(111, 11, "medium", "With unidentified error", 1, 0),
                new ExecutedScenarioWithErrorAndProblemJoin(112, 11, "medium", "With identified error", 1, 1),
                new ExecutedScenarioWithErrorAndProblemJoin(113, 11, "sanity-check", "Without error", 0, 0));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutedScenarioRepositoryIT-findAllErrorAndProblemCount_even_closed_ones.xml"})
    public void testFindAllErrorAndProblemCount_even_closed_ones() {
        // GIVEN
        Long runId = 11L;
        // WHEN
        List<ExecutedScenarioWithErrorAndProblemJoin> allErrorAndProblemCounts = cut.findAllErrorAndProblemCounts(Collections.singleton(runId));
        // THEN
        assertThat(allErrorAndProblemCounts).containsOnly(
                new ExecutedScenarioWithErrorAndProblemJoin(111, 11, "medium", "With unidentified error", 1, 0),
                new ExecutedScenarioWithErrorAndProblemJoin(112, 11, "medium", "With identified error", 1, 1),
                new ExecutedScenarioWithErrorAndProblemJoin(113, 11, "sanity-check", "Without error", 0, 0),
                new ExecutedScenarioWithErrorAndProblemJoin(114, 11, "high", "With identified, closed, error", 0, 1),
                new ExecutedScenarioWithErrorAndProblemJoin(115, 11, "high", "With identified, closed (with date), error", 0, 1));
    }
}
