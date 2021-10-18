package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
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

}
