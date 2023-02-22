package com.decathlon.ara.security.access.project;

import com.decathlon.ara.security.dto.permission.ResourcePermission;
import com.decathlon.ara.security.service.member.user.account.UserSessionService;
import com.decathlon.ara.service.ProjectService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("projectData")
public class ProjectDataResourceAccess extends ProjectResourceAccess {

    public ProjectDataResourceAccess(UserSessionService userSessionService, ProjectService projectService) {
        super(userSessionService, projectService);
    }

    @Override
    protected List<ResourcePermission> getMaintainerPermissions() {
        return getAllPermissions();
    }

    @Override
    protected List<ResourcePermission> getMemberPermissions() {
        return getReadOnlyPermissions();
    }
}
