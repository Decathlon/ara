package com.decathlon.ara.domain.enumeration;

/**
 * Filter by the effective status of problems, optionally with a combination of statuses.
 *
 * @see EffectiveProblemStatus EffectiveProblemStatus for values: ProblemStatusFilter contains all
 * EffectiveProblemStatus plus combinations of it
 */
public enum ProblemStatusFilter {

    /**
     * The problem resolution is in-progress (and can have an open defect). It is the same as either
     * {@link EffectiveProblemStatus#OPEN} or {@link ProblemStatus#OPEN}.
     */
    OPEN,

    /**
     * The problem is resolved (and can have a closed defect) and did not reappear after the closing date. It is the
     * same as {@link EffectiveProblemStatus#CLOSED} but _NOT_ as {@link ProblemStatus#CLOSED}.
     */
    CLOSED,

    /**
     * The problem is resolved (and can have a closed defect) BUT did reappear after the closing date.
     */
    REAPPEARED,

    /**
     * The problem is in-progress (open), or is resolved (and can have a closed defect) BUT did reappear after the
     * closing date. This is a combination of {@link EffectiveProblemStatus#OPEN} and
     * {@link EffectiveProblemStatus#REAPPEARED}, to show problems to watch (in-progress or to reopen).
     */
    OPEN_OR_REAPPEARED,

    /**
     * The problem is resolved (and can have a closed defect), with OR without having reappeared after the closing date.
     * This is a combination of {@link EffectiveProblemStatus#CLOSED} and {@link EffectiveProblemStatus#REAPPEARED}, or,
     * put simply, this is exactly {@link ProblemStatus#CLOSED}.
     */
    CLOSED_OR_REAPPEARED

}
