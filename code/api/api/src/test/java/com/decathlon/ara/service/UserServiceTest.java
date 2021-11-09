package com.decathlon.ara.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import com.decathlon.ara.configuration.security.TestAuthentication;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.UserRole;
import com.decathlon.ara.domain.enumeration.UserSecurityRole;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.repository.UserRoleRepository;
import com.decathlon.ara.service.dto.user.RoleDTO;
import com.decathlon.ara.service.dto.user.UserDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserRoleRepository userRoleRepository;
    
    @Mock
    private SessionRegistry sessionRegistry;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void findAllShouldReturnEmptyListWhenNoUsersExists() {
        Assertions.assertEquals(0, userRepository.findAll().size());
    }
    
    @Test
    void findAllShouldReturnListContaningAllDatabaseUserWithoutRoles() {
        List<User> databaseUsers = List.of(new User("userA", "issuerA"),
                new User("userB", "issuerA"),
                new User("userA", "issuerB"),
                new User("userC", "issuerB"));
        
        Mockito.when(userRepository.findAll()).thenReturn(databaseUsers);
        List<UserDTO> resultList = userService.findAll();
        Assertions.assertEquals(databaseUsers.size(), resultList.size());
        for (int i=0; i< databaseUsers.size(); i++) {
            Assertions.assertEquals(databaseUsers.get(i).getId(), resultList.get(i).getMemberName());
            Assertions.assertEquals(databaseUsers.get(i).getName(), resultList.get(i).getName());
            Assertions.assertEquals(databaseUsers.get(i).getIssuer(), resultList.get(i).getIssuer());
            Assertions.assertNull(resultList.get(i).getRoles());
        }
    }
    
    @Test
    void findOneShouldThrowNotFoundExceptionWhenUserDoesntExist() throws NotFoundException {
        Assertions.assertThrows(NotFoundException.class, () -> userService.findOne("userName"));
    }
    
    @Test
    void findOneShouldReturnUserDTOWithRolesWhenUserExist() throws NotFoundException {
        User user = new User("name", "issuer");
        Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
        List<UserRole> roles = List.of(new UserRole(user, UserSecurityRole.ADMIN), new UserRole(user, UserSecurityRole.AUDITING));
        Mockito.when(userRoleRepository.findAllByIdUserId(user.getId())).thenReturn(roles);
        UserDTO result = userService.findOne(user.getId());
        Assertions.assertEquals(user.getMemberName(), result.getMemberName());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getIssuer(), result.getIssuer());
        Assertions.assertEquals(roles.size(), result.getRoles().size());
        for (UserRole role : roles) {
            Assertions.assertTrue(result.getRoles().contains(role.getRole()));
        }
    }
    
    @Test
    void addRoleShouldThrowNotFoundExceptionWhenUserDoestExists() throws BadRequestException{
        Assertions.assertThrows(NotFoundException.class, () -> userService.addRole("userName", UserSecurityRole.ADMIN));
    }
    
    @Test
    void addRoleShouldThrowNotUniqueExceptionWhenUserHasAlreadyTheRole() throws BadRequestException{
        User user = new User("name", "issuer");
        Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
        Mockito.when(userRoleRepository.findByIdUserIdAndIdRole(user.getId(), UserSecurityRole.ADMIN)).thenReturn(new UserRole());
        Assertions.assertThrows(NotUniqueException.class, () -> userService.addRole(user.getId(), UserSecurityRole.ADMIN));
    }
    
    @Test
    void addRoleShouldReturnRoleDTOWhenUserExistAndUserHasNotAlreadyTheRole() throws BadRequestException {
        User user = new User("name", "issuer");
        Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
        RoleDTO result = userService.addRole(user.getId(), UserSecurityRole.ADMIN);
        Assertions.assertEquals(UserSecurityRole.ADMIN, result.getRole());
        ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository).save(captor.capture());
        UserRole savedUserRole = captor.getValue();
        Assertions.assertEquals(user, savedUserRole.getUser());
        Assertions.assertEquals(UserSecurityRole.ADMIN, savedUserRole.getRole());
    }
    
    @Test
    void addRoleShouldInvalidateSessionOfUpdatedUser() throws BadRequestException {
        User user = new User("name", "issuer");
        Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
        OAuth2User userPrincipal = new TestAuthentication(user.getId(), null);
        Mockito.when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(new TestAuthentication("other1", null), userPrincipal, new TestAuthentication("other2", null)));
        SessionInformation session1 = Mockito.mock(SessionInformation.class);
        SessionInformation session2 = Mockito.mock(SessionInformation.class);
        Mockito.when(sessionRegistry.getAllSessions(userPrincipal, false)).thenReturn(List.of(session1, session2));
        userService.addRole(user.getId(), UserSecurityRole.ADMIN);
        Mockito.verify(sessionRegistry, Mockito.times(0)).getAllSessions("other1", false);
        Mockito.verify(sessionRegistry, Mockito.times(0)).getAllSessions("other2", false);
        Mockito.verify(session1).expireNow();
        Mockito.verify(session2).expireNow();
    }
    
    @Test
    void addRoleShouldInvalidateSessionAfterTransactionCommitWhenTransactionStartedBeforeCall() throws BadRequestException {
        try {
            TransactionSynchronizationManager.initSynchronization();
            User user = new User("name", "issuer");
            Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
            OAuth2User userPrincipal = new TestAuthentication(user.getId(), null);
            Mockito.when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(userPrincipal));
            SessionInformation session1 = Mockito.mock(SessionInformation.class);
            SessionInformation session2 = Mockito.mock(SessionInformation.class);
            Mockito.when(sessionRegistry.getAllSessions(userPrincipal, false)).thenReturn(List.of(session1, session2));
            userService.addRole(user.getId(), UserSecurityRole.ADMIN);
            Mockito.verifyNoInteractions(session1, session2);
            
            TransactionSynchronizationUtils.triggerAfterCommit();
            
            Mockito.verify(session1).expireNow();
            Mockito.verify(session2).expireNow();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
    
    @Test
    void deleteRoleShouldThrowNotFoundExceptionWhenUserDoestExists() throws NotFoundException{
        Assertions.assertThrows(NotFoundException.class, () -> userService.deleteRole("userName", UserSecurityRole.ADMIN));
    }
    
    @Test
    void deleteRoleShouldThrowNotFoundExceptionWhenUserExistsAndUserRoleDoesntExists() throws NotFoundException{
        User user = new User("name", "issuer");
        Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
        Assertions.assertThrows(NotFoundException.class, () -> userService.deleteRole(user.getId(), UserSecurityRole.ADMIN));
    }
    
    @Test
    void deleteRoleShouldSucceedWhenUserAndUserRoleExists() throws NotFoundException {
        User user = new User("name", "issuer");
        Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
        UserRole userRole = new UserRole();
        Mockito.when(userRoleRepository.findByIdUserIdAndIdRole(user.getId(), UserSecurityRole.ADMIN)).thenReturn(userRole);
        userService.deleteRole(user.getId(), UserSecurityRole.ADMIN);
        ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
        Mockito.verify(userRoleRepository).delete(captor.capture());
        UserRole deletedUserRole = captor.getValue();
        Assertions.assertEquals(userRole, deletedUserRole);
    }
    
    @Test
    void deleteRoleShouldInvalidateSessionOfUpdatedUser() throws BadRequestException {
        User user = new User("name", "issuer");
        Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
        Mockito.when(userRoleRepository.findByIdUserIdAndIdRole(user.getId(), UserSecurityRole.ADMIN)).thenReturn(new UserRole());
        OAuth2User userPrincipal = new TestAuthentication(user.getId(), null);
        Mockito.when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(new TestAuthentication("other1", null), userPrincipal, new TestAuthentication("other2", null)));
        SessionInformation session1 = Mockito.mock(SessionInformation.class);
        SessionInformation session2 = Mockito.mock(SessionInformation.class);
        Mockito.when(sessionRegistry.getAllSessions(userPrincipal, false)).thenReturn(List.of(session1, session2));
        userService.deleteRole(user.getId(), UserSecurityRole.ADMIN);
        Mockito.verify(sessionRegistry, Mockito.times(0)).getAllSessions("other1", false);
        Mockito.verify(sessionRegistry, Mockito.times(0)).getAllSessions("other2", false);
        Mockito.verify(session1).expireNow();
        Mockito.verify(session2).expireNow();
    }
    
    @Test
    void deleteRoleShouldInvalidateSessionAfterTransactionCommitWhenTransactionStartedBeforeCall() throws BadRequestException {
        try {
            TransactionSynchronizationManager.initSynchronization();
            User user = new User("name", "issuer");
            Mockito.when(userRepository.findByMemberName(user.getId())).thenReturn(user);
            Mockito.when(userRoleRepository.findByIdUserIdAndIdRole(user.getId(), UserSecurityRole.ADMIN)).thenReturn(new UserRole());
            OAuth2User userPrincipal = new TestAuthentication(user.getId(), null);
            Mockito.when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(userPrincipal));
            SessionInformation session1 = Mockito.mock(SessionInformation.class);
            SessionInformation session2 = Mockito.mock(SessionInformation.class);
            Mockito.when(sessionRegistry.getAllSessions(userPrincipal, false)).thenReturn(List.of(session1, session2));
            userService.deleteRole(user.getId(), UserSecurityRole.ADMIN);
            Mockito.verifyNoInteractions(session1, session2);
            
            TransactionSynchronizationUtils.triggerAfterCommit();
            
            Mockito.verify(session1).expireNow();
            Mockito.verify(session2).expireNow();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
