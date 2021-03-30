package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_execution_type")
public class ScenarioExecutionType {

    @EmbeddedId
    private CodeWithProjectId id;

    @Column(length = 50)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    @JoinColumn(name = "scenario_type_code", referencedColumnName = "code", updatable = false, insertable = false)
    private ScenarioType scenarioType;
}
