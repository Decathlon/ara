package com.decathlon.ara.domain;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ProjectGroupMember extends ProjectMember<Group> {

    public ProjectGroupMember() {
    }

    public ProjectGroupMember(Project project, Group group) {
        super(project, group);
    }

}
