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

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

public class CountryTest {

    private static Country country(String code) {
        String dummyName = Integer.toString(RandomUtils.nextInt()); // Check it must have no impact
        return new Country(Long.valueOf(42), 1, code, dummyName);
    }

    @Test
    @SuppressWarnings("static-method")
    public void testCompareTo() {
        assertThat(country(null).compareTo(country(null))).isZero();
        assertThat(country(null).compareTo(country("A"))).isNegative();
        assertThat(country("A").compareTo(country(null))).isPositive();

        assertThat(country("A").compareTo(country("A"))).isZero();
        assertThat(country("A").compareTo(country("B"))).isNegative();
        assertThat(country("B").compareTo(country("A"))).isPositive();

        assertThat(country("A").compareTo(null)).isPositive();
    }

}
