package com.decathlon.ara.service.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional(readOnly = true)
public class SecurityService {

    private ProjectUserMemberRepository projectUserMemberRepository;
    private ProjectGroupMemberRepository projectGroupMemberRepository;
    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private GroupMemberRepository groupMemberRepository;

    private volatile boolean adminCreated;

    @Value("${ara.security.admin.init.name:}")
    private String firstAdminName;

    @Value("${ara.security.newUser.role:PROJECT_OR_GROUP_CREATOR}")
    private UserSecurityRole newUserRole;

    public SecurityService(ProjectUserMemberRepository projectUserMemberRepository, ProjectGroupMemberRepository projectGroupMemberRepository, UserRepository userRepository, UserRoleRepository userRoleRepository, GroupMemberRepository groupMemberRepository) {
        this.projectUserMemberRepository = projectUserMemberRepository;
        this.projectGroupMemberRepository = projectGroupMemberRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.groupMemberRepository = groupMemberRepository;
        adminCreated = userRoleRepository.existsByIdRole(UserSecurityRole.ADMIN);
    }

    @Cacheable(value = "security.user.project.roles", key = "#projectCode.concat(#userName)")
    public Set<MemberRole> getProjectMemberRoles(String projectCode, String userName) {
        Set<MemberRole> memberRoles = new HashSet<>();
        ProjectUserMember userMember = projectUserMemberRepository.findByProjectCodeAndIdMemberName(projectCode, userName);
        if (userMember != null) {
            memberRoles.add(userMember.getRole());
        }
        for (ProjectGroupMember groupMember : projectGroupMemberRepository.findAllProjectGroupMemberByProjectCodeAndUserName(projectCode, userName)) {
            memberRoles.add(groupMember.getRole());
        }
        return memberRoles;
    }

    public Set<MemberRole> getGroupMemberRoles(String groupName, String userName) {
        Set<MemberRole> memberRoles = new HashSet<>();
        GroupMember groupMember = groupMemberRepository.findByContainerIdentifierAndMemberName(groupName, userName);
        if (groupMember != null) {
            memberRoles.add(groupMember.getRole());
        }
        return memberRoles;
    }

    @Transactional
    public User initUser(String userName, String issuer) {
        User user = userRepository.findByNameAndIssuer(userName, issuer);
        if (user == null) {
            user = new User(userName, issuer);
            userRepository.save(user);
            if (!adminCreated && (firstAdminName == null || firstAdminName.isBlank() || firstAdminName.equals(userName))) {
                synchronized (SecurityService.class) {
                    if (!adminCreated) {
                        initUserRole(user, UserSecurityRole.ADMIN);
                        adminCreated = true;
                    }
                }
            }
            initUserRole(user, newUserRole);
        }
        return user;
    }

    private void initUserRole(User user, UserSecurityRole role) {
        if (role != null) {
            UserRole userRole = userRoleRepository.findByIdUserIdAndIdRole(user.getId(), role);
            if (userRole == null) {
                userRole = new UserRole(user, role);
                userRoleRepository.save(userRole);
            }
        }
    }

    public List<UserSecurityRole> getUserRoles(String userId) {
        return userRoleRepository.findAllByIdUserId(userId).stream().map(UserRole::getRole).toList();
    }

}
