package com.decathlon.ara.security.dto.user.scope;

import com.decathlon.ara.security.service.AuthorityService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Optional;

public class UserAccountScope {

    private String project;

    private UserAccountScopeRole role;

    public UserAccountScope(@NonNull String projectCode, @NonNull UserAccountScopeRole role) {
        this.project = projectCode;
        this.role = role;
    }

    public String getProject() {
        return project;
    }

    public UserAccountScopeRole getRole() {
        return role;
    }

    /**
     * Create a new {@link UserAccountScope}
     * @param projectCode the project code. Can't be blank
     * @param roleAsString the role, but as a string. Must match a {@link UserAccountScopeRole}.
     * @return a {@link UserAccountScope}, if projectCode and roleAsString are correct.
     */
    public static Optional<UserAccountScope> userAccountScopeFactory(@NonNull String projectCode, @NonNull String roleAsString) {
        if (StringUtils.isBlank(projectCode) || StringUtils.isBlank(roleAsString)) {
            return Optional.empty();
        }

        try {
            var userAccountRole = UserAccountScopeRole.valueOf(roleAsString.toUpperCase());
            return Optional.of(new UserAccountScope(projectCode, userAccountRole));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Get matching authority from project and role
     * @return the granted authority
     */
    @JsonIgnore
    public GrantedAuthority getMatchingAuthority() {
        var projectCodeAndRole = String.format("%s:%s", project, role);
        var authorityAsString = String.format("%s%s", AuthorityService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCodeAndRole);
        return () -> authorityAsString;
    }
}
