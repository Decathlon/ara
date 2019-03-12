package com.decathlon.ara.domain;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTest {

    private static Type type(String code) {
        // Check they must have no impact
        String dummyName = Integer.toString(RandomUtils.nextInt());
        boolean dummyIsBrowser = RandomUtils.nextBoolean();
        boolean dummyIsMobile = RandomUtils.nextBoolean();

        return new Type(Long.valueOf(42), 1, code, dummyName, dummyIsBrowser, dummyIsMobile, null);
    }

    @Test
    @SuppressWarnings("static-method")
    public void testCompareTo() {
        assertThat(type(null).compareTo(type(null))).isZero();
        assertThat(type(null).compareTo(type("A"))).isNegative();
        assertThat(type("A").compareTo(type(null))).isPositive();

        assertThat(type("A").compareTo(type("A"))).isZero();
        assertThat(type("A").compareTo(type("B"))).isNegative();
        assertThat(type("B").compareTo(type("A"))).isPositive();

        assertThat(type("A").compareTo(null)).isPositive();
    }

}
