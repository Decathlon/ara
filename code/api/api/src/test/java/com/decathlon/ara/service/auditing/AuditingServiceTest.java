package com.decathlon.ara.service.auditing;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.UserRole;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.domain.enumeration.UserSecurityRole;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.ProjectUserMemberRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.repository.UserRoleRepository;
import com.decathlon.ara.service.dto.auditing.MemberRoleDetails;
import com.decathlon.ara.service.dto.auditing.ProjectRoleDetails;
import com.decathlon.ara.service.dto.auditing.UserRoleDetails;

@ExtendWith(MockitoExtension.class)
class AuditingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock(lenient = true)
    private UserRoleRepository userRoleRepository;
    @Mock(lenient = true)
    private GroupMemberRepository groupMemberRepository;
    @Mock(lenient = true)
    private ProjectUserMemberRepository projectUserMemberRepository;
    @Mock(lenient = true)
    private ProjectGroupMemberRepository projectGroupMemberRepository;

    @InjectMocks
    private AuditingService auditingService;

    @Test
    void auditUsersRolesShouldReturnEmptyListIfNoUserExists() {
        Assertions.assertTrue(auditingService.auditUsersRoles().isEmpty());
    }

    @Test
    void auditUsersRolesShouldReturnEmptyListWithAllUserWhenNoUserHasRight() {
        User user1 = new User("user1", "issuer");
        User user2 = new User("user2", "issuer");
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserRoleDetails> auditUsersRoles = auditingService.auditUsersRoles();

        Assertions.assertEquals(2, auditUsersRoles.size());
        Assertions.assertEquals(user1.getMemberName(), auditUsersRoles.get(0).getUserName());
        Assertions.assertTrue(auditUsersRoles.get(0).getRoles().isEmpty());
        Assertions.assertTrue(auditUsersRoles.get(0).getProjects().isEmpty());
        Assertions.assertEquals(user2.getMemberName(), auditUsersRoles.get(1).getUserName());
        Assertions.assertTrue(auditUsersRoles.get(1).getRoles().isEmpty());
        Assertions.assertTrue(auditUsersRoles.get(1).getProjects().isEmpty());
    }

    @Test
    void auditUsersRolesShouldReturnDetailsWithMultipleUserAndRoles() {
        User userWithOnlyGlobalRoles = new User("user1", "issuer");
        User userWithOnlyProjectsRoles = new User("user2", "issuer");
        User userWithComplexRoles = new User("user3", "issuer");

        Project project1 = new Project("project1", "project1");
        Project project2 = new Project("project2", "project2");
        Project project3 = new Project("project3", "project3");

        Group group1 = new Group("group1");
        Group group2 = new Group("group2");

        Mockito.when(userRepository.findAll()).thenReturn(List.of(userWithOnlyGlobalRoles, userWithOnlyProjectsRoles, userWithComplexRoles));

        Mockito.when(userRoleRepository.findAllByIdUserId(userWithOnlyGlobalRoles.getMemberName())).thenReturn(List.of(new UserRole(userWithOnlyGlobalRoles, UserSecurityRole.ADMIN)));
        Mockito.when(userRoleRepository.findAllByIdUserId(userWithComplexRoles.getMemberName())).thenReturn(List.of(new UserRole(userWithComplexRoles, UserSecurityRole.PROJECT_OR_GROUP_CREATOR), new UserRole(userWithComplexRoles, UserSecurityRole.AUDITING)));

        Mockito.when(groupMemberRepository.findAllByIdUserName(userWithOnlyProjectsRoles.getMemberName())).thenReturn(List.of(new GroupMember(group1, userWithOnlyProjectsRoles)));
        Mockito.when(groupMemberRepository.findAllByIdUserName(userWithComplexRoles.getMemberName())).thenReturn(List.of(new GroupMember(group1, userWithComplexRoles), new GroupMember(group2, userWithComplexRoles)));

        ProjectGroupMember project1group1Member = new ProjectGroupMember(project1, group1);
        project1group1Member.setRole(MemberRole.ADMIN);

        ProjectGroupMember project2group2Member = new ProjectGroupMember(project2, group2);
        project2group2Member.setRole(MemberRole.MEMBER);

        Mockito.when(projectGroupMemberRepository.findAllByIdMemberName(group1.getName())).thenReturn(List.of(project1group1Member));
        Mockito.when(projectGroupMemberRepository.findAllByIdMemberName(group2.getName())).thenReturn(List.of(project2group2Member));

        ProjectUserMember project1member1 = new ProjectUserMember(project1, userWithOnlyProjectsRoles);
        project1member1.setRole(MemberRole.MAINTAINER);

        ProjectUserMember project1member2 = new ProjectUserMember(project1, userWithComplexRoles);
        project1member2.setRole(MemberRole.ADMIN);

        ProjectUserMember project3member1 = new ProjectUserMember(project3, userWithComplexRoles);
        project3member1.setRole(MemberRole.ADMIN);

        Mockito.when(projectUserMemberRepository.findAllByIdMemberName(userWithOnlyProjectsRoles.getMemberName())).thenReturn(List.of(project1member1));
        Mockito.when(projectUserMemberRepository.findAllByIdMemberName(userWithComplexRoles.getMemberName())).thenReturn(List.of(project1member2, project3member1));

        List<UserRoleDetails> auditUsersRoles = auditingService.auditUsersRoles();

        List<UserRoleDetails> expected = new ArrayList<>();
        UserRoleDetails userRoleDetails = new UserRoleDetails(userWithOnlyGlobalRoles.getMemberName());
        userRoleDetails.addRoles(UserSecurityRole.ADMIN);
        expected.add(userRoleDetails);
        userRoleDetails = new UserRoleDetails(userWithOnlyProjectsRoles.getMemberName());
        ProjectRoleDetails projectRoleDetails = userRoleDetails.getProject(project1.getCode());
        projectRoleDetails.addRole(MemberRole.ADMIN, group1.getName());
        projectRoleDetails.addRole(MemberRole.MAINTAINER, null);
        expected.add(userRoleDetails);
        userRoleDetails = new UserRoleDetails(userWithComplexRoles.getMemberName());
        userRoleDetails.addRoles(UserSecurityRole.PROJECT_OR_GROUP_CREATOR);
        userRoleDetails.addRoles(UserSecurityRole.AUDITING);
        projectRoleDetails = userRoleDetails.getProject(project1.getCode());
        projectRoleDetails.addRole(MemberRole.ADMIN, group1.getName());
        projectRoleDetails.addRole(MemberRole.ADMIN, null);
        projectRoleDetails = userRoleDetails.getProject(project2.getCode());
        projectRoleDetails.addRole(MemberRole.MEMBER, group2.getName());
        projectRoleDetails = userRoleDetails.getProject(project3.getCode());
        projectRoleDetails.addRole(MemberRole.ADMIN, null);
        expected.add(userRoleDetails);

        Assertions.assertEquals(expected.size(), auditUsersRoles.size());
        for (int i = 0; i < auditUsersRoles.size(); i++) {
            checkDetails(expected.get(i), auditUsersRoles.get(i));
        }
    }

    public void checkDetails(UserRoleDetails expected, UserRoleDetails result) {
        Assertions.assertEquals(expected.getUserName(), result.getUserName());
        Assertions.assertEquals(expected.getRoles(), result.getRoles());
        Assertions.assertEquals(expected.getProjects().size(), result.getProjects().size());
        for (int i = 0; i < result.getProjects().size(); i++) {
            checkDetails(expected.getProjects().get(i), result.getProjects().get(i));
        }
    }

    public void checkDetails(ProjectRoleDetails expected, ProjectRoleDetails result) {
        Assertions.assertEquals(expected.getCode(), result.getCode());
        Assertions.assertEquals(expected.getRoles().size(), result.getRoles().size());
        for (int i = 0; i < result.getRoles().size(); i++) {
            checkDetails(expected.getRoles().get(i), result.getRoles().get(i));
        }
    }

    public void checkDetails(MemberRoleDetails expected, MemberRoleDetails result) {
        Assertions.assertEquals(expected.getMemberRole(), result.getMemberRole());
        Assertions.assertEquals(expected.getInheritFromGroup(), result.getInheritFromGroup());
    }

}
