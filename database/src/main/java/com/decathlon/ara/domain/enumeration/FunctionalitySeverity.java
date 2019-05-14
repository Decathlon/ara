package com.decathlon.ara.domain.enumeration;

import java.util.Arrays;

public enum FunctionalitySeverity {

    LOW,
    MEDIUM,
    HIGH;

    public static boolean exists(String value) {
        return Arrays.stream(FunctionalitySeverity.values()).anyMatch(v -> v.name().equals(value));
    }

}
