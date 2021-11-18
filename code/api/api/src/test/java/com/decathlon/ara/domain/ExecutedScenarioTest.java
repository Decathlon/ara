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

import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutedScenarioTest {

    @Test
    public void getHandling_ShouldReturnSUCCESS_WhenNoError() {
        // GIVEN
        final ExecutedScenario executedScenarioWithoutError = new ExecutedScenario();

        // WHEN
        final Handling handling = executedScenarioWithoutError.getHandling();

        // THEN
        assertThat(handling).isEqualTo(Handling.SUCCESS);
    }

    @Test
    public void getHandling_ShouldReturnUNHANDLED_WhenOnlyErrorsWithoutProblem() {
        // GIVEN
        final ExecutedScenario executedScenarioWithoutError = new ExecutedScenario();
        executedScenarioWithoutError.addError(new Error().withStepLine(1));
        executedScenarioWithoutError.addError(new Error().withStepLine(2));

        // WHEN
        final Handling handling = executedScenarioWithoutError.getHandling();

        // THEN
        assertThat(handling).isEqualTo(Handling.UNHANDLED);
    }

    @Test
    public void getHandling_ShouldReturnHANDLED_WhenAtLeastOneErrorWithProblem() {
        // GIVEN
        final Error errorWithProblem = new Error().withStepLine(2);
        var problemPattern = new ProblemPattern().withProblem(new Problem().withStatus(ProblemStatus.OPEN));
        errorWithProblem.setProblemOccurrences(Set.of(new ProblemOccurrence(new Error(), problemPattern)));
        final ExecutedScenario executedScenarioWithoutError = new ExecutedScenario();
        executedScenarioWithoutError.addError(new Error().withStepLine(1));
        executedScenarioWithoutError.addError(errorWithProblem);
        executedScenarioWithoutError.addError(new Error().withStepLine(3));

        // WHEN
        final Handling handling = executedScenarioWithoutError.getHandling();

        // THEN
        assertThat(handling).isEqualTo(Handling.HANDLED);
    }

}
