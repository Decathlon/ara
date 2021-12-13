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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import com.decathlon.ara.domain.enumeration.CoverageLevel;
import com.decathlon.ara.util.builder.FunctionalityBuilder;

class FunctionalityTest {

    @Test
    void getCoverageLevel_should_return_COVERED() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder()
                .withScenarios(Collections.singleton(new Scenario())).build();

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.COVERED);
    }

    @Test
    void getCoverageLevel_should_return_PARTIALLY_COVERED() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder()
                .withScenarios(new HashSet<>(Arrays.asList(
                        scenario(false, 1),
                        scenario(true, 2)))).build();

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.PARTIALLY_COVERED);
    }

    @Test
    void getCoverageLevel_should_return_IGNORED_COVERAGE() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder()
                .withScenarios(Collections.singleton(scenario(true, 0))).build();

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.IGNORED_COVERAGE);
    }

    @Test
    void getCoverageLevel_should_return_STARTED() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withStarted(Boolean.TRUE).build();

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.STARTED);
    }

    @Test
    void getCoverageLevel_should_return_NOT_AUTOMATABLE() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withNotAutomatable(Boolean.TRUE).build();

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.NOT_AUTOMATABLE);
    }

    @Test
    void getCoverageLevel_should_return_NOT_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.NOT_COVERED);
    }

    private Scenario scenario(boolean ignored, int line) {
        Scenario scenario = new Scenario();
        scenario.setIgnored(ignored);
        scenario.setLine(line);
        return scenario;
    }

}
