package com.decathlon.ara.domain.enumeration;

public enum QualityStatus {

    /**
     * Either not all blocking runs did not execute, or they did not produce the needed JSON reports (yet?).
     */
    INCOMPLETE,

    /**
     * The cycle is complete (all runs having the includeInThresholds flag set are status=DONE),
     * and quality thresholds were not met.
     */
    FAILED,

    /**
     * The cycle is complete (all runs having the includeInThresholds flag set are status=DONE),
     * and quality thresholds were met (above error thresholds), but still under warning thresholds.
     */
    WARNING,

    /**
     * The cycle is complete (all runs having the includeInThresholds flag set are status=DONE),
     * and quality thresholds were met (above both error and warning thresholds).
     */
    PASSED;

    /**
     * @return true if the quality is enough to make the build eligible (status is WARNING or PASSED)
     */
    public boolean isAcceptable() {
        return this == WARNING || this == PASSED;
    }

}
