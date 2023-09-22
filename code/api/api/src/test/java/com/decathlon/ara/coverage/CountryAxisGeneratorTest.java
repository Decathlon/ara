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
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import com.decathlon.ara.util.builder.FunctionalityBuilder;
import com.decathlon.ara.util.factory.CountryFactory;

@ExtendWith(MockitoExtension.class)
class CountryAxisGeneratorTest {

    private static final int A_PROJECT_ID = 42;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryAxisGenerator cut;

    @Test
    void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("countries");
    }

    @Test
    void testGetName() {
        assertThat(cut.getName()).isEqualTo("Countries");
    }

    @Test
    void testGetPoints() {
        // GIVEN
        when(countryRepository.findAllByProjectIdOrderByCode(A_PROJECT_ID)).thenReturn(Arrays.asList(
                CountryFactory.get(0l, 0l, "be", "Belgium"),
                CountryFactory.get(0l, 0l, "cn", "China")));

        // WHEN
        List<AxisPointDTO> points = cut.getPoints(A_PROJECT_ID).toList();

        //THEN
        Assertions.assertEquals(2, points.size());
        Assertions.assertTrue(equals(points.get(0), "be", "BE", "Belgium"));
        Assertions.assertTrue(equals(points.get(1), "cn", "CN", "China"));
    }

    @Test
    void testGetValuePoints_without_countryCodes() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withCountryCodes("").build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    void testGetValuePoints_with_null_countryCodes() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    void testGetValuePoints_with_one_countryCode() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withCountryCodes("cn").build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "cn" });
    }

    @Test
    void testGetValuePoints_with_two_countryCodes() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withCountryCodes("be,cn").build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "be", "cn" });
    }

    private boolean equals(AxisPointDTO result, String id, String name, String tooltip) {
        return Objects.equals(result.getId(), id) && Objects.equals(result.getName(), name) && Objects.equals(result.getTooltip(), tooltip);
    }

}
