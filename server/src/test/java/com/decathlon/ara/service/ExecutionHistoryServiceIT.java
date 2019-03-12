package com.decathlon.ara.service;

import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.dto.run.RunWithQualitiesDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class ExecutionHistoryServiceIT {

    @Autowired
    private ExecutionHistoryService cut;

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionHistoryServiceIT-getLatestExecutionHistories.xml" })
    public void getLatestExecutionHistories_should_return_executions_with_teamIds_and_counts() {
        // GIVEN
        long projectId = 1;

        // WHEN
        final List<ExecutionHistoryPointDTO> latestExecutions = cut.getLatestExecutionHistories(projectId);

        // THEN
        assertThat(latestExecutions).hasSize(2);
        assertThat(latestExecutions.get(0).getRuns()).hasSize(1);
        assertThat(latestExecutions.get(1).getRuns()).hasSize(1);

        final RunWithQualitiesDTO run = latestExecutions.get(0).getRuns().get(0);

        assertThat(run.getQualitiesPerSeverity().get("medium").getTotal()).isEqualTo(1);
        assertThat(run.getQualitiesPerSeverity().get("*").getTotal()).isEqualTo(1);

        assertThat(run.getQualitiesPerTeamAndSeverity().get("10").get("medium").getTotal()).isEqualTo(1);
        assertThat(run.getQualitiesPerTeamAndSeverity().get("10").get("*").getTotal()).isEqualTo(1);
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionHistoryServiceIT-getLatestExecutionHistories.xml" })
    public void getExecution_should_return_execution_with_teamIds_and_counts() throws NotFoundException {
        // GIVEN
        long projectId = 1;
        long executionId = 1;

        // WHEN
        final ExecutionHistoryPointDTO execution = cut.getExecution(projectId, executionId);

        // THEN
        assertThat(execution.getRuns().get(0).getQualitiesPerTeamAndSeverity().get("10").get("medium").getTotal()).isEqualTo(1);
    }

    @Test(expected = NotFoundException.class)
    @DatabaseSetup({ "/dbunit/ExecutionHistoryServiceIT-getLatestExecutionHistories.xml" })
    public void getExecution_should_throw_NotFoundException_when_not_found() throws NotFoundException {
        // GIVEN
        long projectId = 1;
        long nonexistentExecutionId = NONEXISTENT.longValue();

        // WHEN
        cut.getExecution(projectId, nonexistentExecutionId);
    }

}
