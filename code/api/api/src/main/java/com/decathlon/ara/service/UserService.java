package com.decathlon.ara.service;

import java.util.List;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.decathlon.ara.Messages;
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
import com.decathlon.ara.transaction.RunnableAfterCommitTransactionSynchronization;

@Service
@Transactional(readOnly = true)
public class UserService {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private SessionRegistry sessionRegistry;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, SessionRegistry sessionRegistry) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.sessionRegistry = sessionRegistry;
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(user -> new UserDTO(user.getMemberName(), user.getName(), user.getIssuer())).toList();
    }

    public UserDTO findOne(String name) throws NotFoundException {
        User user = userRepository.findByMemberName(name);
        if (user == null) {
            throw new NotFoundException(String.format(Messages.NOT_FOUND, "user"), "user");
        }
        List<UserRole> allRoles = userRoleRepository.findAllByIdUserId(user.getId());
        return new UserDTO(user.getMemberName(), user.getName(), user.getIssuer(), allRoles.stream().map(UserRole::getRole).toList());
    }

    @Transactional
    public RoleDTO addRole(String name, UserSecurityRole role) throws BadRequestException {
        User user = userRepository.findByMemberName(name);
        if (user == null) {
            throw new NotFoundException(String.format(Messages.NOT_FOUND, "user"), "user");
        }
        UserRole userRole = userRoleRepository.findByIdUserIdAndIdRole(name, role);
        if (userRole != null) {
            throw new NotUniqueException(String.format(Messages.ALREADY_EXIST, "user role"), "user-role", "role", role.name());
        }
        userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);
        invalidateAssociatedSession(name);
        return new RoleDTO(role);
    }

    @Transactional
    public void deleteRole(String name, UserSecurityRole role) throws NotFoundException {
        User user = userRepository.findByMemberName(name);
        if (user == null) {
            throw new NotFoundException(String.format(Messages.NOT_FOUND, "user"), "user");
        }
        UserRole userRole = userRoleRepository.findByIdUserIdAndIdRole(name, role);
        if (userRole == null) {
            throw new NotFoundException(String.format(Messages.NOT_FOUND, "user role"), "user-role");
        }
        userRoleRepository.delete(userRole);
        invalidateAssociatedSession(name);
    }

    private void invalidateAssociatedSession(String name) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof OAuth2User oauth2Principal && oauth2Principal.getName().equals(name)) {
                RunnableAfterCommitTransactionSynchronization sessionInvalidateRunnable = () -> sessionRegistry.getAllSessions(oauth2Principal, false).forEach(SessionInformation::expireNow);
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.registerSynchronization(sessionInvalidateRunnable);
                } else {
                    sessionInvalidateRunnable.run();
                }
                break;
            }
        }
    }

}
