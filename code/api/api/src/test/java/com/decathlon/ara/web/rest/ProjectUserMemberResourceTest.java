package com.decathlon.ara.web.rest;

import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.service.MemberService;
import com.decathlon.ara.service.ProjectUserMemberService;

@ExtendWith(MockitoExtension.class)
public class ProjectUserMemberResourceTest extends MemberResourceTest<Project, User, ProjectUserMember>{
    
    @Mock
    private ProjectUserMemberService projectUserMemberService;
    
    @InjectMocks
    private ProjectUserMemberResource projectUserMemberResource;

    @Override
    MemberService<Project, User, ProjectUserMember> getMemberService() {
        return projectUserMemberService;
    }

    @Override
    MemberResource<Project, User, ProjectUserMember> getMemberResource() {
        return projectUserMemberResource;
    }

    @Override
    Map<String, String> getParametersMap() {
        return Map.of("projectCode", "identifier");
    }


}
