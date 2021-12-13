package com.decathlon.ara.coreapi.domain;

import java.time.LocalDateTime;
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
public class AraExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_execution_id")
    @SequenceGenerator(name = "ara_execution_id", sequenceName = "ara_execution_id", allocationSize = 1)
    private Long id;

    private String comment;
    private String link;

    private LocalDateTime startDatetime;

    @OneToMany
    private List<AraScenarioResult> scenarioResults = Collections.emptyList();

    @ManyToOne
    private AraEnvironment environment;

    public Long getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public String getLink() {
        return link;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public List<AraScenarioResult> getScenarioResults() {
        return scenarioResults;
    }

    public AraEnvironment getEnvironment() {
        return environment;
    }

}
