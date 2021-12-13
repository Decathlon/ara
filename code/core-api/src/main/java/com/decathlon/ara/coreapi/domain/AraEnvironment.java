package com.decathlon.ara.coreapi.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class AraEnvironment {

    @EmbeddedId
    private CodeWithProjectId id;

    private String name;

    public CodeWithProjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
