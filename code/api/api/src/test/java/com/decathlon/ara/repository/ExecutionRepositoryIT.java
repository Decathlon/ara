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

package com.decathlon.ara.repository;

import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@SpringBootTest
@TestExecutionListeners({
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@TestPropertySource(properties = {
        "ara.database.target=h2"
})
@Transactional
class ExecutionRepositoryIT {

    @Autowired
    private ExecutionRepository cut;

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionRepositoryIT-findAllByStatusAndJobUrlIn.xml" })
    void findAllByStatusAndJobUrlIn() {
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
    void findLatestOfEachCycle() {
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
                .toList();
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionRepositoryIT-findPreviousOf.xml" })
    void findPreviousOf() {
        // GIVEN
        List<Long> executionsId = Arrays.asList(Long.valueOf(3), Long.valueOf(6), Long.valueOf(7));

        // WHEN
        List<Execution> previousExecutions = cut.findPreviousOf(executionsId);

        // THEN
        assertThat(getIds(previousExecutions)).containsOnly(longs(2, 5));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutionRepositoryIT-findNextOf.xml" })
    void findNextOf() {
        // GIVEN
        List<Long> executionsId = Arrays.asList(Long.valueOf(2), Long.valueOf(5), Long.valueOf(7));

        // WHEN
        List<Execution> nextExecutions = cut.findNextOf(executionsId);

        // THEN
        assertThat(getIds(nextExecutions)).containsOnly(longs(3, 6));
    }

}
