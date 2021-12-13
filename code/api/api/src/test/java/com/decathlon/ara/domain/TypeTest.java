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

import com.decathlon.ara.util.factory.TypeFactory;

class TypeTest {

    private static Type type(String code) {
        // Check they must have no impact
        String dummyName = Integer.toString(RandomUtils.nextInt());
        boolean dummyIsBrowser = RandomUtils.nextBoolean();
        boolean dummyIsMobile = RandomUtils.nextBoolean();

        return TypeFactory.get(Long.valueOf(42), 1, code, dummyName, dummyIsBrowser, dummyIsMobile, null);
    }

    @Test
    void testCompareTo() {
        assertThat(type(null).compareTo(type(null))).isZero();
        assertThat(type(null).compareTo(type("A"))).isNegative();
        assertThat(type("A").compareTo(type(null))).isPositive();

        assertThat(type("A").compareTo(type("A"))).isZero();
        assertThat(type("A").compareTo(type("B"))).isNegative();
        assertThat(type("B").compareTo(type("A"))).isPositive();

        assertThat(type("A").compareTo(null)).isPositive();
    }

}
