package com.decathlon.ara.domain.enumeration;

import lombok.Getter;

@Getter
public enum CoverageLevel {

    COVERED("Covered (no ignored)", "Functionalities having active scenarios, none of them are ignored"),
    PARTIALLY_COVERED("Partially covered (few ignored)", "Functionalities having active scenarios as well as ignored scenarios"),
    IGNORED_COVERAGE("Ignored coverage (all ignored)", "Functionalities having ignored scenarios and no active scenarios"),
    STARTED("Started", "Functionalities marked as 'Started' without any active nor ignored scenario"),
    NOT_AUTOMATABLE("Not automatable", "Functionalities marked as 'Not automatable' without any active nor ignored scenario"),
    NOT_COVERED("Not covered", "Functionalities that are not started nor not-automatable and have no active nor ignored scenarios");

    private final String label;
    private final String tooltip;

    CoverageLevel(String label, String tooltip) {
        this.label = label;
        this.tooltip = tooltip;
    }

}
