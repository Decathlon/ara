package com.decathlon.ara.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(MockitoExtension.class)
public class ScenarioTest {

    @Test
    public void getScenarioSteps_returnEmptyList_whenContentIsBlank() {
        // Given
        Scenario scenario = new Scenario().withContent(null);

        // When

        // Then
        var steps = scenario.getScenarioSteps();
        assertThat(steps).isNotNull().isEmpty();
    }

    @Test
    public void getScenarioSteps_returnScenarioSteps_whenContentNotBlank() {
        // Given
        Scenario scenario = new Scenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = scenario.getScenarioSteps();
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
    public void getScenarioSteps_returnScenarioStepsWithoutWrongLineNumbers_whenContentContainsWrongLineNumbers() {
        // Given
        Scenario scenario = new Scenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "incorrect_number:skipped:123:Some content\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = scenario.getScenarioSteps();
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
    public void getScenarioSteps_returnFilteredScenarioSteps_whenSomeContentsAreDifferentThan3Or4Blocks() {
        // Given
        Scenario scenario = new Scenario()
                .withContent(
                        "1:passed:Preparing for data fetching\n" +
                                "2:skipped:123:Calling the data API\n" +
                                "21:no_content\n" +
                                "22:skipped:123:Calling the data API:some_additional_content\n" +
                                "3:failed:Successfully retrieving the data"
                );

        // When

        // Then
        var steps = scenario.getScenarioSteps();
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
    public void getScenarioSteps_returnScenarioStepsWithoutWhitespaces_whenContentsContainsWhitespaces() {
        // Given
        Scenario scenario = new Scenario()
                .withContent(
                        "1 :passed :Preparing for data fetching\n" +
                                "2:skipped: 123 : Calling the data API\n" +
                                "3: failed:  Successfully retrieving the data"
                );

        // When

        // Then
        var steps = scenario.getScenarioSteps();
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
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnBlankName_whenNameIsBlank() {
        // Given
        Scenario scenario = new Scenario().withName(null);

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnOnlyScenarioName_whenNameDoesNotContainColon() {
        // Given
        Scenario scenario = new Scenario().withName("Scenario without functionality codes");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario without functionality codes");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnOnlyScenarioName_whenNameContainsColonButNoValidFunctionalityKeyword() {
        // Given
        Scenario scenario = new Scenario().withName("No correct codes:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodes() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndEmptyFunctionalityCodes_whenNameContainsScenarioNameAndOnlyFunctionalityKeyword() {
        // Given
        Scenario scenario = new Scenario().withName("functionality:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).isNotNull().isEmpty();
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndOrderedFunctionalityCodes_whenNameContainsScenarioNameAndUnorderedFunctionalityCodes() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 3, 5, 1, 2, 4:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(5).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndDistinctFunctionalityCodes_whenNameContainsScenarioNameAndRepeatedFunctionalityCodes() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 3, 5, 2, 1, 2, 3, 4:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(5).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndOnlyValidNumberFunctionalityCodes_whenNameContainsScenarioNameAndSomeInvalidNumbersFunctionalityCodes() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 3, 5, not_a_number, 1, 2, to be ignored, 4:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(5).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndOrderedFunctionalityCodesAsNumbers_whenNameContainsScenarioNameAndUnorderedFunctionalityCodes() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 13, 212, 1, 3, 50, 313, 5, 27, 45, 2:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(10).containsExactly("1", "2", "3", "5", "13", "27", "45", "50", "212", "313");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesBeginingWithUppercase() {
        // Given
        Scenario scenario = new Scenario().withName("Functionality 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalitiesCodes() {
        // Given
        Scenario scenario = new Scenario().withName("functionalities 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalitiesCodesBeginingWithUppercase() {
        // Given
        Scenario scenario = new Scenario().withName("Functionalities 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndSomeTextBeforeFunctionalityCodes() {
        // Given
        Scenario scenario = new Scenario().withName(" {some description before} -> functionality 1, 2, 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndSeparatorIsAmpersand() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 1 & 2 & 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndSeparatorsAreAmpersandsAndCommas() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 1, 2 & 3:Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndWhitespaceBetweenColon() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 1, 2, 3  :    Scenario name");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }

    @Test
    public void getNameWithoutCodesAndFunctionalityCodesFromScenarioName_returnScenarioNameAndFunctionalityCodes_whenNameContainsScenarioNameAndFunctionalityCodesAndMoreThan2Colons() {
        // Given
        Scenario scenario = new Scenario().withName("functionality 1, 2, 3:Scenario name:another part");

        // When

        // Then
        var scenarioNameAndFunctionalities = scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
        assertThat(scenarioNameAndFunctionalities).isNotNull();

        var scenarioName = scenarioNameAndFunctionalities.getFirst();
        assertThat(scenarioName).isEqualTo("Scenario name:another part");

        var functionalityCodes = scenarioNameAndFunctionalities.getSecond();
        assertThat(functionalityCodes).hasSize(3).containsExactly("1", "2", "3");
    }
}

