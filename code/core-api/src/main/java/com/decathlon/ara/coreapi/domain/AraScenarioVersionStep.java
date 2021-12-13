package com.decathlon.ara.coreapi.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class AraScenarioVersionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_scenario_version_step_id")
    @SequenceGenerator(name = "ara_scenario_version_step_id", sequenceName = "ara_scenario_version_step_id", allocationSize = 1)
    private Long id;

    private long line;
    private String content;

    protected Long getId() {
        return id;
    }

    protected long getLine() {
        return line;
    }

    protected String getContent() {
        return content;
    }

}
