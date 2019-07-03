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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RunTest {

    private static Run run(String countryCode, String typeCode) {
        Run run = new Run();
        run.setCountry(new Country().withCode(countryCode));
        run.setType(new Type().withCode(typeCode));
        return run;
    }

    @Test
    @SuppressWarnings("static-method")
    public void testCompareTo() {
        assertThat(run(null, null).compareTo(run(null, null))).isZero();
        assertThat(run(null, null).compareTo(run("A", null))).isNegative();
        assertThat(run(null, null).compareTo(run(null, "A"))).isNegative();
        assertThat(run("A", null).compareTo(run(null, null))).isPositive();
        assertThat(run(null, "A").compareTo(run(null, null))).isPositive();

        assertThat(run("A", "B").compareTo(run("B", "A"))).isNegative();
        assertThat(run("B", "A").compareTo(run("A", "B"))).isPositive();
        assertThat(run("B", "A").compareTo(run("B", "A"))).isZero();

        assertThat(run("A", "A").compareTo(run("A", "B"))).isNegative();
        assertThat(run("A", "B").compareTo(run("A", "A"))).isPositive();
        assertThat(run("A", "B").compareTo(run("A", "B"))).isZero();
    }

}
