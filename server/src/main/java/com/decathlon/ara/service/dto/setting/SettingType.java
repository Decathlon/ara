package com.decathlon.ara.service.dto.setting;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SettingType {

    /**
     * A small text field (edited with an &lt;input/&gt;).
     */
    STRING,

    /**
     * A bigger (than STRING), multi-lines text field (edited with a &lt;textarea/&gt;).
     */
    TEXTAREA,

    /**
     * The value is a code present in a list of choices (see the 'options' array in {@link SettingDTO}).
     */
    SELECT,

    /**
     * The value represents a password, and is write-only from the GUI perspective: it can be replaced but never read.
     */
    PASSWORD,

    /**
     * The value is displayed as a check, if true, and edited with a checkbox.
     */
    BOOLEAN,

    /**
     * A number value (edited with a number-specialized &lt;input/&gt;)
     */
    INT;

    @JsonValue
    public String getJsonValue() {
        return name().toLowerCase();
    }

}
