package com.decathlon.ara.domain.security.member.user;

import com.decathlon.ara.domain.Project;
import org.springframework.lang.NonNull;

import javax.persistence.*;

@MappedSuperclass
public class ProjectScope {

    @MapsId("projectId")
    @ManyToOne
    @JoinColumn(updatable = false)
    protected Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(10) default 'MEMBER'")
    protected ProjectRole role;

    public ProjectScope() {
        this.role = ProjectRole.MEMBER;
    }

    public ProjectScope(@NonNull Project project, @NonNull ProjectRole role) {
        this.project = project;
        this.role = role;
    }

    public Project getProject() {
        return project;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }
}
