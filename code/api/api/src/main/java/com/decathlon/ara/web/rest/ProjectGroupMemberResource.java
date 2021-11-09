package com.decathlon.ara.web.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.service.ProjectGroupMemberService;

@RestController
@RequestMapping(ProjectGroupMemberResource.PATH)
public class ProjectGroupMemberResource extends ProjectMemberResource<Group, ProjectGroupMember> {

    static final String PATH = ProjectMemberResource.BASE_PATH + "/groups";

    public ProjectGroupMemberResource(ProjectGroupMemberService projectMemberService) {
        super(projectMemberService);
    }

}
