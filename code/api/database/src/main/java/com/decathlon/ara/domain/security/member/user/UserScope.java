package com.decathlon.ara.domain.security.member.user;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.role.ProjectRole;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class UserScope {

    @EmbeddedId
    private UserScopeId userScopeId;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login")
    @JoinColumn(name = "providerName", referencedColumnName = "provider")
    private User user;

    @MapsId("projectId")
    @ManyToOne
    @JoinColumn(updatable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(10) default 'MEMBER'")
    private ProjectRole role;

    public UserScope() {
        this.role = ProjectRole.MEMBER;
    }

    public UserScope(@NonNull User user, @NonNull Project project, @NonNull ProjectRole role) {
        this.userScopeId = new UserScopeId(user.getProviderName(), user.getLogin(), project.getId());
        this.user = user;
        this.project = project;
        this.role = role;
    }

    public User getUser() {
        return user;
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

    public static class UserScopeId implements Serializable {

        private User.UserId userId;

        private Long projectId;

        public UserScopeId(@NonNull User user, @NonNull Project project) {
            this.userId = new User.UserId(user.getProviderName(), user.getLogin());
            this.projectId = project.getId();
        }

        public UserScopeId(@NonNull String providerName, @NonNull String login, @NonNull Long projectId) {
            this.userId = new User.UserId(providerName, login);
            this.projectId = projectId;
        }

        public User.UserId getUserId() {
            return userId;
        }

        public Long getProjectId() {
            return projectId;
        }
    }
}
