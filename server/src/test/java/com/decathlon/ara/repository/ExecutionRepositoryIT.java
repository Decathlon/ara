package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class ExecutionRepositoryIT {

    @Autowired
    private ExecutionRepository cut;

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionRepositoryIT-findAllByStatusAndJobUrlIn.xml" })
    public void findAllByStatusAndJobUrlIn() {
        // GIVEN
        List<String> jobUrlsToQuery = Arrays.asList(
                "http://jobs/1/", // Status DONE
                "http://jobs/2/", // Status DONE
                "http://jobs/3/", // Status RUNNING
                "http://jobs/4/", // Status UNAVAILABLE
                "http://jobs/42/", // Does not exist
                "http://jobs/404/"); // Does not exist

        // WHEN
        final List<String> foundJobUrls = cut.findJobUrls(JobStatus.DONE, jobUrlsToQuery);

        // THEN
        assertThat(foundJobUrls).containsAll(Arrays.asList("http://jobs/1/", "http://jobs/2/"));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionRepositoryIT-findLatestOfEachCycle.xml" })
    public void findLatestOfEachCycle() {
        // GIVEN
        long projectId = 1;

        // WHEN
        List<Execution> latestExecutions = cut.findLatestOfEachCycleByProjectId(projectId);

        // THEN
        assertThat(getIds(latestExecutions)).containsOnly(longs(2, 6));
    }

    private List<Long> getIds(List<Execution> latestExecutions) {
        return latestExecutions.stream()
                .map(Execution::getId)
                .collect(Collectors.toList());
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionRepositoryIT-findPreviousOf.xml" })
    public void findPreviousOf() {
        // GIVEN
        List<Execution> executions = Arrays.asList(
                new Execution().withId(Long.valueOf(3)),
                new Execution().withId(Long.valueOf(6)),
                new Execution().withId(Long.valueOf(7)));

        // WHEN
        List<Execution> previousExecutions = cut.findPreviousOf(executions);

        // THEN
        assertThat(getIds(previousExecutions)).containsOnly(longs(2, 5));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionRepositoryIT-findNextOf.xml" })
    public void findNextOf() {
        // GIVEN
        List<Execution> executions = Arrays.asList(
                new Execution().withId(Long.valueOf(2)),
                new Execution().withId(Long.valueOf(5)),
                new Execution().withId(Long.valueOf(7)));

        // WHEN
        List<Execution> nextExecutions = cut.findNextOf(executions);

        // THEN
        assertThat(getIds(nextExecutions)).containsOnly(longs(3, 6));
    }

}
