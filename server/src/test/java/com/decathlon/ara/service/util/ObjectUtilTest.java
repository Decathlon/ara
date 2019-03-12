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
