package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.enumeration.ScenarioStepIncidentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_step_incident")
public class ScenarioStepIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scenario_step_incident_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ScenarioStepIncidentType type;

    @ManyToOne(optional = false)
    private ScenarioStep step;
}
