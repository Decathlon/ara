package com.decathlon.ara.coreapi.domain;

import java.util.Collections;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class AraRule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_rule_id")
    @SequenceGenerator(name = "ara_rule_id", sequenceName = "ara_rule_id", allocationSize = 1)
    private Long id;

    @OneToOne(optional = false)
    private AraProject project;

    @OneToMany
    private Set<AraTag> runTags = Collections.emptySet();

    @OneToMany
    private Set<AraTag> executionTags = Collections.emptySet();

    protected Long getId() {
        return id;
    }

    protected AraProject getProject() {
        return project;
    }

    protected Set<AraTag> getRunTags() {
        return runTags;
    }

    protected Set<AraTag> getExecutionTags() {
        return executionTags;
    }

}
