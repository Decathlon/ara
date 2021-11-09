package com.decathlon.ara.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;

import com.decathlon.ara.domain.enumeration.MemberRole;

@MappedSuperclass
public abstract class ProjectMember<M extends Member> implements MemberRelationship<Project, M> {

    @Embeddable
    public static class ProjectMemberPk implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long projectId;
        private String memberName;

        public ProjectMemberPk() {
        }

        public ProjectMemberPk(Long projectId, String memberName) {
            this.projectId = projectId;
            this.memberName = memberName;
        }
    }

    @EmbeddedId
    private ProjectMemberPk id;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @MapsId("memberName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_name")
    private M member;
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    protected ProjectMember() {
    }

    protected ProjectMember(Project project, M member) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(member);
        this.id = new ProjectMemberPk(project.getId(), member.getMemberName());
        this.project = project;
        this.member = member;
    }

    public Project getProject() {
        return project;
    }

    public Long getProjectId() {
        return id.projectId;
    }

    @Override
    public M getMember() {
        return member;
    }

    @Override
    public Project getContainer() {
        return getProject();
    }

    @Override
    public String getMemberName() {
        return id.memberName;
    }

    @Override
    public MemberRole getRole() {
        return role;
    }

    @Override
    public void setRole(MemberRole role) {
        this.role = role;
    }
}
