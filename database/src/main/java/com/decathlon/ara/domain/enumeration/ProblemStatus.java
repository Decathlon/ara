package com.decathlon.ara.domain.enumeration;

/**
 * The status of a problem, as stored on database and acted by users.
 */
public enum ProblemStatus {

    /**
     * The problem resolution is in-progress (and can have an open defect).
     */
    OPEN,

    /**
     * The problem is resolved (and can have a closed defect).<br>
     * It may or may not have reappeared after the closing date: see {@link EffectiveProblemStatus}.
     */
    CLOSED

}
