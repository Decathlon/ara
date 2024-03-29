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

import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.dto.run.RunWithQualitiesDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.transaction.Transactional;
import java.util.List;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

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
class ExecutionHistoryServiceIT {

    @Autowired
    private ExecutionHistoryService cut;

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionHistoryServiceIT-getLatestExecutionHistories.xml" })
    void getLatestExecutionHistories_should_return_executions_with_teamIds_and_counts() {
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
    void getExecution_should_return_execution_with_teamIds_and_counts() throws NotFoundException {
        // GIVEN
        long projectId = 1;
        long executionId = 1;

        // WHEN
        final ExecutionHistoryPointDTO execution = cut.getExecution(projectId, executionId);

        // THEN
        assertThat(execution.getRuns().get(0).getQualitiesPerTeamAndSeverity().get("10").get("medium").getTotal()).isEqualTo(1);
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionHistoryServiceIT-getLatestExecutionHistories.xml" })
    void getExecution_should_throw_NotFoundException_when_not_found() throws NotFoundException {
        // GIVEN
        long projectId = 1;
        long nonexistentExecutionId = NONEXISTENT.longValue();

        // WHEN
        assertThrows(NotFoundException.class, () -> cut.getExecution(projectId, nonexistentExecutionId));
    }

}
