package com.decathlon.ara.domain.enumeration;

import java.util.Arrays;

public enum FunctionalityType {

    FOLDER,
    FUNCTIONALITY;

    public static boolean exists(String value) {
        return Arrays.stream(FunctionalityType.values()).anyMatch(v -> v.name().equals(value));
    }

}
