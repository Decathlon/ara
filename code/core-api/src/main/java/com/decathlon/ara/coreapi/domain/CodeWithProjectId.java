package com.decathlon.ara.coreapi.domain;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class CodeWithProjectId implements Serializable {
    @ManyToOne
    private AraProject project;
    private String code;
}
