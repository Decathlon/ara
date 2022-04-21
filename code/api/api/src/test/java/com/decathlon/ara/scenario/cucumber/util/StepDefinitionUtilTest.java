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

package com.decathlon.ara.scenario.cucumber.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.decathlon.ara.scenario.cucumber.bean.Argument;

@SuppressWarnings("static-method")
class StepDefinitionUtilTest {

    @Test
    void testSimulateMatchingStepDefinition() {
        String stepName = "A step number 1 that fails with error \"string parameter 1\"";
        Argument[] arguments = { new Argument("1", 14), new Argument("string parameter 1", 39) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^A step number (\\d+) that fails with error \"([^\"]*)\"$");
    }

    @Test
    void testArgumentAtStart() {
        String stepName = "A1";
        Argument[] arguments = { new Argument("1", 1) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^A(\\d+)$");
    }

    @Test
    void testArgumentAtEnd() {
        String stepName = "1A";
        Argument[] arguments = { new Argument("1", 0) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^(\\d+)A$");
    }

    @Test
    void testIntegerArgumentAsString() {
        String stepName = "A\"1\"B";
        Argument[] arguments = { new Argument("1", 2) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^A\"([^\"]*)\"B$");
    }

    @Test
    void testNoArgument() {
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition("abc", new Argument[0]);
        assertThat(simulatedStepDefinition).isEqualTo("^abc$");
    }

    @Test
    void testSpecialCharacters() {
        String original = "Test <([{\\^-=$!|]})?*+.>";
        String replaced = "Test " +
                "\\<" +
                "\\(" +
                "\\[" +
                "\\{" +
                "\\\\" +
                "\\^" +
                "\\-" +
                "\\=" +
                "\\$" +
                "\\!" +
                "\\|" +
                "\\]" +
                "\\}" +
                "\\)" +
                "\\?" +
                "\\*" +
                "\\+" +
                "\\." +
                "\\>";
        assertThat(StepDefinitionUtil.simulateMatchingStepDefinition(original, new Argument[0])).isEqualTo("^" + replaced + "$");
    }

}
