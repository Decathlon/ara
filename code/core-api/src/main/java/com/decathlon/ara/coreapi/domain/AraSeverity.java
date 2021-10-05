package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Data
@Entity
public class AraSeverity {
    @EmbeddedId
    private CodeWithProjectId id;

    private String name;
    private String description;

    private long level;
    private double warningThreshold;
    private double failureThreshold;
}
