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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;

@ExtendWith(MockitoExtension.class)
public class SeverityAxisGeneratorTest {

    private static final int ANY_PROJECT_ID = -42;

    @InjectMocks
    private SeverityAxisGenerator cut;

    @Test
    public void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("severity");
    }

    @Test
    public void testGetName() {
        assertThat(cut.getName()).isEqualTo("Severities");
    }

    @Test
    public void testGetPoints() {
        // WHEN / THEN
        assertThat(cut.getPoints(ANY_PROJECT_ID)).containsExactly(
                new AxisPointDTO("HIGH", "High", null),
                new AxisPointDTO("MEDIUM", "Medium", null),
                new AxisPointDTO("LOW", "Low", null));
    }

    @Test
    public void testGetValuePoints_without_severity() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    public void testGetValuePoints_with_severity() {
        // GIVEN
        Functionality functionality = new Functionality().withSeverity(FunctionalitySeverity.HIGH);

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "HIGH" });
    }

}
