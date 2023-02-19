package com.decathlon.ara.domain.security.member.user.account;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_account_scope")
public class UserProjectScope extends ProjectScope {

    @EmbeddedId
    private UserProjectScopeId scopeId;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login")
    @JoinColumn(name = "providerName", referencedColumnName = "provider")
    private User user;

    public UserProjectScope() {
        super();
    }

    public UserProjectScope(@NonNull User user, @NonNull Project project, @NonNull ProjectRole role) {
        super(project, role);
        this.scopeId = new UserProjectScopeId(user.getProviderName(), user.getLogin(), project.getId());
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public static class UserProjectScopeId implements Serializable {

        private User.UserId userId;

        private Long projectId;

        public UserProjectScopeId() {
        }

        public UserProjectScopeId(@NonNull User user, @NonNull Project project) {
            this.userId = new User.UserId(user.getProviderName(), user.getLogin());
            this.projectId = project.getId();
        }

        public UserProjectScopeId(@NonNull String providerName, @NonNull String login, @NonNull Long projectId) {
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
