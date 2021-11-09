package com.decathlon.ara.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.MemberContainerRepository;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.GroupRepository;
import com.decathlon.ara.repository.MemberRelationshipRepository;
import com.decathlon.ara.repository.MemberRepository;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.exception.BadRequestException;

@ExtendWith(MockitoExtension.class)
public class ProjectGroupMemberServiceTest extends MemberServiceTest<Project, Group, ProjectGroupMember>{
    
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectGroupMemberRepository projectGroupMemberRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    
    @InjectMocks
    private ProjectGroupMemberService projectGroupMemberService;

    @Override
    protected MemberService<Project, Group, ProjectGroupMember> getMemberService() {
        return projectGroupMemberService;
    }

    @Override
    protected MemberContainerRepository<Project, ?> getMemberContainerRepository() {
        return projectRepository;
    }

    @Override
    protected MemberRelationshipRepository<Project, Group, ProjectGroupMember> getMemberRelationshipRepository() {
        return projectGroupMemberRepository;
    }

    @Override
    protected MemberRepository<Group> getMemberRepository() {
        return groupRepository;
    }
    
    @Override
    protected Project contructContainer(String name) {
        return new Project(name, name);
    }

    @Override
    protected Group contructMember(String memberName) {
        return new Group(memberName);
    }

    @Override
    protected ProjectGroupMember contructMemberRelationship(String memberName, MemberRole role) {
        ProjectGroupMember projectGroupMember = new ProjectGroupMember(new Project(), contructMember(memberName));
        projectGroupMember.setRole(role);
        return projectGroupMember;
    }

    @Test
    @Override
    void addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist() throws BadRequestException {
        addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist((container, member, relationship) -> Assertions.assertEquals(container, relationship.getProject()));
    }

}
