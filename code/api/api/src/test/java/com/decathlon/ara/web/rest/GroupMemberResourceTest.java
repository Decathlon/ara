package com.decathlon.ara.web.rest;

import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.service.GroupMemberService;
import com.decathlon.ara.service.MemberService;

@ExtendWith(MockitoExtension.class)
public class GroupMemberResourceTest extends MemberResourceTest<Group, User, GroupMember>{
    
    @Mock
    private GroupMemberService groupMemberService;
    
    @InjectMocks
    private GroupMemberResource groupMemberResource;

    @Override
    MemberService<Group, User, GroupMember> getMemberService() {
        return groupMemberService;
    }

    @Override
    MemberResource<Group, User, GroupMember> getMemberResource() {
        return groupMemberResource;
    }

    @Override
    Map<String, String> getParametersMap() {
        return Map.of("groupName", "identifier");
    }


}
