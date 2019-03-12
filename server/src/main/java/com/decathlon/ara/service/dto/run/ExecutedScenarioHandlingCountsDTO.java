package com.decathlon.ara.service.dto.run;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.enumeration.Handling;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

/**
 * Holds a number of {@link ExecutedScenario} + the count of these scenarios in each {@link Handling} state.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ExecutedScenarioHandlingCountsDTO {

    /**
     * The number of {@link ExecutedScenario} entities whose {@link ExecutedScenario#getHandling() getHandling()} is
     * {@link Handling#SUCCESS}.
     */
    private int passed;

    /**
     * The number of {@link ExecutedScenario} entities whose {@link ExecutedScenario#getHandling() getHandling()} is
     * {@link Handling#UNHANDLED}.
     */
    private int unhandled;

    /**
     * The number of {@link ExecutedScenario} entities whose {@link ExecutedScenario#getHandling() getHandling()} is
     * {@link Handling#HANDLED}.
     */
    private int handled;

    /**
     * The total number of {@link ExecutedScenario} entities counted by this object.
     *
     * @return the sum of {@link #passed} + {@link #unhandled} + {@link #handled}.
     */
    public int getTotal() {
        return passed + unhandled + handled;
    }

    /**
     * Add all counts from the {@code other} object to this object.
     *
     * @param other the other object to addition to this object
     */
    public void add(ExecutedScenarioHandlingCountsDTO other) {
        setPassed(getPassed() + other.getPassed());
        setUnhandled(getUnhandled() + other.getUnhandled());
        setHandled(getHandled() + other.getHandled());
    }

}
