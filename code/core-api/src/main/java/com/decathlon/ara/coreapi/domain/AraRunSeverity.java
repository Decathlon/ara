package com.decathlon.ara.coreapi.domain;

import javax.persistence.*;

@Entity
public class AraRunSeverity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_run_severity_id")
    @SequenceGenerator(name = "ara_run_severity_id", sequenceName = "ara_run_severity_id", allocationSize = 1)
    private Long id;

    @ManyToOne
    private AraDefaultSeverity defaultSeverity;

    private double warningThreshold;
    private double failureThreshold;

    protected Long getId() {
        return id;
    }

    protected AraDefaultSeverity getDefaultSeverity() {
        return defaultSeverity;
    }

    protected double getWarningThreshold() {
        return warningThreshold;
    }

    protected double getFailureThreshold() {
        return failureThreshold;
    }
}
