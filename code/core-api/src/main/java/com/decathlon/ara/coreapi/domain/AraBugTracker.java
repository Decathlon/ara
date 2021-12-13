package com.decathlon.ara.coreapi.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class AraBugTracker {
    @Id
    private String code;

    private String name;
    private String description;

    @OneToOne(optional = false)
    private AraProject project;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AraProject getProject() {
        return project;
    }
    
    
}
