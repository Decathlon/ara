package com.decathlon.ara.coreapi.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class AraScenario {

    @EmbeddedId
    private CodeWithProjectId id;

    private String name;

    protected CodeWithProjectId getId() {
        return id;
    }

    protected String getName() {
        return name;
    }

}
