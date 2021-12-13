package com.decathlon.ara.coreapi.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class AraTeam {

    @EmbeddedId
    private CodeWithProjectId id;

    private String name;
    private String description;
    
    protected CodeWithProjectId getId() {
        return id;
    }
    protected String getName() {
        return name;
    }
    protected String getDescription() {
        return description;
    }

}
