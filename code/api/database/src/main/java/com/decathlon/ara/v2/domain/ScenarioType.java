package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_type")
public class ScenarioType {

    @EmbeddedId
    private CodeWithProjectId id;

    @Column(length = 50)
    private String name;

    private String description;

    @Column(nullable = false)
    private String technology;
}
