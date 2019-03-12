package com.decathlon.ara.domain.enumeration;

/**
 * The handling state of a scenario: if at least one erroneous step has one open or closed (but not reappeared) problem,
 * the errors are handled. When an action is needed to attain this state, the scenarion is unhandled. Scenarios without
 * errors are, of course, successes.
 */
public enum Handling {

    /**
     * The scenario has no error.<br>
     * No need to do any action on the scenario.
     */
    SUCCESS,

    /**
     * The scenario has at least one error with an assigned problem that is open, or is closed but not reappeared.<br>
     * No need to do any further action: the errors are already handled by in-progress problems and defects.
     */
    HANDLED,

    /**
     * The scenario has only errors without any assigned problem, or all problems reappeared after closing.<br>
     * An action is required: either append a problem to one of the scenario's error, or reopen one
     * closed-but-reappeared problem/defect.
     */
    UNHANDLED

}
