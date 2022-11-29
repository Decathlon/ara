package com.decathlon.ara.security.service.resource.project;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.security.dto.permission.ResourcePermission;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.service.ProjectService;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public abstract class ProjectResourceAccess {

    protected AuthorityService authorityService;

    protected ProjectService projectService;

    protected ProjectResourceAccess(AuthorityService authorityService, ProjectService projectService) {
        this.authorityService = authorityService;
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

        var userProfile = authorityService.getProfile();
        if (userProfile.isEmpty()) {
            return false;
        }
        if (UserEntity.UserEntityProfile.SUPER_ADMIN.equals(userProfile.get())) {
            return true;
        }

        if (UserEntity.UserEntityProfile.AUDITOR.equals(userProfile.get())) {
            return permission.isReadOnly();
        }

        var role = authorityService.getRoleOnProject(projectCode);
        var permissionsByRole = getPermissionsByRole();

        return role.map(permissionsByRole::get)
                .map(rolePermissions -> rolePermissions.contains(permission))
                .orElse(false);
    }

    private Map<UserEntityRoleOnProject.ScopedUserRoleOnProject, List<ResourcePermission>> getPermissionsByRole() {
        var admin = UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN;
        var maintainer = UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER;
        var member = UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER;
        return Map.ofEntries(
                entry(admin, getAdminPermissions()),
                entry(maintainer, getMaintainerPermissions()),
                entry(member, getMemberPermissions())
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
