package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
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
@Entity(name = "ara_scenario")
public class Scenario implements Serializable {

    @EmbeddedId
    private CodeWithProjectId id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    @JoinColumn(name = "scenario_type_code", referencedColumnName = "code", updatable = false, insertable = false)
    private ScenarioType type;

    @Column(name = "scenario_type_code", nullable = false, length = 50)
    private String typeCode;

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    @JoinColumn(name = "team_code", referencedColumnName = "code", updatable = false, insertable = false)
    private Team team;

    @Column(name = "team_code", nullable = false, length = 50)
    private String teamCode;

    @ManyToMany
    @JoinTable(
            name = "ara_scenarios_tags",
            joinColumns = {
                    @JoinColumn(name = "project_code", referencedColumnName = "project_code", nullable = false),
                    @JoinColumn(name = "scenario_code", referencedColumnName = "code", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false),
                    @JoinColumn(name = "tag_code", referencedColumnName = "code", nullable = false)
            }
    )
    private List<Tag> tags;

    @OneToMany
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", nullable = false)
    @JoinColumn(name = "scenario_code", referencedColumnName = "code", nullable = false)
    private List<ScenarioVersion> versions = Collections.emptyList();
}
