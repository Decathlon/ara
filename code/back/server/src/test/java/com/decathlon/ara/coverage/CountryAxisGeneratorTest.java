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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;

@ExtendWith(MockitoExtension.class)
public class CountryAxisGeneratorTest {

    private static final int A_PROJECT_ID = 42;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryAxisGenerator cut;

    @Test
    public void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("countries");
    }

    @Test
    public void testGetName() {
        assertThat(cut.getName()).isEqualTo("Countries");
    }

    @Test
    public void testGetPoints() {
        // GIVEN
        when(countryRepository.findAllByProjectIdOrderByCode(A_PROJECT_ID)).thenReturn(Arrays.asList(
                new Country().withCode("be").withName("Belgium"),
                new Country().withCode("cn").withName("China")));

        // WHEN / THEN
        assertThat(cut.getPoints(A_PROJECT_ID)).containsExactly(
                new AxisPointDTO("be", "BE", "Belgium"),
                new AxisPointDTO("cn", "CN", "China"));
    }

    @Test
    public void testGetValuePoints_without_countryCodes() {
        // GIVEN
        Functionality functionality = new Functionality().withCountryCodes("");

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    public void testGetValuePoints_with_null_countryCodes() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    public void testGetValuePoints_with_one_countryCode() {
        // GIVEN
        Functionality functionality = new Functionality().withCountryCodes("cn");

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "cn" });
    }

    @Test
    public void testGetValuePoints_with_two_countryCodes() {
        // GIVEN
        Functionality functionality = new Functionality().withCountryCodes("be,cn");

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "be", "cn" });
    }

}
