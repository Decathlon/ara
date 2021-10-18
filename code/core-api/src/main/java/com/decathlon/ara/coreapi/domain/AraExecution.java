package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
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

}
