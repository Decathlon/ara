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

package com.decathlon.ara.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.enumeration.CoverageLevel;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import com.decathlon.ara.util.builder.FunctionalityBuilder;

@ExtendWith(MockitoExtension.class)
class CoverageAxisGeneratorTest {

    private static final int ANY_PROJECT_ID = -42;

    @InjectMocks
    private CoverageAxisGenerator cut;

    @Test
    void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("coverage");
    }

    @Test
    void testGetName() {
        assertThat(cut.getName()).isEqualTo("Coverage level");
    }

    @Test
    void testGetPoints() {
        // WHEN 
        List<AxisPointDTO> points = cut.getPoints(ANY_PROJECT_ID).toList();
        // THEN
        Assertions.assertEquals(6, points.size());
        Assertions.assertTrue(equals(points.get(0), CoverageLevel.COVERED));
        Assertions.assertTrue(equals(points.get(1), CoverageLevel.PARTIALLY_COVERED));
        Assertions.assertTrue(equals(points.get(2), CoverageLevel.IGNORED_COVERAGE));
        Assertions.assertTrue(equals(points.get(3), CoverageLevel.STARTED));
        Assertions.assertTrue(equals(points.get(4), CoverageLevel.NOT_AUTOMATABLE));
        Assertions.assertTrue(equals(points.get(5), CoverageLevel.NOT_COVERED));
    }

    @Test
    void testGetValuePoints_COVERED() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder()
                .withScenarios(Collections.singleton(new Scenario())).build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "COVERED" });
    }

    @Test
    void testGetValuePoints_PARTIALLY_COVERED() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder()
                .withScenarios(new HashSet<>(Arrays.asList(
                        scenario(false, 1),
                        scenario(true, 2)))).build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "PARTIALLY_COVERED" });
    }

    @Test
    void testGetValuePoints_IGNORED_COVERAGE() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder()
                .withScenarios(Collections.singleton(scenario(true, 0))).build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "IGNORED_COVERAGE" });
    }

    @Test
    void testGetValuePoints_STARTED() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withStarted(Boolean.TRUE).build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "STARTED" });
    }

    @Test
    void testGetValuePoints_NOT_AUTOMATABLE() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withNotAutomatable(Boolean.TRUE).build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "NOT_AUTOMATABLE" });
    }

    @Test
    void testGetValuePoints_NOT_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "NOT_COVERED" });
    }

    private Scenario scenario(boolean ignored, int line) {
        Scenario scenario = new Scenario();
        scenario.setIgnored(ignored);
        scenario.setLine(line);
        return scenario;
    }

    private boolean equals(AxisPointDTO result, CoverageLevel coverageLevel) {
        return Objects.equals(result.getId(), coverageLevel.name()) && Objects.equals(result.getName(), coverageLevel.getLabel()) && Objects.equals(result.getTooltip(), coverageLevel.getTooltip());
    }

}
