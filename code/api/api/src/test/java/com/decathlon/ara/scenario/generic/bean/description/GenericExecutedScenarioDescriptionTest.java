/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.scenario.generic.bean.description;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.scenario.generic.bean.description.step.GenericExecutedScenarioStep;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class GenericExecutedScenarioDescriptionTest {

    @Test
    void getStepsContent_returnEmptyString_whenStepsEmpty() {
        // Given
        GenericExecutedScenarioDescription description = new GenericExecutedScenarioDescription();

        // When

        // Then
        String content = description.getStepsContent();
        assertThat(content).isNotNull().isEmpty();
    }

    @Test
    void getStepsContent_returnContent_whenStepsNotEmpty() {
        // Given
        GenericExecutedScenarioDescription description = genericExecutedScenarioDescription(
                List.of(
                        genericExecutedScenarioStep(1L, "passed", 10L, "The first line... And it passed!"),
                        genericExecutedScenarioStep(2L, "skipped", null, "Another line... This line was skipped!"),
                        genericExecutedScenarioStep(3L, "failed", 25L, "The last line... It failed...")));

        // When

        // Then
        String content = description.getStepsContent();
        assertThat(content).isEqualTo(
                "1:passed:10:The first line... And it passed!\n" +
                        "2:skipped:Another line... This line was skipped!\n" +
                        "3:failed:25:The last line... It failed...");

    }

    private GenericExecutedScenarioDescription genericExecutedScenarioDescription(List<GenericExecutedScenarioStep> steps) {
        GenericExecutedScenarioDescription genericExecutedScenarioDescription = new GenericExecutedScenarioDescription();
        TestUtil.setField(genericExecutedScenarioDescription, "steps", steps);
        return genericExecutedScenarioDescription;
    }

    private GenericExecutedScenarioStep genericExecutedScenarioStep(Long line, String status, Long value, String content) {
        GenericExecutedScenarioStep genericExecutedScenarioStep = new GenericExecutedScenarioStep();
        TestUtil.setField(genericExecutedScenarioStep, "line", line);
        TestUtil.setField(genericExecutedScenarioStep, "status", status);
        TestUtil.setField(genericExecutedScenarioStep, "value", value);
        TestUtil.setField(genericExecutedScenarioStep, "content", content);
        return genericExecutedScenarioStep;
    }
}
