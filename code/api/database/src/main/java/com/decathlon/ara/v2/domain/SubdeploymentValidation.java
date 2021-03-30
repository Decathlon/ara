package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_sub_deployment_validation")
public class SubdeploymentValidation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="sub_deployment_validation_seq")
    private Long id;

    @OneToMany
    @OrderBy("startDate DESC")
    @JoinColumn(name = "sub_deployment_validation_id", nullable = false)
    private List<SubdeploymentValidationJob> jobHistory;

    private String comment;

    @ManyToOne(optional = false)
    private DeploymentValidation deploymentValidation;

    @ManyToOne
    @JoinColumn(name = "tag_code", referencedColumnName = "code")
    @JoinColumn(name = "project_code", referencedColumnName = "project_code")
    private Tag tag;

    @ManyToOne(optional = false)
    private ScenarioExecutionType type;

    @OneToMany
    private List<ScenarioResult> scenarioResults = Collections.emptyList();
}
