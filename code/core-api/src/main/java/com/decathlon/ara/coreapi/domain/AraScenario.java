package com.decathlon.ara.coreapi.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

@Data
@Entity
public class AraScenario {

    @EmbeddedId
    private CodeWithProjectId id;

    private String name;

}
