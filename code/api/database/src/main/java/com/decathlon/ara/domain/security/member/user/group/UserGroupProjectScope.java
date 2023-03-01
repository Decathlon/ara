package com.decathlon.ara.domain.security.member.user.group;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_group_scope")
public class UserGroupProjectScope extends ProjectScope {

    @EmbeddedId
    private UserGroupProjectScopeId scopeId;

    @MapsId("groupId")
    @ManyToOne
    @JoinColumn(name = "user_group_id", referencedColumnName = "id")
    private UserGroup group;

    public UserGroupProjectScope() {
        super();
    }

    public UserGroupProjectScope(UserGroup group, Project project, ProjectRole role) {
        super(project, role);
        this.scopeId = new UserGroupProjectScopeId(group, project);
        this.group = group;
    }

    public static class UserGroupProjectScopeId implements Serializable {

        private Long groupId;

        private Long projectId;

        public UserGroupProjectScopeId() {
        }

        public UserGroupProjectScopeId(@NonNull UserGroup userGroup, @NonNull Project project) {
            this.groupId = userGroup.getId();
            this.projectId = project.getId();
        }

        public Long getGroupId() {
            return groupId;
        }

        public Long getProjectId() {
            return projectId;
        }
    }

    public UserGroup getGroup() {
        return group;
    }
}
