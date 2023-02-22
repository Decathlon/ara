package com.decathlon.ara.security.access.project;

import com.decathlon.ara.security.dto.permission.ResourcePermission;
import com.decathlon.ara.security.service.member.user.account.UserSessionService;
import com.decathlon.ara.service.ProjectService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("projectInstance")
public class ProjectInstanceResourceAccess extends ProjectResourceAccess {

    public ProjectInstanceResourceAccess(UserSessionService userSessionService, ProjectService projectService) {
        super(userSessionService, projectService);
    }

    @Override
    protected List<ResourcePermission> getMaintainerPermissions() {
        return getReadOnlyPermissions();
    }

    @Override
    protected List<ResourcePermission> getMemberPermissions() {
        return getReadOnlyPermissions();
    }
}
