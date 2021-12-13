package com.decathlon.ara.coreapi.domain;

import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

@Entity
public class AraScenarioVersion extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_scenario_version_id")
    @SequenceGenerator(name = "ara_scenario_version_id", sequenceName = "ara_scenario_version_id", allocationSize = 1)
    private Long id;

    private String version;
    private String path;

    @ManyToOne
    private AraRepository repository;

    @ManyToOne
    private AraScenario scenario;

    @OneToMany
    private List<AraScenarioVersionStep> steps = Collections.emptyList();

    protected Long getId() {
        return id;
    }

    protected String getVersion() {
        return version;
    }

    protected String getPath() {
        return path;
    }

    protected AraRepository getRepository() {
        return repository;
    }

    protected AraScenario getScenario() {
        return scenario;
    }

    protected List<AraScenarioVersionStep> getSteps() {
        return steps;
    }

}
