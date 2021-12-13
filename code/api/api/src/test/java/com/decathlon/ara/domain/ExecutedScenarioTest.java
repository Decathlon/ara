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

package com.decathlon.ara.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.util.TestUtil;

class ExecutedScenarioTest {

    @Test
    void getHandling_ShouldReturnSUCCESS_WhenNoError() {
        // GIVEN
        final ExecutedScenario executedScenarioWithoutError = new ExecutedScenario();

        // WHEN
        final Handling handling = executedScenarioWithoutError.getHandling();

        // THEN
        assertThat(handling).isEqualTo(Handling.SUCCESS);
    }

    @Test
    void getHandling_ShouldReturnUNHANDLED_WhenOnlyErrorsWithoutProblem() {
        // GIVEN
        final ExecutedScenario executedScenarioWithoutError = new ExecutedScenario();
        executedScenarioWithoutError.addError(error(1));
        executedScenarioWithoutError.addError(error(2));

        // WHEN
        final Handling handling = executedScenarioWithoutError.getHandling();

        // THEN
        assertThat(handling).isEqualTo(Handling.UNHANDLED);
    }

    @Test
    void getHandling_ShouldReturnHANDLED_WhenAtLeastOneErrorWithProblem() {
        // GIVEN
        final Error errorWithProblem = error(2);
        Problem problem = new Problem();
        problem.setStatus(ProblemStatus.OPEN);
        var problemPattern = new ProblemPattern();
        problemPattern.setProblem(problem);
        ProblemOccurrence problemOccurrence = new ProblemOccurrence(new Error(), problemPattern);
        TestUtil.setField(errorWithProblem, "problemOccurrences", Set.of(problemOccurrence));
        final ExecutedScenario executedScenarioWithoutError = new ExecutedScenario();
        executedScenarioWithoutError.addError(error(1));
        executedScenarioWithoutError.addError(errorWithProblem);
        executedScenarioWithoutError.addError(error(3));

        // WHEN
        final Handling handling = executedScenarioWithoutError.getHandling();

        // THEN
        assertThat(handling).isEqualTo(Handling.HANDLED);
    }

    private Error error(int stepLine) {
        Error error = new Error();
        error.setStepLine(stepLine);
        return error;
    }

}
