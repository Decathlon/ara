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

package com.decathlon.ara.service.util;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectUtilTest {

    @Test
    public void trimStringValues_should_trim_field() {
        // GIVEN
        SimpleClass simpleClass = new SimpleClass();
        simpleClass.setSimpleField("  \t value \t  ");

        // WHEN
        ObjectUtil.trimStringValues(simpleClass);

        // THEN
        assertThat(simpleClass.getSimpleField()).isEqualTo("value");
    }

    @Test
    public void trimStringValues_should_trim_super_fields_too() {
        // GIVEN
        SubClass subClass = new SubClass();
        subClass.setSuperField("  \t superValue \t  ");
        subClass.setSubField("  \t subValue \t  ");

        // WHEN
        ObjectUtil.trimStringValues(subClass);

        // THEN
        assertThat(subClass.getSuperField()).isEqualTo("superValue");
        assertThat(subClass.getSubField()).isEqualTo("subValue");
    }

    @Getter
    @Setter
    private static class SimpleClass {

        private String simpleField;

        private int notAStringField;

    }

    @Getter
    @Setter
    private static class SuperClass {

        private String superField;

    }

    @Getter
    @Setter
    private static class SubClass extends SuperClass {

        private String subField;

    }

}
