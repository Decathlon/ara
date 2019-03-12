package com.decathlon.ara.domain;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
