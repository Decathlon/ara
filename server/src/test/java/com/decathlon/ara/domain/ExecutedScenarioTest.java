package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import org.junit.Test;

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
        errorWithProblem.addProblemPattern(new ProblemPattern()
                .withProblem(new Problem()
                        .withStatus(ProblemStatus.OPEN)));
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
