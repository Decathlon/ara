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
import com.decathlon.ara.v2.domain.*;
import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnBlankName_whenNameIsBlank() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName(null);

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnOnlyScenarioName_whenNameDoesNotContainColon() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("Scenario without functionality codes");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario without functionality codes");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnOnlyScenarioName_whenNameContainsColonButNoValidFunctionalityKeyword() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("No correct codes:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodes() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndEmptyFunctionalityCodes_whenNameContainsScenarioNameAndOnlyFunctionalityKeyword() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndOrderedFunctionalityCodes_whenNameContainsScenarioNameAndUnorderedFunctionalityCodes() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 3, 5, 1, 2, 4:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(5).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndDistinctFunctionalityCodes_whenNameContainsScenarioNameAndRepeatedFunctionalityCodes() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 3, 5, 2, 1, 2, 3, 4:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(5).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndOnlyValidNumberFunctionalityCodes_whenNameContainsScenarioNameAndSomeInvalidNumbersFunctionalityCodes() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 3, 5, not_a_number, 1, 2, to be ignored, 4:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(5).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndOrderedFunctionalityCodesAsNumbers_whenNameContainsScenarioNameAndUnorderedFunctionalityCodes() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 13, 212, 1, 3, 50, 313, 5, 27, 45, 2:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(10).containsExactly("1", "2", "3", "5", "13", "27", "45", "50", "212", "313");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesBeginingWithUppercase() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("Functionality 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalitiesCodes() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionalities 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalitiesCodesBeginingWithUppercase() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("Functionalities 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndSomeTextBeforeFunctionalityCodes() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName(" {some description before} -> functionality 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndSeparatorIsAmpersand() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 1 & 2 & 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndSeparatorsAreAmpersandsAndCommas() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 1, 2 & 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndWhitespaceBetweenColon() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 1, 2, 3  :    Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndMoreThan2Colons() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withName("functionality 1, 2, 3:Scenario name:another part");

        // When

        // Then
        var scenarioNameAndFunctionalities = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name:another part");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void matchesScenario_returnFalse_whenFeatureFilesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withFeatureFile("an_execution_feature_file");
        Scenario scenario = new Scenario()
                .withFeatureFile("the_scenario_feature_file");
        Source source = mock(Source.class);

        // When

        // Then
        var executedScenarioMatchesScenario = executedScenario.matchesScenario(scenario, source);
        assertThat(executedScenarioMatchesScenario).isFalse();
    }

    @Test
    public void matchesScenario_returnFalse_whenScenarioNamesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 1, 2, 3 : Some executed scenario name");
        Scenario scenario = new Scenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 1, 2, 3 : Another scenario name");
        Source source = mock(Source.class);

        // When

        // Then
        var executedScenarioMatchesScenario = executedScenario.matchesScenario(scenario, source);
        assertThat(executedScenarioMatchesScenario).isFalse();
    }

    @Test
    public void matchesScenario_returnFalse_whenSourcesAreDifferent() {
        // Given
        Source executedScenarioSource = mock(Source.class);
        Source scenarioSource = mock(Source.class);
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 1, 2, 3 :Same scenario name");
        Scenario scenario = new Scenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 1, 2, 3: Same scenario name")
                .withSource(scenarioSource);

        // When

        // Then
        var executedScenarioMatchesScenario = executedScenario.matchesScenario(scenario, executedScenarioSource);
        assertThat(executedScenarioMatchesScenario).isFalse();
    }

    @Test
    public void matchesScenario_returnTrue_whenExecutedScenarioAndScenarioAreTheSame() {
        // Given
        Source sameSource = mock(Source.class);
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 1, 2, 3 :Same scenario name");
        Scenario scenario = new Scenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 1, 2, 3: Same scenario name")
                .withSource(sameSource);

        // When

        // Then
        var executedScenarioMatchesScenario = executedScenario.matchesScenario(scenario, sameSource);
        assertThat(executedScenarioMatchesScenario).isTrue();
    }

    @Test
    public void matchesScenario_returnTrue_whenExecutedScenarioAndScenarioAreTheSameButFunctionalityCodesAreDifferent() {
        // Given
        Source sameSource = mock(Source.class);
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 1, 2, 3 :Same scenario name");
        Scenario scenario = new Scenario()
                .withFeatureFile("same_feature_file")
                .withName("Functionalities 4, 5, 6: Same scenario name")
                .withSource(sameSource);

        // When

        // Then
        var executedScenarioMatchesScenario = executedScenario.matchesScenario(scenario, sameSource);
        assertThat(executedScenarioMatchesScenario).isTrue();
    }

    @Test
    public void getStatelessScenarioSteps_returnEmptyList_whenContentIsBlank() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withContent(null);

        // When

        // Then
        var steps = executedScenario.getStatelessScenarioSteps();
        assertThat(steps).isNotNull().isEmpty();
    }

    @Test
    public void getStatelessScenarioSteps_returnScenarioSteps_whenContentNotBlank() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getStatelessScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching"),
                        tuple(2, "Calling the data API"),
                        tuple(3, "Successfully retrieving the data")
                );
    }

    @Test
    public void getStatelessScenarioSteps_returnScenarioStepsWithoutWrongLineNumbers_whenContentContainsWrongLineNumbers() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "incorrect_number:skipped:123:Some content\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getStatelessScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching"),
                        tuple(2, "Calling the data API"),
                        tuple(3, "Successfully retrieving the data")
                );
    }

    @Test
    public void getStatelessScenarioSteps_returnFilteredScenarioSteps_whenSomeContentsAreDifferentThan3Or4Blocks() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "21:no_content\n" +
                                "22:skipped:123:Calling the data API:some_additional_content\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getStatelessScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching"),
                        tuple(2, "Calling the data API"),
                        tuple(3, "Successfully retrieving the data")
                );
    }

    @Test
    public void getStatelessScenarioSteps_returnScenarioStepsWithoutWhitespaces_whenContentsContainsWhitespaces() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1 :passed :Preparing for data fetching\n" +
                                "2:skipped: 123 : Calling the data API\n" +
                                "3: failed:  Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getStatelessScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching"),
                        tuple(2, "Calling the data API"),
                        tuple(3, "Successfully retrieving the data")
                );
    }

    @Test
    public void getExecutedScenarioSteps_returnEmptyList_whenContentIsBlank() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario().withContent(null);

        // When

        // Then
        var steps = executedScenario.getExecutedScenarioSteps();
        assertThat(steps).isNotNull().isEmpty();
    }

    @Test
    public void getExecutedScenarioSteps_returnScenarioSteps_whenContentNotBlank() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getExecutedScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content", "value", "state")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching", Optional.empty(), "passed"),
                        tuple(2, "Calling the data API", Optional.of("123"), "skipped"),
                        tuple(3, "Successfully retrieving the data", Optional.empty(), "failed")
                );
    }

    @Test
    public void getExecutedScenarioSteps_returnScenarioStepsWithoutWrongLineNumbers_whenContentContainsWrongLineNumbers() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "incorrect_number:skipped:123:Some content\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getExecutedScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content", "value", "state")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching", Optional.empty(), "passed"),
                        tuple(2, "Calling the data API", Optional.of("123"), "skipped"),
                        tuple(3, "Successfully retrieving the data", Optional.empty(), "failed")
                );
    }

    @Test
    public void getExecutedScenarioSteps_returnFilteredScenarioSteps_whenSomeContentsAreDifferentThan3Or4Blocks() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "21:no_content\n" +
                                "22:skipped:123:Calling the data API:some_additional_content\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getExecutedScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content", "value", "state")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching", Optional.empty(), "passed"),
                        tuple(2, "Calling the data API", Optional.of("123"), "skipped"),
                        tuple(3, "Successfully retrieving the data", Optional.empty(), "failed")
                );
    }

    @Test
    public void getExecutedScenarioSteps_returnScenarioStepsWithoutWhitespaces_whenContentsContainsWhitespaces() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario()
                .withContent(
                        "1 :passed :Preparing for data fetching\n" +
                                "2:skipped: 123 : Calling the data API\n" +
                                "3: failed:  Successfully retrieving the data"
                );

        // When

        // Then
        var steps = executedScenario.getExecutedScenarioSteps();
        assertThat(steps)
                .hasSize(3)
                .extracting("line", "content", "value", "state")
                .containsExactlyInAnyOrder(
                        tuple(1, "Preparing for data fetching", Optional.empty(), "passed"),
                        tuple(2, "Calling the data API", Optional.of("123"), "skipped"),
                        tuple(3, "Successfully retrieving the data", Optional.empty(), "failed")
                );
    }

    @Test
    public void shareTheSameFunctionalityCodesAs_returnFalse_whenTheFunctionalityCodesSizesAreDifferent() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withName("Functionalities 1, 2, 3 : Scenario name 1");
        ExecutedScenario executedScenario2 = new ExecutedScenario().withName("functionality 1,2: Scenario name 2");

        // When

        // Then
        var bothExecutedScenariosShareTheSameFunctionalityCodes = executedScenario1.shareTheSameFunctionalityCodesAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameFunctionalityCodes).isFalse();
    }

    @Test
    public void shareTheSameFunctionalityCodesAs_returnFalse_whenTheFunctionalityCodesAreDifferent() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withName("Functionalities 1, 2, 3 : Scenario name 1");
        ExecutedScenario executedScenario2 = new ExecutedScenario().withName("functionality 1,4,3 : Scenario name 2");

        // When

        // Then
        var bothExecutedScenariosShareTheSameFunctionalityCodes = executedScenario1.shareTheSameFunctionalityCodesAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameFunctionalityCodes).isFalse();
    }

    @Test
    public void shareTheSameFunctionalityCodesAs_returnTrue_whenTheFunctionalityCodesAreTheSame() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withName("Functionalities 1, 2, 3 : Scenario name 1");
        ExecutedScenario executedScenario2 = new ExecutedScenario().withName("functionality 1,2,3 : Scenario name 2");

        // When

        // Then
        var bothExecutedScenariosShareTheSameFunctionalityCodes = executedScenario1.shareTheSameFunctionalityCodesAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameFunctionalityCodes).isTrue();
    }

    @Test
    public void shareTheSameFunctionalityCodesAs_returnTrue_whenTheFunctionalityCodesAreTheSameButNotInTheSameOrder() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withName("Functionalities 2, 3, 1 : Scenario name 1");
        ExecutedScenario executedScenario2 = new ExecutedScenario().withName("functionality 1,2,3 : Scenario name 2");

        // When

        // Then
        var bothExecutedScenariosShareTheSameFunctionalityCodes = executedScenario1.shareTheSameFunctionalityCodesAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameFunctionalityCodes).isTrue();
    }

    @Test
    public void shareTheSameFunctionalityCodesAs_returnTrue_whenTheFunctionalityCodesAreTheSameButThereAreSomeDuplicates() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withName("Functionalities 1, 2, 3 : Scenario name 1");
        ExecutedScenario executedScenario2 = new ExecutedScenario().withName("functionality 1,3,2,2,3,1 : Scenario name 2");

        // When

        // Then
        var bothExecutedScenariosShareTheSameFunctionalityCodes = executedScenario1.shareTheSameFunctionalityCodesAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameFunctionalityCodes).isTrue();
    }

    @Test
    public void shareTheSameStepsAs_returnFalse_whenSomeLineContentsAreDifferent() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                "2:passed:123: Calling the data API\n" +
                "3:passed:Successfully retrieving the data"
        );
        ExecutedScenario executedScenario2 = new ExecutedScenario().withContent(
                "1:passed:This content is different\n" +
                "2:passed:123: Calling the data API\n" +
                "3:passed:Successfully retrieving the data"
        );

        // When

        // Then
        var bothExecutedScenariosShareTheSameContent = executedScenario1.shareTheSameStepsAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameContent).isFalse();
    }

    @Test
    public void shareTheSameStepsAs_returnFalse_whenSomeLinesAreDifferent() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:passed:123: Calling the data API\n" +
                        "3:passed:Successfully retrieving the data"
        );
        ExecutedScenario executedScenario2 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:passed:123: Calling the data API\n" +
                        "13:passed:Successfully retrieving the data"
        );

        // When

        // Then
        var bothExecutedScenariosShareTheSameContent = executedScenario1.shareTheSameStepsAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameContent).isFalse();
    }

    @Test
    public void shareTheSameStepsAs_returnFalse_whenSomeLineStatesAreDifferent() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:passed:123: Calling the data API\n" +
                        "3:passed:Successfully retrieving the data"
        );
        ExecutedScenario executedScenario2 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:failed:123: Calling the data API\n" +
                        "3:passed:Successfully retrieving the data"
        );

        // When

        // Then
        var bothExecutedScenariosShareTheSameContent = executedScenario1.shareTheSameStepsAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameContent).isFalse();
    }

    @Test
    public void shareTheSameStepsAs_returnTrue_whenSomeLineValuesAreDifferent() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withContent(
                "1:passed:1:Preparing for data fetching\n" +
                        "2:passed:2: Calling the data API\n" +
                        "3:passed:Successfully retrieving the data"
        );
        ExecutedScenario executedScenario2 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:passed:3: Calling the data API\n" +
                        "3:passed:2:Successfully retrieving the data"
        );

        // When

        // Then
        var bothExecutedScenariosShareTheSameContent = executedScenario1.shareTheSameStepsAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameContent).isTrue();
    }

    @Test
    public void shareTheSameStepsAs_returnFalse_whenSomeLinesAreNotInTheSameOrder() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:passed:Calling the data API\n" +
                        "3:passed:Successfully retrieving the data"
        );
        ExecutedScenario executedScenario2 = new ExecutedScenario().withContent(
                "2:passed:Calling the data API\n" +
                        "1:passed:Preparing for data fetching\n" +
                        "3:passed:Successfully retrieving the data"
        );

        // When

        // Then
        var bothExecutedScenariosShareTheSameContent = executedScenario1.shareTheSameStepsAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameContent).isFalse();
    }

    @Test
    public void shareTheSameStepsAs_returnFalse_whenSomeLinesAreDuplicates() {
        // Given
        ExecutedScenario executedScenario1 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:passed:123: Calling the data API\n" +
                        "3:passed:Successfully retrieving the data"
        );
        ExecutedScenario executedScenario2 = new ExecutedScenario().withContent(
                "1:passed:Preparing for data fetching\n" +
                        "2:passed:123: Calling the data API\n" +
                        "2:passed:123: Calling the data API\n" +
                        "3:passed:Successfully retrieving the data"
        );

        // When

        // Then
        var bothExecutedScenariosShareTheSameContent = executedScenario1.shareTheSameStepsAs(executedScenario2);
        assertThat(bothExecutedScenariosShareTheSameContent).isFalse();
    }

    @Test
    public void getExtendedExecutedScenario_returnExtendedExecutedScenario() {
        // Given
        ExecutedScenario executedScenario = new ExecutedScenario();
        Source source = mock(Source.class);
        String branchName = "branch-name";

        // When

        // Then
        var result = executedScenario.getExtendedExecutedScenario(branchName, source);
        assertThat(result).isNotNull();
        assertThat(result.getLegacyExecutedScenario()).isEqualTo(executedScenario);
        assertThat(result.getBranchName()).isEqualTo(branchName);
        assertThat(result.getLegacySource()).isEqualTo(source);
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenScenariosNull() {
        // Given
        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario();

        // When

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(null);
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenScenarioNamesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);

        com.decathlon.ara.v2.domain.Scenario scenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario scenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario scenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario);

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Another scenario name", List.of())
        );

        when(scenario1.getName()).thenReturn("Scenario name 1");
        when(scenario2.getName()).thenReturn("Scenario name 2");
        when(scenario3.getName()).thenReturn("Scenario name 3");

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(scenario1, scenario2, scenario3)
        );
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenScenarioTypesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);
        Source source = mock(Source.class);

        com.decathlon.ara.v2.domain.Scenario migrationScenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ScenarioType migrationScenarioType = mock(ScenarioType.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario)
                .withLegacySource(source);

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Scenario name 3", List.of())
        );

        when(source.getCode()).thenReturn("source_code");

        when(migrationScenario1.getName()).thenReturn("Scenario name 1");
        when(migrationScenario2.getName()).thenReturn("Scenario name 2");
        when(migrationScenario3.getName()).thenReturn("Scenario name 3");
        when(migrationScenario3.getType()).thenReturn(migrationScenarioType);
        when(migrationScenarioType.getId()).thenReturn(new CodeWithProjectId().withCode("another_source_code"));

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(migrationScenario1, migrationScenario2, migrationScenario3)
        );
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenAllScenarioVersionFeatureFilesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);
        Source source = mock(Source.class);

        com.decathlon.ara.v2.domain.Scenario migrationScenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ScenarioType migrationScenarioType = mock(ScenarioType.class);

        ScenarioVersion migrationScenarioVersion1 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion2 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion3 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion4 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion5 = mock(ScenarioVersion.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario)
                .withLegacySource(source);

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Scenario name 3", List.of())
        );
        when(executedScenario.getFeatureName()).thenReturn("feature_name");

        when(source.getCode()).thenReturn("source_code");

        when(migrationScenario1.getName()).thenReturn("Scenario name 1");
        when(migrationScenario2.getName()).thenReturn("Scenario name 2");
        when(migrationScenario3.getName()).thenReturn("Scenario name 3");
        when(migrationScenario3.getType()).thenReturn(migrationScenarioType);
        when(migrationScenarioType.getId()).thenReturn(new CodeWithProjectId().withCode("source_code"));
        when(migrationScenario3.getVersions()).thenReturn(
                List.of(migrationScenarioVersion1, migrationScenarioVersion2, migrationScenarioVersion3, migrationScenarioVersion4, migrationScenarioVersion5)
        );
        when(migrationScenarioVersion1.getFileName()).thenReturn("feature_name-1");
        when(migrationScenarioVersion2.getFileName()).thenReturn("feature_name-2");
        when(migrationScenarioVersion3.getFileName()).thenReturn("feature_name-3");
        when(migrationScenarioVersion4.getFileName()).thenReturn("feature_name-4");
        when(migrationScenarioVersion5.getFileName()).thenReturn("feature_name-5");

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(migrationScenario1, migrationScenario2, migrationScenario3)
        );
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenAllScenarioVersionBranchesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);
        Source source = mock(Source.class);

        com.decathlon.ara.v2.domain.Scenario migrationScenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ScenarioType migrationScenarioType = mock(ScenarioType.class);

        ScenarioVersion migrationScenarioVersion1 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion2 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion3 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion4 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion5 = mock(ScenarioVersion.class);

        Branch migrationBranch = mock(Branch.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario)
                .withLegacySource(source)
                .withBranchName("branch_name");

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Scenario name 3", List.of())
        );
        when(executedScenario.getFeatureName()).thenReturn("feature_name-5");

        when(source.getCode()).thenReturn("source_code");

        when(migrationScenario1.getName()).thenReturn("Scenario name 1");
        when(migrationScenario2.getName()).thenReturn("Scenario name 2");
        when(migrationScenario3.getName()).thenReturn("Scenario name 3");
        when(migrationScenario3.getType()).thenReturn(migrationScenarioType);
        when(migrationScenarioType.getId()).thenReturn(new CodeWithProjectId().withCode("source_code"));
        when(migrationScenario3.getVersions()).thenReturn(
                List.of(migrationScenarioVersion1, migrationScenarioVersion2, migrationScenarioVersion3, migrationScenarioVersion4, migrationScenarioVersion5)
        );
        when(migrationBranch.getId()).thenReturn(new CodeWithProjectId().withCode("another_branch_name"));
        when(migrationScenarioVersion1.getFileName()).thenReturn("feature_name-1");
        when(migrationScenarioVersion2.getFileName()).thenReturn("feature_name-2");
        when(migrationScenarioVersion3.getFileName()).thenReturn("feature_name-3");
        when(migrationScenarioVersion4.getFileName()).thenReturn("feature_name-4");
        when(migrationScenarioVersion5.getFileName()).thenReturn("feature_name-5");
        when(migrationScenarioVersion5.getBranch()).thenReturn(migrationBranch);

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(migrationScenario1, migrationScenario2, migrationScenario3)
        );
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenAllScenarioVersionSeveritiesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);
        Source source = mock(Source.class);

        com.decathlon.ara.v2.domain.Scenario migrationScenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ScenarioType migrationScenarioType = mock(ScenarioType.class);

        ScenarioVersion migrationScenarioVersion1 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion2 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion3 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion4 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion5 = mock(ScenarioVersion.class);

        Branch migrationBranch = mock(Branch.class);

        ScenarioSeverity migrationSeverity = mock(ScenarioSeverity.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario)
                .withLegacySource(source)
                .withBranchName("branch_name");

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Scenario name 3", List.of())
        );
        when(executedScenario.getFeatureName()).thenReturn("feature_name-5");
        when(executedScenario.getSeverity()).thenReturn("another-severity");

        when(source.getCode()).thenReturn("source_code");

        when(migrationScenario1.getName()).thenReturn("Scenario name 1");
        when(migrationScenario2.getName()).thenReturn("Scenario name 2");
        when(migrationScenario3.getName()).thenReturn("Scenario name 3");
        when(migrationScenario3.getType()).thenReturn(migrationScenarioType);
        when(migrationScenarioType.getId()).thenReturn(new CodeWithProjectId().withCode("source_code"));
        when(migrationScenario3.getVersions()).thenReturn(
                List.of(migrationScenarioVersion1, migrationScenarioVersion2, migrationScenarioVersion3, migrationScenarioVersion4, migrationScenarioVersion5)
        );
        when(migrationBranch.getId()).thenReturn(new CodeWithProjectId().withCode("branch_name"));
        when(migrationSeverity.getId()).thenReturn(new CodeWithProjectId().withCode("severity"));
        when(migrationScenarioVersion1.getFileName()).thenReturn("feature_name-1");
        when(migrationScenarioVersion2.getFileName()).thenReturn("feature_name-2");
        when(migrationScenarioVersion3.getFileName()).thenReturn("feature_name-3");
        when(migrationScenarioVersion4.getFileName()).thenReturn("feature_name-4");
        when(migrationScenarioVersion5.getFileName()).thenReturn("feature_name-5");
        when(migrationScenarioVersion5.getBranch()).thenReturn(migrationBranch);
        when(migrationScenarioVersion5.getSeverity()).thenReturn(migrationSeverity);

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(migrationScenario1, migrationScenario2, migrationScenario3)
        );
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenAllScenarioVersionFeaturesAreDifferent() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);
        Source source = mock(Source.class);

        com.decathlon.ara.v2.domain.Scenario migrationScenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ScenarioType migrationScenarioType = mock(ScenarioType.class);

        ScenarioVersion migrationScenarioVersion1 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion2 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion3 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion4 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion5 = mock(ScenarioVersion.class);

        Branch migrationBranch = mock(Branch.class);

        ScenarioSeverity migrationSeverity = mock(ScenarioSeverity.class);

        Feature migrationFeature = mock(Feature.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario)
                .withLegacySource(source)
                .withBranchName("branch_name");

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Scenario name 3", List.of("1", "2", "3"))
        );
        when(executedScenario.getFeatureName()).thenReturn("feature_name-5");
        when(executedScenario.getSeverity()).thenReturn("severity");

        when(source.getCode()).thenReturn("source_code");

        when(migrationScenario1.getName()).thenReturn("Scenario name 1");
        when(migrationScenario2.getName()).thenReturn("Scenario name 2");
        when(migrationScenario3.getName()).thenReturn("Scenario name 3");
        when(migrationScenario3.getType()).thenReturn(migrationScenarioType);
        when(migrationScenarioType.getId()).thenReturn(new CodeWithProjectId().withCode("source_code"));
        when(migrationScenario3.getVersions()).thenReturn(
                List.of(migrationScenarioVersion1, migrationScenarioVersion2, migrationScenarioVersion3, migrationScenarioVersion4, migrationScenarioVersion5)
        );
        when(migrationBranch.getId()).thenReturn(new CodeWithProjectId().withCode("branch_name"));
        when(migrationSeverity.getId()).thenReturn(new CodeWithProjectId().withCode("severity"));
        when(migrationFeature.getCode()).thenReturn("4");
        when(migrationScenarioVersion1.getFileName()).thenReturn("feature_name-1");
        when(migrationScenarioVersion2.getFileName()).thenReturn("feature_name-2");
        when(migrationScenarioVersion3.getFileName()).thenReturn("feature_name-3");
        when(migrationScenarioVersion4.getFileName()).thenReturn("feature_name-4");
        when(migrationScenarioVersion5.getFileName()).thenReturn("feature_name-5");
        when(migrationScenarioVersion5.getBranch()).thenReturn(migrationBranch);
        when(migrationScenarioVersion5.getSeverity()).thenReturn(migrationSeverity);
        when(migrationScenarioVersion5.getCoveredFeatures()).thenReturn(List.of(migrationFeature));

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(migrationScenario1, migrationScenario2, migrationScenario3)
        );
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnEmptyOptional_whenAllScenarioVersionStepsAreDifferent() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);
        Source source = mock(Source.class);

        com.decathlon.ara.v2.domain.Scenario migrationScenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ScenarioType migrationScenarioType = mock(ScenarioType.class);

        ScenarioVersion migrationScenarioVersion1 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion2 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion3 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion4 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion5 = mock(ScenarioVersion.class);

        Branch migrationBranch = mock(Branch.class);

        ScenarioSeverity migrationSeverity = mock(ScenarioSeverity.class);

        Feature migrationFeature1 = mock(Feature.class);
        Feature migrationFeature2 = mock(Feature.class);
        Feature migrationFeature3 = mock(Feature.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario)
                .withLegacySource(source)
                .withBranchName("branch_name");

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Scenario name 3", List.of("1", "2", "3"))
        );
        when(executedScenario.getFeatureName()).thenReturn("feature_name-5");
        when(executedScenario.getSeverity()).thenReturn("severity");
        when(executedScenario.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Go to login page"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Fill in the username and password"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(3, "Press the login button"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(4, "Click on user profile"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(5, "Logout")
        ));

        when(source.getCode()).thenReturn("source_code");

        when(migrationScenario1.getName()).thenReturn("Scenario name 1");
        when(migrationScenario2.getName()).thenReturn("Scenario name 2");
        when(migrationScenario3.getName()).thenReturn("Scenario name 3");
        when(migrationScenario3.getType()).thenReturn(migrationScenarioType);
        when(migrationScenarioType.getId()).thenReturn(new CodeWithProjectId().withCode("source_code"));
        when(migrationScenario3.getVersions()).thenReturn(
                List.of(migrationScenarioVersion1, migrationScenarioVersion2, migrationScenarioVersion3, migrationScenarioVersion4, migrationScenarioVersion5)
        );
        when(migrationBranch.getId()).thenReturn(new CodeWithProjectId().withCode("branch_name"));
        when(migrationSeverity.getId()).thenReturn(new CodeWithProjectId().withCode("severity"));
        when(migrationFeature1.getCode()).thenReturn("1");
        when(migrationFeature2.getCode()).thenReturn("2");
        when(migrationFeature3.getCode()).thenReturn("3");
        when(migrationScenarioVersion1.getFileName()).thenReturn("feature_name-1");
        when(migrationScenarioVersion2.getFileName()).thenReturn("feature_name-2");
        when(migrationScenarioVersion3.getFileName()).thenReturn("feature_name-3");
        when(migrationScenarioVersion4.getFileName()).thenReturn("feature_name-4");
        when(migrationScenarioVersion5.getFileName()).thenReturn("feature_name-5");
        when(migrationScenarioVersion5.getBranch()).thenReturn(migrationBranch);
        when(migrationScenarioVersion5.getSeverity()).thenReturn(migrationSeverity);
        when(migrationScenarioVersion5.getCoveredFeatures()).thenReturn(
                List.of(migrationFeature1, migrationFeature2, migrationFeature3)
        );
        when(migrationScenarioVersion5.getSteps()).thenReturn(List.of(
                new ScenarioStep().withLine(1).withContent("Go to login page"),
                new ScenarioStep().withLine(2).withContent("Fill in the username and password"),
                new ScenarioStep().withLine(4).withContent("Click on user profile"),
                new ScenarioStep().withLine(3).withContent("Press the login button"),
                new ScenarioStep().withLine(5).withContent("Logout")
        ));

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(migrationScenario1, migrationScenario2, migrationScenario3)
        );
        assertThat(version).isNotNull().isEmpty();
    }

    @Test
    public void getMatchingMigrationScenarioVersion_returnScenarioVersion_whenAScenarioVersionMatchesExecutedScenario() {
        // Given
        ExecutedScenario executedScenario = mock(ExecutedScenario.class);
        Source source = mock(Source.class);

        com.decathlon.ara.v2.domain.Scenario migrationScenario1 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario2 = mock(com.decathlon.ara.v2.domain.Scenario.class);
        com.decathlon.ara.v2.domain.Scenario migrationScenario3 = mock(com.decathlon.ara.v2.domain.Scenario.class);

        ScenarioType migrationScenarioType = mock(ScenarioType.class);

        ScenarioVersion migrationScenarioVersion1 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion2 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion3 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion4 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion5 = mock(ScenarioVersion.class);

        Branch migrationBranch = mock(Branch.class);

        ScenarioSeverity migrationSeverity = mock(ScenarioSeverity.class);

        Feature migrationFeature1 = mock(Feature.class);
        Feature migrationFeature2 = mock(Feature.class);
        Feature migrationFeature3 = mock(Feature.class);

        ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario = new ExecutedScenario.ExtendedExecutedScenario()
                .withLegacyExecutedScenario(executedScenario)
                .withLegacySource(source)
                .withBranchName("branch_name");

        // When
        when(executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(
                Pair.of("Scenario name 3", List.of("2", "3", "1", "1", "2", "3"))
        );
        when(executedScenario.getFeatureName()).thenReturn("feature_name-5");
        when(executedScenario.getSeverity()).thenReturn("severity");
        when(executedScenario.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Go to login page"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Fill in the username and password"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(3, "Press the login button"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(4, "Click on user profile"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(5, "Logout")
        ));

        when(source.getCode()).thenReturn("source_code");

        when(migrationScenario1.getName()).thenReturn("Scenario name 1");
        when(migrationScenario2.getName()).thenReturn("Scenario name 2");
        when(migrationScenario3.getName()).thenReturn("Scenario name 3");
        when(migrationScenario3.getType()).thenReturn(migrationScenarioType);
        when(migrationScenarioType.getId()).thenReturn(new CodeWithProjectId().withCode("source_code"));
        when(migrationScenario3.getVersions()).thenReturn(
                List.of(migrationScenarioVersion1, migrationScenarioVersion2, migrationScenarioVersion3, migrationScenarioVersion4, migrationScenarioVersion5)
        );
        when(migrationBranch.getId()).thenReturn(new CodeWithProjectId().withCode("branch_name"));
        when(migrationSeverity.getId()).thenReturn(new CodeWithProjectId().withCode("severity"));
        when(migrationFeature1.getCode()).thenReturn("1");
        when(migrationFeature2.getCode()).thenReturn("2");
        when(migrationFeature3.getCode()).thenReturn("3");
        when(migrationScenarioVersion1.getFileName()).thenReturn("feature_name-1");
        when(migrationScenarioVersion2.getFileName()).thenReturn("feature_name-2");
        when(migrationScenarioVersion3.getFileName()).thenReturn("feature_name-3");
        when(migrationScenarioVersion4.getFileName()).thenReturn("feature_name-4");
        when(migrationScenarioVersion5.getFileName()).thenReturn("feature_name-5");
        when(migrationScenarioVersion5.getBranch()).thenReturn(migrationBranch);
        when(migrationScenarioVersion5.getSeverity()).thenReturn(migrationSeverity);
        when(migrationScenarioVersion5.getCoveredFeatures()).thenReturn(
                List.of(migrationFeature3, migrationFeature2, migrationFeature1, migrationFeature1, migrationFeature2, migrationFeature3)
        );
        when(migrationScenarioVersion5.getSteps()).thenReturn(List.of(
                new ScenarioStep().withLine(1).withContent("Go to login page"),
                new ScenarioStep().withLine(2).withContent("Fill in the username and password"),
                new ScenarioStep().withLine(3).withContent("Press the login button"),
                new ScenarioStep().withLine(4).withContent("Click on user profile"),
                new ScenarioStep().withLine(5).withContent("Logout")
        ));

        // Then
        Optional<ScenarioVersion> version = extendedExecutedScenario.getMatchingMigrationScenarioVersion(
                List.of(migrationScenario1, migrationScenario2, migrationScenario3)
        );
        assertThat(version).hasValue(migrationScenarioVersion5);
    }
}
