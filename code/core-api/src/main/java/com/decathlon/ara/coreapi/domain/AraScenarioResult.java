package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import java.util.Collections;
import java.util.List;

import javax.persistence.*;

@Data
@Entity
public class AraScenarioResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_execution_id")
    @SequenceGenerator(name = "ara_execution_id", sequenceName = "ara_execution_id", allocationSize = 1)
    private Long id;

    private String status;
    private String screenshotURL;
    private String videoURL;
    private String otherDisplayURL;

    @Lob
    private String trace;

    @ManyToOne
    private AraProblem problem;

    @OneToMany
    private List<AraScenarioVersionStep> incidentSteps = Collections.emptyList();

}
