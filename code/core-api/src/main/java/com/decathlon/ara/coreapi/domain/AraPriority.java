package com.decathlon.ara.coreapi.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class AraPriority {

    @EmbeddedId
    private CodeWithProjectId id;

    private String name;
    private long level;

    protected CodeWithProjectId getId() {
        return id;
    }

    protected String getName() {
        return name;
    }

    protected long getLevel() {
        return level;
    }

}
