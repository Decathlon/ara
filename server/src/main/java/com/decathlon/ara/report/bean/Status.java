package com.decathlon.ara.report.bean;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines all possible statuses provided by cucumber-jvm.
 */
public enum Status {

    PASSED, FAILED, SKIPPED, PENDING, UNDEFINED, MISSING;

    @JsonValue
    public String getJsonValue() {
        return name().toLowerCase();
    }

}
