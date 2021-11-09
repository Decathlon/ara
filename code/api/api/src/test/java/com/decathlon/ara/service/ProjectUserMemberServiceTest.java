package com.decathlon.ara.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.MemberContainerRepository;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.MemberRelationshipRepository;
import com.decathlon.ara.repository.MemberRepository;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.ProjectUserMemberRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
public class ProjectUserMemberServiceTest extends MemberServiceTest<Project, User, ProjectUserMember>{
    
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectUserMemberRepository projectUserMemberRepository;
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ProjectUserMemberService projectUserMemberService;

    @Override
    protected MemberService<Project, User, ProjectUserMember> getMemberService() {
        return projectUserMemberService;
    }

    @Override
    protected MemberContainerRepository<Project, ?> getMemberContainerRepository() {
        return projectRepository;
    }

    @Override
    protected MemberRelationshipRepository<Project, User, ProjectUserMember> getMemberRelationshipRepository() {
        return projectUserMemberRepository;
    }

    @Override
    protected MemberRepository<User> getMemberRepository() {
        return userRepository;
    }
    
    @Override
    protected Project contructContainer(String name) {
        return new Project(name, name);
    }

    @Override
    protected User contructMember(String memberName) {
        User user = new User();
        TestUtil.setField(user, "id", memberName);
        return user;
    }

    @Override
    protected ProjectUserMember contructMemberRelationship(String memberName, MemberRole role) {
        ProjectUserMember projectUserMember = new ProjectUserMember(new Project(), contructMember(memberName));
        projectUserMember.setRole(role);
        return projectUserMember;
    }

    @Test
    @Override
    void addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist() throws BadRequestException {
        addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist((container, member, relationship) -> Assertions.assertEquals(container, relationship.getProject()));
    }

}
