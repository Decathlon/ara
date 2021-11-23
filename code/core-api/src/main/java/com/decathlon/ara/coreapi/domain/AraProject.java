package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class AraProject implements Serializable {

    @Id
    private String code;

    private String name;
    private String description;
    private boolean enabled = true;

}
