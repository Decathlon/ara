package com.decathlon.ara.web.rest;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.service.GroupMemberService;
import com.decathlon.ara.web.rest.util.RestConstants;

@RestController
@RequestMapping(GroupMemberResource.PATH)
public class GroupMemberResource extends MemberResource<Group, User, GroupMember> {

    static final String PATH = RestConstants.API_PATH + "/groups/{groupName}" + MemberResource.BASE_PATH;

    public GroupMemberResource(GroupMemberService groupMemberService) {
        super(groupMemberService);
    }

    @Override
    protected String getIdentifier(Map<String, String> pathVariables) {
        return pathVariables.get("groupName");
    }

}
