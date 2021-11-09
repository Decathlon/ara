package com.decathlon.ara.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.MemberContainerRepository;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.GroupRepository;
import com.decathlon.ara.repository.MemberRelationshipRepository;
import com.decathlon.ara.repository.MemberRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
public class GroupMemberServiceTest extends MemberServiceTest<Group, User, GroupMember>{
    
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private GroupMemberService groupMemberService;

    @Override
    protected MemberService<Group, User, GroupMember> getMemberService() {
        return groupMemberService;
    }

    @Override
    protected MemberContainerRepository<Group, ?> getMemberContainerRepository() {
        return groupRepository;
    }

    @Override
    protected MemberRelationshipRepository<Group, User, GroupMember> getMemberRelationshipRepository() {
        return groupMemberRepository;
    }

    @Override
    protected MemberRepository<User> getMemberRepository() {
        return userRepository;
    }

    @Override
    protected GroupMember contructMemberRelationship(String memberName, MemberRole role) {
        GroupMember groupMember = new GroupMember(new Group(), contructMember(memberName));
        groupMember.setRole(role);
        return groupMember;
    }

    @Override
    protected Group contructContainer(String name) {
        return new Group(name);
    }

    @Override
    protected User contructMember(String memberName) {
        User user = new User();
        TestUtil.setField(user, "id", memberName);
        return user;
    }
    
    @Test
    @Override
    void addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist() throws BadRequestException {
        addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist((container, member, relationship) -> Assertions.assertEquals(container, relationship.getGroup()));
    }
    
}
