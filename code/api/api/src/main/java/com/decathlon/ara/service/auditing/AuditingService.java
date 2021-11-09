package com.decathlon.ara.service.auditing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.UserRole;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.ProjectUserMemberRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.repository.UserRoleRepository;
import com.decathlon.ara.service.dto.auditing.ProjectRoleDetails;
import com.decathlon.ara.service.dto.auditing.UserRoleDetails;

@Service
@Transactional(readOnly = true)
public class AuditingService {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private GroupMemberRepository groupMemberRepository;
    private ProjectUserMemberRepository projectUserMemberRepository;
    private ProjectGroupMemberRepository projectGroupMemberRepository;

    public AuditingService(UserRepository userRepository, UserRoleRepository userRoleRepository, GroupMemberRepository groupMemberRepository, ProjectUserMemberRepository projectUserMemberRepository, ProjectGroupMemberRepository projectGroupMemberRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.projectUserMemberRepository = projectUserMemberRepository;
        this.projectGroupMemberRepository = projectGroupMemberRepository;
    }

    public List<UserRoleDetails> auditUsersRoles() {
        List<UserRoleDetails> result = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserRoleDetails details = new UserRoleDetails(user.getMemberName());
            for (UserRole userRole : userRoleRepository.findAllByIdUserId(user.getMemberName())) {
                details.addRoles(userRole.getRole());
            }
            for (GroupMember groupMember : groupMemberRepository.findAllByIdUserName(user.getMemberName())) {
                for (ProjectGroupMember projectGroupMember : projectGroupMemberRepository.findAllByIdMemberName(groupMember.getGroupName())) {
                    ProjectRoleDetails projectDetails = details.getProject(projectGroupMember.getProject().getCode());
                    projectDetails.addRole(projectGroupMember.getRole(), groupMember.getGroupName());
                }
            }
            for (ProjectUserMember projectUserMember : projectUserMemberRepository.findAllByIdMemberName(user.getMemberName())) {
                ProjectRoleDetails projectDetails = details.getProject(projectUserMember.getProject().getCode());
                projectDetails.addRole(projectUserMember.getRole(), null);
            }
            result.add(details);
        }
        return result;
    }

}
