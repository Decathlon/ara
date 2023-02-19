package com.decathlon.ara.security.service.user.group;

import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.repository.security.member.user.group.UserGroupRepository;
import com.decathlon.ara.security.dto.user.group.UserAccountGroup;
import com.decathlon.ara.security.mapper.UserGroupMapper;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountGroupServiceTest {

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private UserGroupRepository groupRepository;

    @Mock
    private UserGroupMapper groupMapper;

    @InjectMocks
    private UserAccountGroupService userAccountGroupService;

    @Test
    void createGroup_throwForbiddenException_whenCurrentUserNotFound() throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.createGroup(group));
        verify(groupRepository, never()).save(any());
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void createGroup_throwForbiddenException_whenGroupNameIsBlank(String groupName) throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);

        var currentUser = mock(User.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(group.getName()).thenReturn(groupName);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.createGroup(group));
        verify(groupRepository, never()).save(any());
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void createGroup_throwForbiddenException_whenGroupNameAlreadyExists() throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);
        var groupName = "group-name";

        var currentUser = mock(User.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(group.getName()).thenReturn(groupName);
        when(groupRepository.existsByName(groupName)).thenReturn(true);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.createGroup(group));
        verify(groupRepository, never()).save(any());
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void createGroup_saveNewGroup_whenGroupNameDoesNotExist() throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);
        var groupName = "group-name";
        var groupDescription = "group-description";

        var providerName = "provider-name";

        var currentUser = mock(User.class);

        var savedUserGroup = mock(UserGroup.class);
        var mappedSavedUserAccountGroup = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(group.getName()).thenReturn(groupName);
        when(group.getDescription()).thenReturn(groupDescription);
        when(groupRepository.existsByName(groupName)).thenReturn(false);
        when(groupRepository.save(any())).thenReturn(savedUserGroup);
        when(groupMapper.getUserAccountGroupFromUserGroup(savedUserGroup, currentUser)).thenReturn(mappedSavedUserAccountGroup);

        // Then
        var createdUserAccountGroup = userAccountGroupService.createGroup(group);
        assertThat(createdUserAccountGroup).isSameAs(mappedSavedUserAccountGroup);

        var groupToSaveArgumentCaptor = ArgumentCaptor.forClass(UserGroup.class);
        verify(groupRepository, times(1)).save(groupToSaveArgumentCaptor.capture());
        verifyNoMoreInteractions(groupRepository);

        var capturedGroup = groupToSaveArgumentCaptor.getValue();
        assertThat(capturedGroup)
                .extracting("providerName", "name", "description", "creationUser")
                .contains(providerName, groupName, groupDescription, currentUser);

        assertThat(capturedGroup.getScopes()).isEmpty();
        assertThat(capturedGroup.getMembers()).isEmpty();
        assertThat(capturedGroup.getManagers()).containsExactly(currentUser).hasSize(1);

        var now = ZonedDateTime.now();
        var oneSecondBeforeNow = now.minusSeconds(1);
        var oneSecondAfterNow = now.plusSeconds(1);
        var groupCreationDate = capturedGroup.getCreationDate();
        assertThat(groupCreationDate).isBetween(oneSecondBeforeNow, oneSecondAfterNow);
        verify(userSessionService).refreshCurrentUserAuthorities();
    }

    @Test
    void updateGroup_throwForbiddenException_whenCurrentUserNotFound() throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.updateGroup(group));
        verify(groupRepository, never()).save(any());
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void updateGroup_throwForbiddenException_whenGroupIdIsNull() throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);
        var groupName = "group-name";

        var currentUser = mock(User.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(group.getId()).thenReturn(null);
        when(group.getName()).thenReturn(groupName);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.updateGroup(group));
        verify(groupRepository, never()).save(any());
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateGroup_throwForbiddenException_whenGroupNameIsBlank(String groupName) throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);
        var groupId = 1L;

        var currentUser = mock(User.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(group.getId()).thenReturn(groupId);
        when(group.getName()).thenReturn(groupName);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.updateGroup(group));
        verify(groupRepository, never()).save(any());
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void updateGroup_throwForbiddenException_whenGroupDoesNotExist() throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);
        var groupId = 1L;
        var groupName = "group-name";

        var currentUser = mock(User.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(group.getId()).thenReturn(groupId);
        when(group.getName()).thenReturn(groupName);
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.updateGroup(group));
        verify(groupRepository, never()).save(any());
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void updateGroup_updateGroup_whenGroupDoesExist() throws ForbiddenException {
        // Given
        var group = mock(UserAccountGroup.class);
        var groupId = 1L;
        var groupNameToUpdate = "new-group-name";
        var groupDescriptionToUpdate = "new-group-description";

        var currentUser = mock(User.class);

        var creationUser = mock(User.class);
        var existingGroupName = "existing-group-name";
        var existingGroupDescription = "existing-group-description";
        var userGroupToUpdate = new UserGroup(existingGroupName, creationUser);
        userGroupToUpdate.setDescription(existingGroupDescription);

        var updatedUserGroup = mock(UserGroup.class);
        var mappedUpdatedUserAccountGroup = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(group.getId()).thenReturn(groupId);
        when(group.getName()).thenReturn(groupNameToUpdate);
        when(group.getDescription()).thenReturn(groupDescriptionToUpdate);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(userGroupToUpdate));
        when(groupRepository.save(userGroupToUpdate)).thenReturn(updatedUserGroup);
        when(groupMapper.getUserAccountGroupFromUserGroup(updatedUserGroup, currentUser)).thenReturn(mappedUpdatedUserAccountGroup);

        // Then
        var updatedUserAccountGroup = userAccountGroupService.updateGroup(group);
        assertThat(updatedUserAccountGroup).isSameAs(mappedUpdatedUserAccountGroup);

        var groupToUpdateArgumentCaptor = ArgumentCaptor.forClass(UserGroup.class);
        verify(groupRepository, times(1)).save(groupToUpdateArgumentCaptor.capture());
        verifyNoMoreInteractions(groupRepository);

        var capturedGroup = groupToUpdateArgumentCaptor.getValue();
        assertThat(capturedGroup)
                .extracting("name", "description", "creationUser", "updateUser")
                .contains(groupNameToUpdate, groupDescriptionToUpdate, creationUser, currentUser);

        var now = ZonedDateTime.now();
        var oneSecondBeforeNow = now.minusSeconds(1);
        var oneSecondAfterNow = now.plusSeconds(1);
        var groupUpdateDate = capturedGroup.getUpdateDate();
        assertThat(groupUpdateDate).isBetween(oneSecondBeforeNow, oneSecondAfterNow);

        verify(userSessionService).refreshCurrentUserAuthorities();
    }

    @Test
    void deleteGroup_deleteGroup_whenIdGiven() throws ForbiddenException {
        // Given
        var groupId = 1L;

        // When

        // Then
        userAccountGroupService.deleteGroup(groupId);
        verify(groupRepository, times(1)).deleteById(groupId);
        verify(userSessionService, times(1)).refreshCurrentUserAuthorities();
    }

    @Test
    void getAllUserAccountGroupsForCurrentProviderName_throwForbiddenException_whenNoCurrentUserFound() {
        // Given

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.getAllUserAccountGroupsForCurrentProviderName());
    }

    @Test
    void getAllUserAccountGroupsForCurrentProviderName_returnGroups_whenCurrentUserFound() throws ForbiddenException {
        // Given
        var currentUser = mock(User.class);

        var providerName = "provider-name";

        var group1 = mock(UserGroup.class);
        var group2 = mock(UserGroup.class);
        var group3 = mock(UserGroup.class);
        var groups = List.of(group1, group2, group3);

        var convertedGroup1 = mock(UserAccountGroup.class);
        var convertedGroup2 = mock(UserAccountGroup.class);
        var convertedGroup3 = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(groupRepository.findAllByProviderName(providerName)).thenReturn(groups);
        when(groupMapper.getUserAccountGroupFromUserGroup(group1, currentUser)).thenReturn(convertedGroup1);
        when(groupMapper.getUserAccountGroupFromUserGroup(group2, currentUser)).thenReturn(convertedGroup2);
        when(groupMapper.getUserAccountGroupFromUserGroup(group3, currentUser)).thenReturn(convertedGroup3);

        // Then
        var actualGroups = userAccountGroupService.getAllUserAccountGroupsForCurrentProviderName();
        assertThat(actualGroups).containsExactlyInAnyOrder(convertedGroup1, convertedGroup2, convertedGroup3);
    }

    @Test
    void getGroupsContainingUser_throwForbiddenException_whenCurrentUserNotFound() {
        // Given
        var userLogin = "user-login";

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.getGroupsContainingUser(userLogin));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getGroupsContainingUser_throwForbiddenException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var currentUser = mock(User.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.getGroupsContainingUser(userLogin));
    }

    @Test
    void getGroupsContainingUser_returnGroups_whenUserLoginIsNotBlank() throws ForbiddenException {
        // Given
        var currentUser = mock(User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var group1 = mock(UserGroup.class);
        var group2 = mock(UserGroup.class);
        var group3 = mock(UserGroup.class);
        var groups = List.of(group1, group2, group3);

        var convertedGroup1 = mock(UserAccountGroup.class);
        var convertedGroup2 = mock(UserAccountGroup.class);
        var convertedGroup3 = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(groupRepository.findAllByProviderNameAndMembersLogin(providerName, userLogin)).thenReturn(groups);
        when(groupMapper.getUserAccountGroupFromUserGroup(group1, currentUser)).thenReturn(convertedGroup1);
        when(groupMapper.getUserAccountGroupFromUserGroup(group2, currentUser)).thenReturn(convertedGroup2);
        when(groupMapper.getUserAccountGroupFromUserGroup(group3, currentUser)).thenReturn(convertedGroup3);

        // Then
        var actualGroups = userAccountGroupService.getGroupsContainingUser(userLogin);
        assertThat(actualGroups).containsExactlyInAnyOrder(convertedGroup1, convertedGroup2, convertedGroup3);
    }

    @Test
    void getGroupsContainingCurrentUser_throwForbiddenException_whenCurrentUserNotFound() {
        // Given

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.getGroupsContainingCurrentUser());
    }

    @Test
    void getGroupsContainingCurrentUser_returnGroups_whenUserLoginIsNotBlank() throws ForbiddenException {
        // Given
        var currentUser = mock(User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var group1 = mock(UserGroup.class);
        var group2 = mock(UserGroup.class);
        var group3 = mock(UserGroup.class);
        var groups = List.of(group1, group2, group3);

        var convertedGroup1 = mock(UserAccountGroup.class);
        var convertedGroup2 = mock(UserAccountGroup.class);
        var convertedGroup3 = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(currentUser.getLogin()).thenReturn(userLogin);
        when(groupRepository.findAllByProviderNameAndMembersLogin(providerName, userLogin)).thenReturn(groups);
        when(groupMapper.getUserAccountGroupFromUserGroup(group1, currentUser)).thenReturn(convertedGroup1);
        when(groupMapper.getUserAccountGroupFromUserGroup(group2, currentUser)).thenReturn(convertedGroup2);
        when(groupMapper.getUserAccountGroupFromUserGroup(group3, currentUser)).thenReturn(convertedGroup3);

        // Then
        var actualGroups = userAccountGroupService.getGroupsContainingCurrentUser();
        assertThat(actualGroups).containsExactlyInAnyOrder(convertedGroup1, convertedGroup2, convertedGroup3);
    }

    @Test
    void getGroupsManagedByUser_throwForbiddenException_whenCurrentUserNotFound() {
        // Given
        var userLogin = "user-login";

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.getGroupsManagedByUser(userLogin));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getGroupsManagedByUser_throwForbiddenException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var currentUser = mock(User.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.getGroupsManagedByUser(userLogin));
    }

    @Test
    void getGroupsManagedByUser_returnGroups_whenUserLoginIsNotBlank() throws ForbiddenException {
        // Given
        var currentUser = mock(User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var group1 = mock(UserGroup.class);
        var group2 = mock(UserGroup.class);
        var group3 = mock(UserGroup.class);
        var groups = List.of(group1, group2, group3);

        var convertedGroup1 = mock(UserAccountGroup.class);
        var convertedGroup2 = mock(UserAccountGroup.class);
        var convertedGroup3 = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(groupRepository.findAllByProviderNameAndManagersLogin(providerName, userLogin)).thenReturn(groups);
        when(groupMapper.getUserAccountGroupFromUserGroup(group1, currentUser)).thenReturn(convertedGroup1);
        when(groupMapper.getUserAccountGroupFromUserGroup(group2, currentUser)).thenReturn(convertedGroup2);
        when(groupMapper.getUserAccountGroupFromUserGroup(group3, currentUser)).thenReturn(convertedGroup3);

        // Then
        var actualGroups = userAccountGroupService.getGroupsManagedByUser(userLogin);
        assertThat(actualGroups).containsExactlyInAnyOrder(convertedGroup1, convertedGroup2, convertedGroup3);
    }

    @Test
    void getGroupsManagedByCurrentUser_throwForbiddenException_whenCurrentUserNotFound() {
        // Given

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountGroupService.getGroupsManagedByCurrentUser());
    }

    @Test
    void getGroupsManagedByCurrentUser_returnGroups_whenUserLoginIsNotBlank() throws ForbiddenException {
        // Given
        var currentUser = mock(User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var group1 = mock(UserGroup.class);
        var group2 = mock(UserGroup.class);
        var group3 = mock(UserGroup.class);
        var groups = List.of(group1, group2, group3);

        var convertedGroup1 = mock(UserAccountGroup.class);
        var convertedGroup2 = mock(UserAccountGroup.class);
        var convertedGroup3 = mock(UserAccountGroup.class);

        // When
        when(userAccountService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(currentUser.getLogin()).thenReturn(userLogin);
        when(groupRepository.findAllByProviderNameAndManagersLogin(providerName, userLogin)).thenReturn(groups);
        when(groupMapper.getUserAccountGroupFromUserGroup(group1, currentUser)).thenReturn(convertedGroup1);
        when(groupMapper.getUserAccountGroupFromUserGroup(group2, currentUser)).thenReturn(convertedGroup2);
        when(groupMapper.getUserAccountGroupFromUserGroup(group3, currentUser)).thenReturn(convertedGroup3);

        // Then
        var actualGroups = userAccountGroupService.getGroupsManagedByCurrentUser();
        assertThat(actualGroups).containsExactlyInAnyOrder(convertedGroup1, convertedGroup2, convertedGroup3);
    }

}
