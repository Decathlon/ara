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

import com.decathlon.ara.scenario.generic.bean.description.step.GenericExecutedScenarioStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericExecutedScenarioDescriptionTest {

    @Test
    void getStepsContent_returnEmptyString_whenStepsEmpty() {
        // Given
        GenericExecutedScenarioDescription description = new GenericExecutedScenarioDescription().withSteps(null);

        // When

        // Then
        String content = description.getStepsContent();
        assertThat(content).isNotNull().isEmpty();
    }

    @Test
    void getStepsContent_returnContent_whenStepsNotEmpty() {
        // Given
        GenericExecutedScenarioDescription description = new GenericExecutedScenarioDescription()
                .withSteps(
                        List.of(
                                new GenericExecutedScenarioStep()
                                        .withLine(1L)
                                        .withStatus("passed")
                                        .withValue(10L)
                                        .withContent("The first line... And it passed!"),
                                new GenericExecutedScenarioStep()
                                        .withLine(2L)
                                        .withStatus("skipped")
                                        .withValue(null)
                                        .withContent("Another line... This line was skipped!"),
                                new GenericExecutedScenarioStep()
                                        .withLine(3L)
                                        .withStatus("failed")
                                        .withValue(25L)
                                        .withContent("The last line... It failed...")
                        )
                );

        // When

        // Then
        String content = description.getStepsContent();
        assertThat(content).isEqualTo(
                "1:passed:10:The first line... And it passed!\n" +
                        "2:skipped:Another line... This line was skipped!\n" +
                        "3:failed:25:The last line... It failed..."
        );
    }
}
