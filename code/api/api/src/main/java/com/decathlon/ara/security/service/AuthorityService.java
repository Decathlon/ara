package com.decathlon.ara.security.service;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.security.dto.user.LoggedInUserScopeDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AuthorityService {

    private static final String AUTHORITY_USER_PROJECT_SCOPE_PREFIX = "USER_PROJECT_SCOPE:";

    public static final String AUTHORITY_USER_PROFILE_PREFIX = "USER_PROFILE:";

    /**
     * Get the role on a given project, if found.
     * Note that if the user is a super admin or an auditor, the role is still returned (if found) but not taken into account.
     * @param projectCode the project code
     * @return the role, if found
     */
    public Optional<UserEntityRoleOnProject.ScopedUserRoleOnProject> getRoleOnProject(String projectCode) {
        if (StringUtils.isBlank(projectCode)) {
            return Optional.empty();
        }

        if (userDoesNotHaveAccessToAnyAuthority()) {
            return Optional.empty();
        }

        var splitAuthoritiesStream = getSplitAuthoritiesStreamFromPrefix(AUTHORITY_USER_PROJECT_SCOPE_PREFIX);
        return splitAuthoritiesStream
                .filter(array -> array.length == 3)
                .filter(array -> projectCode.equals(array[1]))
                .map(array -> array[2])
                .sorted()
                .map(UserEntityRoleOnProject.ScopedUserRoleOnProject::getScopeFromString)
                .findFirst()
                .orElse(Optional.empty());
    }

    private boolean userDoesNotHaveAccessToAnyAuthority() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        var authorities = authentication.getAuthorities();
        return CollectionUtils.isEmpty(authorities);
    }

    private Stream<String[]> getSplitAuthoritiesStreamFromPrefix(String prefix) {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(prefix))
                .map(authority -> authority.split(":"));
    }

    /**
     * Get the user profile, if found (and correct)
     * @return the user profile
     */
    public Optional<UserEntity.UserEntityProfile> getProfile() {
        if (userDoesNotHaveAccessToAnyAuthority()) {
            return Optional.empty();
        }

        var splitAuthoritiesStream = getSplitAuthoritiesStreamFromPrefix(AUTHORITY_USER_PROFILE_PREFIX);
        return splitAuthoritiesStream
                .filter(array -> array.length == 2)
                .map(array -> array[1])
                .map(UserEntity.UserEntityProfile::getProfileFromString)
                .findFirst()
                .orElse(Optional.empty());
    }

    /**
     * Get the user scoped project codes, if any
     * @return the scoped project codes
     */
    public List<String> getScopedProjectCodes() {
        if (userDoesNotHaveAccessToAnyAuthority()) {
            return new ArrayList<>();
        }

        var splitAuthoritiesStream = getSplitAuthoritiesStreamFromPrefix(AUTHORITY_USER_PROJECT_SCOPE_PREFIX);
        return splitAuthoritiesStream
                .filter(array -> array.length == 3)
                .map(array -> array[1])
                .sorted()
                .toList();
    }

    /**
     * Get the logged-in user scopes
     * @return the user scopes
     */
    public List<LoggedInUserScopeDTO> getLoggedInUserScopes() {
        if (userDoesNotHaveAccessToAnyAuthority()) {
            return new ArrayList<>();
        }

        var splitAuthoritiesStream = getSplitAuthoritiesStreamFromPrefix(AUTHORITY_USER_PROJECT_SCOPE_PREFIX);
        return splitAuthoritiesStream
                .filter(array -> array.length == 3)
                .map(array -> new LoggedInUserScopeDTO(array[1], array[2]))
                .toList();
    }
}
