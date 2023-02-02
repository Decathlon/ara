package com.decathlon.ara.security.service.resource.project;

import com.decathlon.ara.security.dto.permission.ResourcePermission;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.service.ProjectService;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public abstract class ProjectResourceAccess {

    protected UserSessionService userSessionService;

    protected ProjectService projectService;

    protected ProjectResourceAccess(UserSessionService userSessionService, ProjectService projectService) {
        this.userSessionService = userSessionService;
        this.projectService = projectService;
    }

    /**
     * Tells if the resource access is allowed for a given project and permission
     * @param projectCode the project code
     * @param permission the permission
     * @return true iff it is enabled
     */
    public boolean isEnabled(String projectCode, ResourcePermission permission) {
        if (StringUtils.isBlank(projectCode)) {
            return false;
        }

        if (permission == null) {
            return false;
        }

        var projectDoesNotExist = !projectService.exists(projectCode);
        if (projectDoesNotExist) {
            return false;
        }

        var userProfile = userSessionService.getCurrentUserProfile();
        if (userProfile.isEmpty()) {
            return false;
        }
        if (UserAccountProfile.SUPER_ADMIN.equals(userProfile.get())) {
            return true;
        }

        if (UserAccountProfile.AUDITOR.equals(userProfile.get())) {
            return permission.isReadOnly();
        }

        var role = userSessionService.getCurrentUserAccountScopeRoleFromProjectCode(projectCode);
        var permissionsByRole = getPermissionsByRole();

        return role.map(permissionsByRole::get)
                .map(rolePermissions -> rolePermissions.contains(permission))
                .orElse(false);
    }

    private Map<UserAccountScopeRole, List<ResourcePermission>> getPermissionsByRole() {
        return Map.ofEntries(
                entry(UserAccountScopeRole.ADMIN, getAdminPermissions()),
                entry(UserAccountScopeRole.MAINTAINER, getMaintainerPermissions()),
                entry(UserAccountScopeRole.MEMBER, getMemberPermissions())
        );
    }

    protected List<ResourcePermission> getAllPermissions() {
        return Arrays.stream(ResourcePermission.values()).toList();
    }

    protected List<ResourcePermission> getReadOnlyPermissions() {
        return Arrays.stream(ResourcePermission.values()).filter(ResourcePermission::isReadOnly).toList();
    }

    private List<ResourcePermission> getAdminPermissions() {
        return getAllPermissions();
    }

    protected abstract List<ResourcePermission> getMaintainerPermissions();

    protected abstract List<ResourcePermission> getMemberPermissions();
}
