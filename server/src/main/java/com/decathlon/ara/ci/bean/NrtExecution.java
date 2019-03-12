package com.decathlon.ara.ci.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class NrtExecution {

    /**
     * Country code. Eg. "be"...
     */
    private String country;

    /**
     * Non-regression-test type. Eg. "api", "firefox"...
     */
    private String type;

    /**
     * The build that ran (or is running) to run this test.
     */
    private Build build;

}
