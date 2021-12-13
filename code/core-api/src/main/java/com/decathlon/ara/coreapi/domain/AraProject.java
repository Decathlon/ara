package com.decathlon.ara.coreapi.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AraProject implements Serializable {

    @Id
    private String code;
    
    private String name;
    private String description;
    private boolean enabled = true;
    
    protected String getCode() {
        return code;
    }
    protected String getName() {
        return name;
    }
    protected String getDescription() {
        return description;
    }
    protected boolean isEnabled() {
        return enabled;
    }

}
