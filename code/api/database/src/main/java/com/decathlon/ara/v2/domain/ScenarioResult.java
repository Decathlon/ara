package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.enumeration.ScenarioResultStatus;
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
@Entity(name = "ara_scenario_result")
public class ScenarioResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scenario_result_seq")
    private Long id;

    private String code;

    @Enumerated(EnumType.STRING)
    private ScenarioResultStatus status;

    private LocalDateTime startDate;

    private String screenshotUrl;
    
    private String videoUrl;

    private String otherDisplayUrl;

    private String targetServer;

    private String diffReportUrl;

    private String scenarioExecutionUrl;

    private String errorStackTraceUrl;

    private String executionTraceUrl;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "scenario_version_commit_sha", referencedColumnName = "commit_sha", updatable = false, insertable = false)
    @JoinColumn(name = "scenario_code", referencedColumnName = "scenario_code", updatable = false, insertable = false)
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    private ScenarioVersion version;

    @OneToMany
    @JoinColumn(name = "scenario_result_id", nullable = false)
    private List<ScenarioStepIncident> stepIncidents = Collections.emptyList();

    @OneToMany
    @JoinColumn(name = "scenario_result_id", nullable = false)
    private List<ScenarioResultError> errors = Collections.emptyList();

}
