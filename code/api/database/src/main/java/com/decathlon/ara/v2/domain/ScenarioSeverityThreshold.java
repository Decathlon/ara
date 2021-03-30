package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Optional;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_severity_threshold")
public class ScenarioSeverityThreshold implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "severity_threshold_seq")
    private Long id;

    @Min(0)
    @Max(100)
    @Column(precision = 3)
    private int warning;

    @Min(0)
    @Max(100)
    @Column(precision = 3)
    private int failure;

    @OneToOne(optional = false)
    @JoinColumn(name = "scenario_severity_code", referencedColumnName = "code")
    @JoinColumn(name = "project_code", referencedColumnName = "project_code")
    private ScenarioSeverity severity;

    @ManyToOne
    private DeploymentValidation deploymentValidation;

    @ManyToOne(optional = false)
    private Cycle cycle;

    public Optional<DeploymentValidation> getDeploymentValidation() {
        return Optional.ofNullable(deploymentValidation);
    }

    public boolean isCycleSeverityThreshold() {
        return deploymentValidation == null;
    }

    public boolean isDeploymentValidationSeverityThreshold() {
        return !isCycleSeverityThreshold();
    }
}
