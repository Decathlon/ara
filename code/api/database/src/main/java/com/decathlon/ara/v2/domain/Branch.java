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
@Entity(name = "ara_branch")
public class Branch implements Serializable {

    @EmbeddedId
    private CodeWithProjectId id;

    @Column(length = 50)
    private String name;

    @Column(nullable = false)
    private int position = 1;

    private String description;

    private String environmentName;

    @OneToMany(mappedBy = "branch")
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", nullable = false)
    @JoinColumn(name = "branch_code", referencedColumnName = "code", nullable = false)
    private List<Cycle> cycles = Collections.emptyList();

    @ManyToMany
    @JoinTable(
            name = "ara_branches_scenarios",
            joinColumns = {
                    @JoinColumn(name = "project_code", referencedColumnName = "project_code", nullable = false),
                    @JoinColumn(name = "branch_code", referencedColumnName = "code", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false),
                    @JoinColumn(name = "scenario_code", referencedColumnName = "code", nullable = false)
            }
    )
    private List<Scenario> scenarios = Collections.emptyList();
}
