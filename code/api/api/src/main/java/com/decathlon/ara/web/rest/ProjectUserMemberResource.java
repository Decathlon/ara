package com.decathlon.ara.web.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.service.ProjectUserMemberService;

@RestController
@RequestMapping(ProjectUserMemberResource.PATH)
public class ProjectUserMemberResource extends ProjectMemberResource<User, ProjectUserMember> {

    static final String PATH = ProjectMemberResource.BASE_PATH + "/users";

    public ProjectUserMemberResource(ProjectUserMemberService projectMemberService) {
        super(projectMemberService);
    }

}
