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

package com.decathlon.ara.service.dto.ignore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ScenarioIgnoreCountDTOTest {

    @Test
    void getPercent_returns_0_when_no_total() {
        // GIVEN
        ScenarioIgnoreCountDTO count = scenarioIgnoreCountDTO(0, 0);

        // WHEN / THEN
        assertThat(count.getPercent()).isEqualTo(0);
    }

    @Test
    void getPercent_returns_correct_percentage() {
        // GIVEN
        ScenarioIgnoreCountDTO count = scenarioIgnoreCountDTO(1, 4);

        // WHEN / THEN
        assertThat(count.getPercent()).isEqualTo(25);
    }

    @Test
    void getPercent_returns_ceiled_percentage() {
        // GIVEN
        ScenarioIgnoreCountDTO count = scenarioIgnoreCountDTO(991, 1000);

        // WHEN / THEN
        assertThat(count.getPercent()).isEqualTo(100);
    }

    private ScenarioIgnoreCountDTO scenarioIgnoreCountDTO(long ignored, long total) {
        ScenarioIgnoreCountDTO scenarioIgnoreCountDTO = new ScenarioIgnoreCountDTO();
        scenarioIgnoreCountDTO.setIgnored(ignored);
        scenarioIgnoreCountDTO.setTotal(total);
        return scenarioIgnoreCountDTO;
    }

}
