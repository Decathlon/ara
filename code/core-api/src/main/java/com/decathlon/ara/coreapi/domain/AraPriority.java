package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Data
@Entity
public class AraPriority {

    @EmbeddedId
    private CodeWithProjectId id;

    private String name;
    private long level;

}
