package com.decathlon.ara.coreapi.domain;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import java.io.Serializable;

@Embeddable
public class CodeWithProjectId implements Serializable {
    @ManyToOne
    private AraProject project;
    private String code;
    
    protected AraProject getProject() {
        return project;
    }
    protected String getCode() {
        return code;
    }
    
}
