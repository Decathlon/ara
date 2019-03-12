package com.decathlon.ara.postman.bean;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines all possible types of an event.
 */
public enum Listen {

    /**
     * Script executed before a request.
     */
    PRE_REQUEST,

    /**
     * Script executed after a request, containing tests with assertions.
     */
    TEST;

    @JsonValue
    public String getJsonValue() {
        return name().replace("_", "").toLowerCase();
    }

}
