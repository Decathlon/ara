package com.decathlon.ara.service.security;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.GroupMember;
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
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {
    
    @Mock
    private ProjectUserMemberRepository projectUserMemberRepository;
    @Mock
    private ProjectGroupMemberRepository projectGroupMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    
    private SecurityService securityService;
    
    private void prepareTest(boolean adminCreated, UserSecurityRole newUserRole, String firstAdminName) {
        if (adminCreated) {
            Mockito.when(userRoleRepository.existsByIdRole(UserSecurityRole.ADMIN)).thenReturn(true);
        }
        securityService = new SecurityService(projectUserMemberRepository, projectGroupMemberRepository, userRepository, userRoleRepository, groupMemberRepository);
        TestUtil.setField(securityService, "newUserRole", newUserRole);
        TestUtil.setField(securityService, "firstAdminName", firstAdminName);
    }
    
    @Test
    void getProjectMemberRolesShouldReturnEmptySetWhenUserDoesntHaveAnyRole() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        Set<MemberRole> projectMemberRoles = securityService.getProjectMemberRoles(null, null);
        Assertions.assertNotNull(projectMemberRoles);
        Assertions.assertEquals(0, projectMemberRoles.size());
    }
    
    @Test
    void getProjectMemberRolesShouldReturnSetContainingOnlyRoleLinkedToUserWhenUserHasNoRoleLinkedToItsGroup() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        ProjectUserMember projectUserMember = new ProjectUserMember();
        projectUserMember.setRole(MemberRole.MAINTAINER);
        Mockito.when(projectUserMemberRepository.findByProjectCodeAndIdMemberName("a", "b")).thenReturn(projectUserMember);
        Set<MemberRole> projectMemberRoles = securityService.getProjectMemberRoles("a", "b");
        Assertions.assertEquals(1, projectMemberRoles.size());
        Assertions.assertEquals(projectUserMember.getRole(), projectMemberRoles.iterator().next());
    }
    
    @Test
    void getProjectMemberRolesShouldReturnSetContainingOnlyRoleLinkedToItsGroupWhenUserHasNoRoleLinkedToIt() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        ProjectGroupMember projectGroupMember = new ProjectGroupMember();
        projectGroupMember.setRole(MemberRole.MAINTAINER);
        Mockito.when(projectGroupMemberRepository.findAllProjectGroupMemberByProjectCodeAndUserName("a", "b")).thenReturn(List.of(projectGroupMember));
        Set<MemberRole> projectMemberRoles = securityService.getProjectMemberRoles("a", "b");
        Assertions.assertEquals(1, projectMemberRoles.size());
        Assertions.assertEquals(projectGroupMember.getRole(), projectMemberRoles.iterator().next());
    }
    
    @Test
    void getProjectMemberRolesShouldReturnSetContainingAllRoleWhenUserHasRoleLinkedToItAndToItsGroup() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        ProjectUserMember projectUserMember = new ProjectUserMember();
        projectUserMember.setRole(MemberRole.MEMBER);
        Mockito.when(projectUserMemberRepository.findByProjectCodeAndIdMemberName("a", "b")).thenReturn(projectUserMember);
        ProjectGroupMember projectGroupMember = new ProjectGroupMember();
        projectGroupMember.setRole(MemberRole.MAINTAINER);
        Mockito.when(projectGroupMemberRepository.findAllProjectGroupMemberByProjectCodeAndUserName("a", "b")).thenReturn(List.of(projectGroupMember));
        Set<MemberRole> projectMemberRoles = securityService.getProjectMemberRoles("a", "b");
        Assertions.assertEquals(2, projectMemberRoles.size());
        Assertions.assertTrue(projectMemberRoles.contains(projectUserMember.getRole()));
        Assertions.assertTrue(projectMemberRoles.contains(projectGroupMember.getRole()));
    }
    
    @Test
    void getGroupMemberRolesShouldReturnEmptySetWhenUserDoesntHaveAnyRole() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        Set<MemberRole> groupMemberRoles = securityService.getGroupMemberRoles(null, null);
        Assertions.assertNotNull(groupMemberRoles);
        Assertions.assertEquals(0, groupMemberRoles.size());
    }
    
    @Test
    void getGroupMemberRolesShouldReturnSetContainingOnlyUserGroupMemberRoleWhenItHasOne() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        GroupMember groupMember = new GroupMember();
        groupMember.setRole(MemberRole.MAINTAINER);
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName("a", "b")).thenReturn(groupMember);
        Set<MemberRole> groupMemberRoles = securityService.getGroupMemberRoles("a", "b");
        Assertions.assertEquals(1, groupMemberRoles.size());
        Assertions.assertEquals(groupMember.getRole(), groupMemberRoles.iterator().next());
    }
    
    @Test
    void getUserRolesShouldReturnEmptySetWhenUserDoesntHaveAnyRole() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        List<UserSecurityRole> userRoles = securityService.getUserRoles(null);
        Assertions.assertNotNull(userRoles);
        Assertions.assertEquals(0, userRoles.size());
    }
    
    @Test
    void getUserRolesShouldReturnSetContainingAllUserRoleWhenItHasAny() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        UserRole role1 = new UserRole(new User(), UserSecurityRole.PROJECT_OR_GROUP_CREATOR);
        UserRole role2 = new UserRole(new User(), UserSecurityRole.AUDITING);
        Mockito.when(userRoleRepository.findAllByIdUserId("a")).thenReturn(List.of(role1, role2));
        List<UserSecurityRole> userRoles = securityService.getUserRoles("a");
        Assertions.assertEquals(2, userRoles.size());
        Assertions.assertEquals(role1.getRole(), userRoles.get(0));
        Assertions.assertEquals(role2.getRole(), userRoles.get(1));
    }
    
    @Test
    void initUserShouldDoNothingAndReturnUserWhenItAlreadyExists() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        User user = new User();
        Mockito.when(userRepository.findByNameAndIssuer("a", "b")).thenReturn(user);
        User result = securityService.initUser("a", "b");
        Assertions.assertEquals(user, result);
        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(userRoleRepository, Mockito.times(0)).save(Mockito.any());
        boolean adminCreated = TestUtil.getField(securityService, "adminCreated");
        Assertions.assertFalse(adminCreated);
    }
    
    private static Stream<Arguments> provideUserCreationTestArguments() {
        return Stream.of(
                Arguments.of((String)null),
                Arguments.of(""),
                Arguments.of(" "));
    }
    
    @ParameterizedTest
    @MethodSource("provideUserCreationTestArguments")
    void initUserShouldCreateUserWithAdminAndNewUserRolesAndReturnUserWhenUserNotExistsAndAdminNotAlreadyCreatedAndFirstAdminNameIs(String fistAdminNameProperty) {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, fistAdminNameProperty);
        User result = securityService.initUser("a", "b");
        Mockito.verify(userRepository).save(result);
        Assertions.assertEquals("a", result.getName());
        Assertions.assertEquals("b", result.getIssuer());
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository, Mockito.times(2)).save(roleCaptor.capture());
        List<UserRole> allRoles = roleCaptor.getAllValues();
        Assertions.assertEquals(2, allRoles.size());
        Assertions.assertEquals(UserSecurityRole.ADMIN, allRoles.get(0).getRole());
        Assertions.assertEquals(UserSecurityRole.PROJECT_OR_GROUP_CREATOR, allRoles.get(1).getRole());
        boolean adminCreated = TestUtil.getField(securityService, "adminCreated");
        Assertions.assertTrue(adminCreated);
    }
    
    @Test
    void initUserShouldCreateUserWithOnlyNewUserRolesAndReturnUserWhenUserNotExistsAndAdminNotAlreadyCreatedAndFirstAdminNameIsDefineToAnotherUserName() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, "c");
        User result = securityService.initUser("a", "b");
        Mockito.verify(userRepository).save(result);
        Assertions.assertEquals("a", result.getName());
        Assertions.assertEquals("b", result.getIssuer());
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository).save(roleCaptor.capture());
        List<UserRole> allRoles = roleCaptor.getAllValues();
        Assertions.assertEquals(1, allRoles.size());
        Assertions.assertEquals(UserSecurityRole.PROJECT_OR_GROUP_CREATOR, allRoles.get(0).getRole());
        boolean adminCreated = TestUtil.getField(securityService, "adminCreated");
        Assertions.assertFalse(adminCreated);
    }
    
    @Test
    void initUserShouldCreateUserWithAdminAndNewUserRolesAndReturnUserWhenUserNotExistsAndAdminNotAlreadyCreatedAndFirstAdminNameIsDefineToCurrentUserName() {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, "a");
        User result = securityService.initUser("a", "b");
        Mockito.verify(userRepository).save(result);
        Assertions.assertEquals("a", result.getName());
        Assertions.assertEquals("b", result.getIssuer());
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository, Mockito.times(2)).save(roleCaptor.capture());
        List<UserRole> allRoles = roleCaptor.getAllValues();
        Assertions.assertEquals(2, allRoles.size());
        Assertions.assertEquals(UserSecurityRole.ADMIN, allRoles.get(0).getRole());
        Assertions.assertEquals(UserSecurityRole.PROJECT_OR_GROUP_CREATOR, allRoles.get(1).getRole());
        boolean adminCreated = TestUtil.getField(securityService, "adminCreated");
        Assertions.assertTrue(adminCreated);
    }
    
    @Test
    void initUserShouldCreateUserWithNewUserRoleAndReturnUserWhenUserNotExistsAndAdminAlreadyCreated() {
        prepareTest(true, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        User result = securityService.initUser("a", "b");
        Mockito.verify(userRepository).save(result);
        Assertions.assertEquals("a", result.getName());
        Assertions.assertEquals("b", result.getIssuer());
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository).save(roleCaptor.capture());
        List<UserRole> allRoles = roleCaptor.getAllValues();
        Assertions.assertEquals(1, allRoles.size());
        Assertions.assertEquals(UserSecurityRole.PROJECT_OR_GROUP_CREATOR, allRoles.get(0).getRole());
    }
    
    @Test
    void initUserShouldNotCreateMultipleUserWithAdminRoleWhenMultipleUserInitAtTheSameTimeAndAdminNotAlreadyCreated() throws InterruptedException, ExecutionException {
        prepareTest(false, UserSecurityRole.PROJECT_OR_GROUP_CREATOR, null);
        CountDownLatch waiter = new CountDownLatch(1);
        CountDownLatch waiter2 = new CountDownLatch(2);
        Mockito.when(userRepository.save(Mockito.any())).thenAnswer(invocationOnMock -> {
            waiter2.countDown();
            waiter.await();
            return null;
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<User> firstUserInitResultFuture = executor.submit(() -> securityService.initUser("a", "b"));
        Future<User> secondUserInitResultFuture = executor.submit(() -> securityService.initUser("c", "d"));
        executor.shutdown();
        waiter2.await();
        waiter.countDown();
        User firstUserInitResult = firstUserInitResultFuture.get();
        User secondUserInitResult = secondUserInitResultFuture.get();
        Mockito.verify(userRepository).save(firstUserInitResult);
        Mockito.verify(userRepository).save(secondUserInitResult);
        Assertions.assertEquals("a", firstUserInitResult.getName());
        Assertions.assertEquals("b", firstUserInitResult.getIssuer());
        Assertions.assertEquals("c", secondUserInitResult.getName());
        Assertions.assertEquals("d", secondUserInitResult.getIssuer());
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository, Mockito.times(3)).save(roleCaptor.capture());
        List<UserRole> allRoles = roleCaptor.getAllValues();
        Assertions.assertEquals(3, allRoles.size());
        Assertions.assertEquals(UserSecurityRole.ADMIN, allRoles.get(0).getRole());
        Assertions.assertEquals(UserSecurityRole.PROJECT_OR_GROUP_CREATOR, allRoles.get(1).getRole());
        Assertions.assertEquals(UserSecurityRole.PROJECT_OR_GROUP_CREATOR, allRoles.get(2).getRole());
    }
    
    @Test
    void initUserShouldCreateUserWithOnlyAdminRoleWhenAdminNotAlreadyCreatedAndNewUserRoleIsNull() {
        prepareTest(false, null, null);
        User result = securityService.initUser("a", "b");
        Mockito.verify(userRepository).save(result);
        Assertions.assertEquals("a", result.getName());
        Assertions.assertEquals("b", result.getIssuer());
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository).save(roleCaptor.capture());
        List<UserRole> allRoles = roleCaptor.getAllValues();
        Assertions.assertEquals(1, allRoles.size());
        Assertions.assertEquals(UserSecurityRole.ADMIN, allRoles.get(0).getRole());
    }
    
    @Test
    void initUserShouldCreateUserWithoutRoleWhenAdminAlreadyCreatedAndNewUserRoleIsNull() {
        prepareTest(true, null, null);
        User result = securityService.initUser("a", "b");
        Mockito.verify(userRepository).save(result);
        Assertions.assertEquals("a", result.getName());
        Assertions.assertEquals("b", result.getIssuer());
        Mockito.verify(userRoleRepository, Mockito.times(0)).save(Mockito.any());
    }
}
