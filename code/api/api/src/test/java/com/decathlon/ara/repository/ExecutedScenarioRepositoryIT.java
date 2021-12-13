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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.api.client.util.Objects;

@SpringBootTest
@TestExecutionListeners({
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@TestPropertySource(properties = {
        "ara.database.target=h2"
})
class ExecutedScenarioRepositoryIT {

    @Autowired
    private ExecutedScenarioRepository cut;

    @Test
    @DatabaseSetup({ "/dbunit/ExecutedScenarioRepositoryIT-findAllErrorCounts.xml" })
    void testFindAllErrorCounts() {
        // GIVEN
        Long runId = 11L;

        // WHEN
        final List<ExecutedScenarioWithErrorAndProblemJoin> errorCounts = cut.findAllErrorAndProblemCounts(Collections.singleton(runId));

        // THEN
        Assertions.assertEquals(3, errorCounts.size());
        Assertions.assertTrue(contains(errorCounts, 111, 11, "medium", "With unidentified error", 1, 0));
        Assertions.assertTrue(contains(errorCounts, 112, 11, "medium", "With identified error", 1, 1));
        Assertions.assertTrue(contains(errorCounts, 113, 11, "sanity-check", "Without error", 0, 0));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ExecutedScenarioRepositoryIT-findAllErrorAndProblemCount_even_closed_ones.xml" })
    void testFindAllErrorAndProblemCount_even_closed_ones() {
        // GIVEN
        Long runId = 11L;
        // WHEN
        List<ExecutedScenarioWithErrorAndProblemJoin> allErrorAndProblemCounts = cut.findAllErrorAndProblemCounts(Collections.singleton(runId));
        // THEN
        Assertions.assertEquals(5, allErrorAndProblemCounts.size());
        Assertions.assertTrue(contains(allErrorAndProblemCounts, 111, 11, "medium", "With unidentified error", 1, 0));
        Assertions.assertTrue(contains(allErrorAndProblemCounts, 112, 11, "medium", "With identified error", 1, 1));
        Assertions.assertTrue(contains(allErrorAndProblemCounts, 113, 11, "sanity-check", "Without error", 0, 0));
        Assertions.assertTrue(contains(allErrorAndProblemCounts, 114, 11, "high", "With identified, closed, error", 0, 1));
        Assertions.assertTrue(contains(allErrorAndProblemCounts, 115, 11, "high", "With identified, closed (with date), error", 0, 1));
    }

    private boolean contains(List<ExecutedScenarioWithErrorAndProblemJoin> results, long id, long runId, String severity, String name, long unhandledCount, long handledCount) {
        for (ExecutedScenarioWithErrorAndProblemJoin result : results) {
            if (equals(result, id, runId, severity, name, unhandledCount, handledCount)) {
                return true;
            }
        }
        return false;
    }

    private boolean equals(ExecutedScenarioWithErrorAndProblemJoin result, long id, long runId, String severity, String name, long unhandledCount, long handledCount) {
        return result.getId() == id && result.getRunId() == runId && Objects.equal(result.getSeverity(), severity)
                && Objects.equal(result.getName(), name) && result.getUnhandledCount() == unhandledCount && result.getHandledCount() == handledCount;
    }

}
