package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.*;

@Data
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
}
