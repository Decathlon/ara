package com.decathlon.ara.security.service.resource.project;

import com.decathlon.ara.security.dto.permission.ResourcePermission;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.service.ProjectService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("projectData")
public class ProjectDataResourceAccess extends ProjectResourceAccess {

    public ProjectDataResourceAccess(AuthorityService authorityService, ProjectService projectService) {
        super(authorityService, projectService);
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
