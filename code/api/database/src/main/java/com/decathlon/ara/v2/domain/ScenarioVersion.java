package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.id.ScenarioVersionId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_version")
public class ScenarioVersion {

    @EmbeddedId
    private ScenarioVersionId id;

    private LocalDateTime updateDateTime;

    private String fileName;

    private String fileUrl;

    private boolean ignored;

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    @JoinColumn(name = "scenario_severity_code", referencedColumnName = "code", updatable = false, insertable = false)
    private ScenarioSeverity severity;

    @Column(name = "scenario_severity_code", length = 50)
    private String severityCode;

    @OneToMany
    @OrderBy("line ASC")
    @JoinColumn(name = "scenario_version_commit_sha", referencedColumnName = "commit_sha")
    @JoinColumn(name = "scenario_code", referencedColumnName = "scenario_code")
    @JoinColumn(name = "project_code", referencedColumnName = "project_code")
     private List<ScenarioStep> steps = Collections.emptyList();

    @ManyToMany
    @JoinTable(
            name = "ara_covered_versioned_features",
            joinColumns = {
                    @JoinColumn(name = "scenario_version_id", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "feature_id", nullable = false)
            }
    )
    private List<Feature> coveredFeatures = Collections.emptyList();

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code")
    @JoinColumn(name = "branch_code", referencedColumnName = "code")
    private Branch branch;
}
