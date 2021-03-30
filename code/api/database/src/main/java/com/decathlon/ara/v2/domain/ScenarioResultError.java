package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_result_error")
public class ScenarioResultError {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scenario_result_error_seq")
    private Long id;

    private String trace;

    @OneToOne
    private ScenarioStepIncident stepIncident;
}
