package com.decathlon.ara.domain.enumeration;

/**
 * A problem can be OPEN or CLOSED in database. But if it is CLOSED and the problem reappeared after its closing date,
 * the effective status is REAPPEARED, hiding the CLOSED state until it is reopened.
 */
public enum EffectiveProblemStatus {

    /**
     * The problem resolution is in-progress (and can have an open defect).
     */
    OPEN,

    /**
     * The problem is resolved (and can have a closed defect) and did not reappear after the closing date.
     */
    CLOSED,

    /**
     * The problem is resolved (and can have a closed defect) BUT did reappear after the closing date.
     */
    REAPPEARED

}
