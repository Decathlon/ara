package com.decathlon.ara.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.GroupRepository;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.service.dto.group.GroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;

@Service
@Transactional
public class GroupService {

    private static final String NAME_FOR_MESSAGE = "group";

    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private GroupMemberRepository groupMemberRepository;
    private ProjectGroupMemberRepository projectGroupMemberRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, GroupMemberRepository groupMemberRepository, ProjectGroupMemberRepository projectGroupMemberRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.projectGroupMemberRepository = projectGroupMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<GroupDTO> findAll() {
        return groupRepository.findAll().stream().map(group -> new GroupDTO(group.getMemberName())).toList();
    }

    @Transactional(readOnly = true)
    public GroupDTO findOne(String name) throws NotFoundException {
        Group group = groupRepository.findByMemberName(name);
        if (group == null) {
            throw new NotFoundException(String.format(Messages.NOT_FOUND, NAME_FOR_MESSAGE), NAME_FOR_MESSAGE);
        }
        return new GroupDTO(group.getMemberName());
    }

    public GroupDTO create(GroupDTO groupDto) throws NotUniqueException {
        Group group = groupRepository.findByMemberName(groupDto.getName());
        if (group != null) {
            throw new NotUniqueException(String.format(Messages.ALREADY_EXIST, NAME_FOR_MESSAGE), NAME_FOR_MESSAGE, "name", group.getName());
        }
        group = new Group(groupDto.getName());
        groupRepository.save(group);
        GroupMember groupMember = new GroupMember(group, userRepository.findByMemberName(SecurityContextHolder.getContext().getAuthentication().getName()));
        groupMember.setRole(MemberRole.ADMIN);
        groupMemberRepository.save(groupMember);
        return groupDto;
    }

    public void delete(String groupName) throws BadRequestException {
        Group group = groupRepository.findByMemberName(groupName);
        if (group == null) {
            throw new NotFoundException(String.format(Messages.NOT_FOUND, NAME_FOR_MESSAGE), NAME_FOR_MESSAGE);
        }
        List<ProjectGroupMember> projectGroupMemberList = projectGroupMemberRepository.findAllByIdMemberName(groupName);
        if (!projectGroupMemberList.isEmpty()) {
            throw new BadRequestException("Group cannot be deleted, because it is actually member of all these projects : " + projectGroupMemberList.stream().map(projectGroupMember -> projectGroupMember.getProject().getCode()).collect(Collectors.joining(", ")), NAME_FOR_MESSAGE, "member_of_project");
        }
        groupMemberRepository.deleteByIdGroupName(groupName);
        groupRepository.delete(group);
    }

}
