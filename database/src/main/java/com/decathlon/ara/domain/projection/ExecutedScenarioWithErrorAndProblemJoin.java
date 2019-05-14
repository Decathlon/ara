package com.decathlon.ara.domain.projection;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

/**
 * A join of {@link ExecutedScenario}, {@link Error} and {@link Problem}: provide a few information about the scenario,
 * with handled (with not reappeared problem) and unhandled (without problem, or with reappeared problem) errors, if
 * any.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Wither
public class ExecutedScenarioWithErrorAndProblemJoin {

    /**
     * The {@link ExecutedScenario#id} of this entity.
     */
    private long id;

    /**
     * The {@link Run#id} in which this scenario is.
     */
    private long runId;

    /**
     * The {@link Severity#code} of this scenario (can be a code not existing in database: it is user-provided).
     */
    private String severity;

    /**
     * The name of this scenario.
     */
    private String name;

    /**
     * Is > 0 if there are any unhandled errors for this scenario.<br>
     * If both {@code unhandledCount} and {@link #handledCount} are > 1, the scenario is considered handled.<br>
     * If both {@code unhandledCount} and {@link #handledCount} are 0, the scenario is successful (it has no error).
     */
    private long unhandledCount;

    /**
     * Is > 0 if there are any handled errors for this scenario.<br>
     * If both {@link #unhandledCount} and {@code handledCount} are > 1, the scenario is considered handled.<br>
     * If both {@link #unhandledCount} and {@code handledCount} are 0, the scenario is successful (it has no error).
     */
    private long handledCount;

}
