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
@Entity(name = "ara_project")
public class Project implements Serializable {

    @Id
    @Column(length = 50)
    private String code;

    @Column(length = 30)
    private String name;

    private String description;

    private boolean enabled = true;

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<ProblemRootCause> problemRootCauses = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<Tag> tags = Collections.emptyList();

    @OneToMany
    @OrderBy("level ASC")
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<ScenarioSeverity> severities = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<Team> teams = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<ScenarioType> scenarioTypes = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<ScenarioExecutionType> scenarioExecutionTypes = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<Feature> features = Collections.emptyList();

    @OneToMany
    @OrderBy("level ASC")
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<FeaturePriority> featurePriorities = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<Repository> repositories = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "project_code", updatable = false, insertable = false)
    private List<DeployedVersion> deployedVersions = Collections.emptyList();
}
