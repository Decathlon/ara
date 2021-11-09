package com.decathlon.ara.domain;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ProjectUserMember extends ProjectMember<User> {

    public ProjectUserMember() {
    }

    public ProjectUserMember(Project project, User user) {
        super(project, user);
    }

}
