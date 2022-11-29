package com.decathlon.ara.security.service.resource.project;

import com.decathlon.ara.security.dto.permission.ResourcePermission;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.service.ProjectService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("projectSettings")
public class ProjectSettingsResourceAccess extends ProjectResourceAccess {

    public ProjectSettingsResourceAccess(AuthorityService authorityService, ProjectService projectService) {
        super(authorityService, projectService);
    }

    @Override
    protected List<ResourcePermission> getMaintainerPermissions() {
        return getReadOnlyPermissions();
    }

    @Override
    protected List<ResourcePermission> getMemberPermissions() {
        return new ArrayList<>();
    }
}
