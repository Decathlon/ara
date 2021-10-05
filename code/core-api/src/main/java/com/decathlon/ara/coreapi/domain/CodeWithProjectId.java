package com.decathlon.ara.coreapi.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class CodeWithProjectId implements Serializable {
    @ManyToOne
    private AraProject project;
    private String code;
}
