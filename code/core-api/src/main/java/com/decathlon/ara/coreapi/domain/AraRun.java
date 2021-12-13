package com.decathlon.ara.coreapi.domain;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class AraRun extends Auditable<String> {

    @EmbeddedId
    private CodeWithProjectId id;

    private String version;
    private String status;
    private String discardReason;

    @OneToMany
    private List<AraRunSeverity> severities = Collections.emptyList();

    @OneToMany
    private List<AraExecution> executions = Collections.emptyList();

    @OneToMany
    private Set<AraTag> tags = Collections.emptySet();

    protected CodeWithProjectId getId() {
        return id;
    }

    protected String getVersion() {
        return version;
    }

    protected String getStatus() {
        return status;
    }

    protected String getDiscardReason() {
        return discardReason;
    }

    protected List<AraRunSeverity> getSeverities() {
        return severities;
    }

    protected List<AraExecution> getExecutions() {
        return executions;
    }

    protected Set<AraTag> getTags() {
        return tags;
    }

}
