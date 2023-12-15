package com.decathlon.ara.service.dto.request;

import java.time.Period;
import java.util.Optional;

public class ExecutedScenarioHistoryDuration {

    private int value;
    private ExecutedScenarioHistoryDurationType type;

    public Optional<Period> getDuration() {
        if (type == null || value < 1) {
            return Optional.empty();
        }
        Period period = switch (type) {
            case DAY -> Period.ofDays(value);
            case WEEK -> Period.ofWeeks(value);
            case MONTH -> Period.ofMonths(value);
            case YEAR -> Period.ofYears(value);
        };
        return Optional.of(period);
    }

    public int getValue() {
        return value;
    }

    public ExecutedScenarioHistoryDurationType getType() {
        return type;
    }


    private enum ExecutedScenarioHistoryDurationType {
        DAY, WEEK, MONTH, YEAR
    }
}
