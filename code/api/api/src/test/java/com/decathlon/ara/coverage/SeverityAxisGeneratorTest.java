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

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import com.decathlon.ara.util.builder.FunctionalityBuilder;

@ExtendWith(MockitoExtension.class)
class SeverityAxisGeneratorTest {

    private static final int ANY_PROJECT_ID = -42;

    @InjectMocks
    private SeverityAxisGenerator cut;

    @Test
    void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("severity");
    }

    @Test
    void testGetName() {
        assertThat(cut.getName()).isEqualTo("Severities");
    }

    @Test
    void testGetPoints() {
        // WHEN
        List<AxisPointDTO> points = cut.getPoints(ANY_PROJECT_ID).toList();

        //THEN
        Assertions.assertEquals(3, points.size());
        Assertions.assertTrue(equals(points.get(0), "HIGH", "High", null));
        Assertions.assertTrue(equals(points.get(1), "MEDIUM", "Medium", null));
        Assertions.assertTrue(equals(points.get(2), "LOW", "Low", null));
    }

    @Test
    void testGetValuePoints_without_severity() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    void testGetValuePoints_with_severity() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withSeverity(FunctionalitySeverity.HIGH).build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "HIGH" });
    }

    private boolean equals(AxisPointDTO result, String id, String name, String tooltip) {
        return Objects.equals(result.getId(), id) && Objects.equals(result.getName(), name) && Objects.equals(result.getTooltip(), tooltip);
    }

}
