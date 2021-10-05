package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class AraProject {

    @Id
    private String code;

    private String name;
    private String description;
    private boolean enabled = true;

}
