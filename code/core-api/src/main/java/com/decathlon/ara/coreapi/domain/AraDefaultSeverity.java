package com.decathlon.ara.coreapi.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class AraDefaultSeverity {
    @EmbeddedId
    private CodeWithProjectId id;
    
    private String name;
    private String description;

    private long level;
    private double warningThreshold;
    private double failureThreshold;
    
    public CodeWithProjectId getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public long getLevel() {
        return level;
    }
    public double getWarningThreshold() {
        return warningThreshold;
    }
    public double getFailureThreshold() {
        return failureThreshold;
    }
    
    
}
