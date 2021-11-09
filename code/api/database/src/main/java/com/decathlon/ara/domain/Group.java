package com.decathlon.ara.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "group2")
public class Group implements Member {

    @Id
    private String name;

    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    @Override
    public String getMemberName() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
