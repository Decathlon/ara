package com.decathlon.ara.domain.security.member.user.entity;

import com.decathlon.ara.domain.Project;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class UserEntityRoleOnProject {

    @EmbeddedId
    private UserEntityRoleOnProjectId userEntityRoleOnProjectId;

    @MapsId("userEntityId")
    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login")
    @JoinColumn(name = "providerName", referencedColumnName = "provider")
    private UserEntity userEntity;

    @MapsId("projectId")
    @ManyToOne
    @JoinColumn(updatable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(10) default 'MEMBER'")
    private ScopedUserRoleOnProject role;

    public UserEntityRoleOnProject() {
        this.role = ScopedUserRoleOnProject.MEMBER;
    }

    public UserEntityRoleOnProject(@NonNull UserEntity userEntity, @NonNull Project project, @NonNull ScopedUserRoleOnProject role) {
        this.userEntityRoleOnProjectId = new UserEntityRoleOnProjectId(userEntity.getProviderName(), userEntity.getLogin(), project.getId());
        this.userEntity = userEntity;
        this.project = project;
        this.role = role;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ScopedUserRoleOnProject getRole() {
        return role;
    }

    public void setRole(ScopedUserRoleOnProject role) {
        this.role = role;
    }

    public enum ScopedUserRoleOnProject {
        ADMIN,
        MAINTAINER,
        MEMBER
    }

    public static class UserEntityRoleOnProjectId implements Serializable {

        private UserEntity.UserEntityId userEntityId;

        private Long projectId;

        public UserEntityRoleOnProjectId() {
        }

        public UserEntityRoleOnProjectId(@NonNull UserEntity userEntity, @NonNull Project project) {
            this.userEntityId = new UserEntity.UserEntityId(userEntity.getProviderName(), userEntity.getLogin());
            this.projectId = project.getId();
        }

        public UserEntityRoleOnProjectId(@NonNull String providerName, @NonNull String login, @NonNull Long projectId) {
            this.userEntityId = new UserEntity.UserEntityId(providerName, login);
            this.projectId = projectId;
        }

        public UserEntity.UserEntityId getUserEntityId() {
            return userEntityId;
        }

        public Long getProjectId() {
            return projectId;
        }
    }
}
