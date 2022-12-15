package com.decathlon.ara.domain.security.member.user.entity;

import com.decathlon.ara.domain.Project;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Optional;

@Entity
public class UserEntityRoleOnProject {

    private static final Logger LOG = LoggerFactory.getLogger(UserEntityRoleOnProject.class);

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
        this.userEntityRoleOnProjectId = new UserEntityRoleOnProjectId(userEntity.getLogin(), userEntity.getProviderName(), project.getId());
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
        MEMBER;

        /**
         * Get a scope from string, when found
         * @param scopeAsString the scope represented as string
         * @return the scope from string
         */
        public static Optional<ScopedUserRoleOnProject> getScopeFromString(String scopeAsString) {
            if (StringUtils.isBlank(scopeAsString)) {
                return Optional.empty();
            }

            try {
                return Optional.of(ScopedUserRoleOnProject.valueOf(scopeAsString.toUpperCase()));
            } catch (IllegalArgumentException exception) {
                LOG.warn("The user scope \"{}\" doesn't exist!", scopeAsString);
                return Optional.empty();
            }
        }
    }

    public static class UserEntityRoleOnProjectId implements Serializable {

        private UserEntity.UserEntityId userEntityId;

        private Long projectId;

        public UserEntityRoleOnProjectId() {
        }

        public UserEntityRoleOnProjectId(@NonNull UserEntity userEntity, @NonNull Project project) {
            this.userEntityId = new UserEntity.UserEntityId(userEntity.getLogin(), userEntity.getProviderName());
            this.projectId = project.getId();
        }

        public UserEntityRoleOnProjectId(@NonNull String login, @NonNull String providerName, @NonNull Long projectId) {
            this.userEntityId = new UserEntity.UserEntityId(login, providerName);
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
