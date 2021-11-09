package com.decathlon.ara.web.rest;

import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.service.MemberService;
import com.decathlon.ara.service.ProjectGroupMemberService;

@ExtendWith(MockitoExtension.class)
public class ProjectGroupMemberResourceTest extends MemberResourceTest<Project, Group, ProjectGroupMember>{
    
    @Mock
    private ProjectGroupMemberService projectGroupMemberService;
    
    @InjectMocks
    private ProjectGroupMemberResource projectGroupMemberResource;

    @Override
    MemberService<Project, Group, ProjectGroupMember> getMemberService() {
        return projectGroupMemberService;
    }

    @Override
    MemberResource<Project, Group, ProjectGroupMember> getMemberResource() {
        return projectGroupMemberResource;
    }

    @Override
    Map<String, String> getParametersMap() {
        return Map.of("projectCode", "identifier");
    }


}
