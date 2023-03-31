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
import org.springframework.security.core.context.SecurityContextHolder;

import com.decathlon.ara.configuration.security.TestAuthentication;
import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.GroupRepository;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.service.dto.group.GroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
    
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private ProjectGroupMemberRepository projectGroupMemberRepository;
    
    @InjectMocks
    private GroupService groupService;
    
    @Test
    void findAllShouldReturnEmptyListWhenNoGroupsExists() {
        Assertions.assertEquals(0, groupRepository.findAll().size());
    }
    
    @Test
    void findAllShouldReturnListContaningAllDatabaseGroup() {
        List<Group> databaseGroups = List.of(new Group("groupA"),
                new Group("groupB"),
                new Group("groupC"));
        
        Mockito.when(groupRepository.findAll()).thenReturn(databaseGroups);
        List<GroupDTO> resultList = groupService.findAll();
        Assertions.assertEquals(databaseGroups.size(), resultList.size());
        for (int i=0; i< databaseGroups.size(); i++) {
            Assertions.assertEquals(databaseGroups.get(i).getMemberName(), resultList.get(i).getName());
        }
    }
    
    @Test
    void findOneShouldThrowNotFoundExceptionWhenGroupDoesntExist() throws NotFoundException {
        Assertions.assertThrows(NotFoundException.class, () -> groupService.findOne("name"));
    }
    
    @Test
    void findOneShouldReturnGroupDTOWhenGroupExist() throws NotFoundException {
        Group group = new Group("name");
        Mockito.when(groupRepository.findByMemberName("name")).thenReturn(group);
        GroupDTO result = groupService.findOne("name");
        Assertions.assertEquals(group.getMemberName(), result.getName());
    }
    
    @Test
    void createShouldThrowNotUniqueExceptionWhenGroupAlreadyExists() {
        Mockito.when(groupRepository.findByMemberName("name")).thenReturn(new Group("name"));
        Assertions.assertThrows(NotUniqueException.class, ()-> groupService.create(new GroupDTO("name")));
        Mockito.verify(groupRepository, Mockito.times(0)).save(Mockito.any());
    }
    
    @Test
    void createShouldCreateGroupAndAddCurrentUserAsAdminWhenGroupNotAlreadyExists() throws NotUniqueException {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = new User();
        TestUtil.setField(user, "id", authentication.getName());
        Mockito.when(userRepository.findByMemberName(authentication.getName())).thenReturn(user);
        GroupDTO groupDto = new GroupDTO("name");
        GroupDTO result = groupService.create(groupDto);
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        Mockito.verify(groupRepository).save(groupCaptor.capture());
        Group savedGroup = groupCaptor.getValue();
        Assertions.assertEquals(groupDto, result);
        Assertions.assertEquals(groupDto.getName(), savedGroup.getName());
        ArgumentCaptor<GroupMember> groupMemberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        Mockito.verify(groupMemberRepository).save(groupMemberCaptor.capture());
        GroupMember savedGroupMember = groupMemberCaptor.getValue();
        Assertions.assertEquals(groupDto.getName(), savedGroupMember.getGroupName());
        Assertions.assertEquals(user.getId(), savedGroupMember.getMemberName());
        Assertions.assertEquals(MemberRole.ADMIN, savedGroupMember.getRole());
    }
    
    @Test
    void deletehouldThrowNotFoundExceptionWhenGroupDoesntExist() {
        Assertions.assertThrows(NotFoundException.class, () -> groupService.delete("name"));
        Mockito.verify(groupRepository, Mockito.times(0)).delete(Mockito.any());
        Mockito.verify(groupMemberRepository, Mockito.times(0)).deleteByIdGroupName(Mockito.any());
    }
    
    @Test
    void deleteShouldThrowBadRequestExceptionWhenGroupIsMemberOfProjects() throws BadRequestException {
        Group group = new Group("name");
        Mockito.when(groupRepository.findByMemberName("name")).thenReturn(group);
        Mockito.when(projectGroupMemberRepository.findAllByIdMemberName("name")).thenReturn(List.of(new ProjectGroupMember(new Project(), group)));
        Assertions.assertThrows(BadRequestException.class, () -> groupService.delete("name"));
        Mockito.verify(groupRepository, Mockito.times(0)).delete(Mockito.any());
        Mockito.verify(groupMemberRepository, Mockito.times(0)).deleteByIdGroupName(Mockito.any());
    }
    
    @Test
    void deleteShouldDeleteGroupAndAllItsMemberWhenItExists() throws BadRequestException {
        Group group = new Group("name");
        Mockito.when(groupRepository.findByMemberName("name")).thenReturn(group);
        groupService.delete("name");
        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        Mockito.verify(groupRepository).delete(captor.capture());
        Group deletedGroup = captor.getValue();
        Assertions.assertEquals(group, deletedGroup);
        Mockito.verify(groupMemberRepository).deleteByIdGroupName("name");
    }

}
