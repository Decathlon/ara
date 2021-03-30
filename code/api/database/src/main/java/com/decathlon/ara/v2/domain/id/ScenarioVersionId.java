package com.decathlon.ara.v2.domain.id;

import com.decathlon.ara.v2.domain.Scenario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.io.Serializable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ScenarioVersionId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code")
    @JoinColumn(name = "scenario_code", referencedColumnName = "code")
    private Scenario scenario;

    @Column(name = "commit_sha", length = 50)
    private String commitSHA;
}
