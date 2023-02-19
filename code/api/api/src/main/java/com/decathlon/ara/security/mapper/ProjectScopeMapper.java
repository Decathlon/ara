package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ProjectScopeMapper {

    /**
     * Convert a list of {@link ProjectScope} into a list of {@link UserAccountScope}
     * @param projectScopes the list of {@link ProjectScope} to convert
     * @return the converted {@link UserAccountScope} list
     */
    public List<UserAccountScope> getUserAccountScopesFromProjectScopes(@NonNull Set<ProjectScope> projectScopes) {
        return projectScopes.stream()
                .filter(role -> role.getProject() != null)
                .filter(role -> role.getRole() != null)
                .map(ProjectScopeMapper::getUserAccountScopeFromProjectScope)
                .toList();
    }

    private static UserAccountScope getUserAccountScopeFromProjectScope(@NonNull ProjectScope projectScope) {
        var projectCode = projectScope.getProject().getCode();
        var userAccountRole = getUserAccountScopeRoleFromProjectRole(projectScope.getRole());
        return new UserAccountScope(projectCode, userAccountRole);
    }

    private static UserAccountScopeRole getUserAccountScopeRoleFromProjectRole(@NonNull ProjectRole projectRole) {
        return UserAccountScopeRole.valueOf(projectRole.name());
    }
}
