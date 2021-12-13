package com.decathlon.ara.coreapi.domain;

import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

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

    protected Long getId() {
        return id;
    }

    protected String getStatus() {
        return status;
    }

    protected String getScreenshotURL() {
        return screenshotURL;
    }

    protected String getVideoURL() {
        return videoURL;
    }

    protected String getOtherDisplayURL() {
        return otherDisplayURL;
    }

    protected String getTrace() {
        return trace;
    }

    protected AraProblem getProblem() {
        return problem;
    }

    protected List<AraScenarioVersionStep> getIncidentSteps() {
        return incidentSteps;
    }

}
