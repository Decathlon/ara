package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@Entity
public class AraTeam {

    @EmbeddedId
    private CodeWithProjectId id;

    private String name;
    private String description;

}
