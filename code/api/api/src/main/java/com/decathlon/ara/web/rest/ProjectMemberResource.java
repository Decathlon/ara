package com.decathlon.ara.web.rest;

import java.util.Map;

import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.MemberRelationship;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.service.MemberService;
import com.decathlon.ara.web.rest.util.RestConstants;

public abstract class ProjectMemberResource<M extends Member, R extends MemberRelationship<Project, M>> extends MemberResource<Project, M, R> {

    static final String BASE_PATH = RestConstants.PROJECT_API_PATH + MemberResource.BASE_PATH;

    protected ProjectMemberResource(MemberService<Project, M, R> memberService) {
        super(memberService);
    }

    @Override
    protected String getIdentifier(Map<String, String> pathVariables) {
        return pathVariables.get("projectCode");
    }

}
