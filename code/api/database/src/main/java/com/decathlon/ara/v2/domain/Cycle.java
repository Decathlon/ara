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
@Entity(name = "ara_cycle")
public class Cycle implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="cycle_seq")
    private Long id;

    @Column(length = 50)
    private String name;

    @Column(nullable = false)
    private int position = 1;

    private String description;

    @OneToMany
    @JoinColumn(name = "cycle_id")
    private List<JobPlatform> jobPlatforms = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "cycle_id", nullable = true)
    private List<ScenarioSeverityThreshold> defaultScenarioSeverityThresholds = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "cycle_id", nullable = false)
    private List<DeploymentValidation> deploymentValidation = Collections.emptyList();

    @ManyToOne
    private Branch branch;
}
