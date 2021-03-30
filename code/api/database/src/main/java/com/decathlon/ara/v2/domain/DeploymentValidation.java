package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.enumeration.DeploymentValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_deployment_validation")
public class DeploymentValidation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="deployment_validation_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentValidationStatus status = DeploymentValidationStatus.CREATED;

    private String discardReason;

    @OneToMany
    @OrderBy("startDate DESC")
    @JoinColumn(name = "deployment_validation_id", nullable = false)
    private List<DeploymentValidationJob> jobHistory;

    private LocalDateTime startDateTime;

    private LocalDateTime updateDateTime;

    private Version version;

    private String comment;

    @ManyToOne
    private Cycle cycle;

    @OneToMany(mappedBy = "deploymentValidation")
    @JoinColumn(name = "deployment_validation_id", nullable = false)
    private List<SubdeploymentValidation> subdeploymentValidations = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "deployment_validation_id", nullable = true)
    private List<ScenarioSeverityThreshold> overriddenScenarioSeverityThresholds = Collections.emptyList();

    public Optional<String> getDiscardReason() {
        return Optional.ofNullable(discardReason);
    }

    @Data
    @With
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Version implements Serializable {

        @Column(name = "version_creation_date_time")
        private LocalDateTime creationDateTime;

        private String release;

        @Column(name = "version")
        private String value;
    }
}
