package com.decathlon.ara.security.service.member.user.group;

import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.repository.security.member.user.group.UserGroupRepository;
import com.decathlon.ara.security.dto.user.group.UserAccountGroup;
import com.decathlon.ara.security.mapper.UserGroupMapper;
import com.decathlon.ara.security.service.member.user.account.UserAccountService;
import com.decathlon.ara.security.service.member.user.account.UserSessionService;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.decathlon.ara.Entities.GROUP;

@Service
public class UserAccountGroupService {

    private final UserAccountService userAccountService;

    private final UserSessionService userSessionService;

    private final UserGroupRepository groupRepository;

    private final UserGroupMapper groupMapper;

    public UserAccountGroupService(
            UserAccountService userAccountService,
            UserSessionService userSessionService,
            UserGroupRepository groupRepository,
            UserGroupMapper groupMapper
    ) {
        this.userAccountService = userAccountService;
        this.userSessionService = userSessionService;
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
    }

    /**
     * Create a {@link UserAccountGroup}
     * @param group the group to create
     * @return the created group
     * @throws ForbiddenException thrown if this operation failed
     */
    public UserAccountGroup createGroup(@NonNull UserAccountGroup group) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "create user account group");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);

        var groupName = group.getName();
        if (StringUtils.isBlank(groupName)) {
            throw exception;
        }

        var groupNameAlreadyExists = groupRepository.existsByName(groupName);
        if (groupNameAlreadyExists) {
            throw exception;
        }

        var groupToSave = new UserGroup(groupName, currentUser);
        groupToSave.setDescription(group.getDescription());
        groupToSave.addManager(currentUser);

        var savedGroup = groupRepository.save(groupToSave);
        userSessionService.refreshCurrentUserAuthorities();

        return groupMapper.getUserAccountGroupFromUserGroup(savedGroup, currentUser);
    }

    /**
     * Update a {@link UserAccountGroup}
     * @param group the group to update
     * @return the updated group
     * @throws ForbiddenException thrown if this operation failed
     */
    public UserAccountGroup updateGroup(@NonNull UserAccountGroup group) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "update user account group");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);

        var groupId = group.getId();
        var groupName = group.getName();
        if (groupId == null || StringUtils.isBlank(groupName)) {
            throw exception;
        }

        var groupToUpdate = groupRepository.findById(groupId).orElseThrow(() -> exception);

        groupToUpdate.setName(groupName);
        groupToUpdate.setDescription(group.getDescription());
        groupToUpdate.setUpdateDate(ZonedDateTime.now());
        groupToUpdate.setUpdateUser(currentUser);

        var updatedGroup = groupRepository.save(groupToUpdate);
        userSessionService.refreshCurrentUserAuthorities();

        return groupMapper.getUserAccountGroupFromUserGroup(updatedGroup, currentUser);
    }

    /**
     * Delete a {@link UserAccountGroup} from a group id
     * @param groupId the group id
     */
    public void deleteGroup(@NonNull Long groupId) throws ForbiddenException {
        groupRepository.deleteById(groupId);
        userSessionService.refreshCurrentUserAuthorities();
    }

    /**
     * Get all {@link UserAccountGroup}
     * @return the groups
     * @throws ForbiddenException thrown if this operation failed
     */
    public List<UserAccountGroup> getAllUserAccountGroupsForCurrentProviderName() throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "get all user account groups for current provider");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);
        return groupRepository.findAllByProviderName(currentUser.getProviderName())
                .stream()
                .map(group -> groupMapper.getUserAccountGroupFromUserGroup(group, currentUser))
                .toList();
    }

    /**
     * Get a list of {@link UserAccountGroup} containing a given user
     * @param userLogin the user login
     * @return the groups
     * @throws ForbiddenException thrown if this operation failed
     */
    public List<UserAccountGroup> getGroupsContainingUser(@NonNull String userLogin) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "get user account groups containing member");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);

        if (StringUtils.isBlank(userLogin)) {
            throw exception;
        }

        return getGroupsContainingUser(currentUser, Optional.of(userLogin));
    }

    /**
     * Get a list of {@link UserAccountGroup} containing the current user
     * @return the groups
     * @throws ForbiddenException thrown if this operation failed
     */
    public List<UserAccountGroup> getGroupsContainingCurrentUser() throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "get user account groups containing current user");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);
        return getGroupsContainingUser(currentUser, Optional.empty());
    }

    private List<UserAccountGroup> getGroupsContainingUser(User currentUser, Optional<String> userLogin) {
        return groupRepository.findAllByProviderNameAndMembersLogin(currentUser.getProviderName(), userLogin.orElse(currentUser.getLogin()))
                .stream()
                .map(group -> groupMapper.getUserAccountGroupFromUserGroup(group, currentUser))
                .toList();
    }

    /**
     * Get a list of {@link UserAccountGroup} managed by a given user
     * @param userLogin the user login
     * @return the groups
     * @throws ForbiddenException thrown if this operation failed
     */
    public List<UserAccountGroup> getGroupsManagedByUser(@NonNull String userLogin) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "get managed user account groups");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);

        if (StringUtils.isBlank(userLogin)) {
            throw exception;
        }

        return getGroupsManagedByUser(currentUser, Optional.of(userLogin));
    }

    /**
     * Get a list of {@link UserAccountGroup} managed by the current user
     * @return the groups
     * @throws ForbiddenException thrown if this operation failed
     */
    public List<UserAccountGroup> getGroupsManagedByCurrentUser() throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "get user account groups managed by current user");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);
        return this.getGroupsManagedByUser(currentUser, Optional.empty());
    }

    private List<UserAccountGroup> getGroupsManagedByUser(User currentUser, Optional<String> userLogin) {
        return groupRepository.findAllByProviderNameAndManagersLogin(currentUser.getProviderName(), userLogin.orElse(currentUser.getLogin()))
                .stream()
                .map(group -> groupMapper.getUserAccountGroupFromUserGroup(group, currentUser))
                .toList();
    }

    /**
     * Add a {@link User} member to a {@link UserGroup}
     * @param userLogin the member user login
     * @param groupId the target group id
     * @return updated {@link UserAccountGroup}
     * @throws ForbiddenException thrown if this operation failed
     */
    public UserAccountGroup addMemberToGroup(@NonNull String userLogin, @NonNull Long groupId) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "add member to group");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);
        var targetUser = userAccountService.getUserFromLogin(userLogin).orElseThrow(() -> exception);
        var targetUserProfile = targetUser.getProfile();
        if (UserProfile.SUPER_ADMIN.equals(targetUserProfile) || UserProfile.AUDITOR.equals(targetUserProfile)) {
            throw exception;
        }
        var targetGroup = groupRepository.findById(groupId).orElseThrow(() -> exception);

        var userWasAdded = targetGroup.getMembers().add(targetUser);
        if (userWasAdded) {
            targetGroup = groupRepository.save(targetGroup);
        }
        return groupMapper.getUserAccountGroupFromUserGroup(targetGroup, currentUser);
    }

    /**
     * Remove a {@link User} member from a {@link UserGroup}
     * @param userLogin the member user login
     * @param groupId the target group id
     * @throws ForbiddenException thrown if this operation failed
     */
    public void removeMemberFromGroup(@NonNull String userLogin, @NonNull Long groupId) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "remove member from group");

        var targetUser = userAccountService.getUserFromLogin(userLogin).orElseThrow(() -> exception);
        var targetUserProfile = targetUser.getProfile();
        if (UserProfile.SUPER_ADMIN.equals(targetUserProfile) || UserProfile.AUDITOR.equals(targetUserProfile)) {
            throw exception;
        }
        var targetGroup = groupRepository.findById(groupId).orElseThrow(() -> exception);

        var userWasRemoved = targetGroup.getMembers().remove(targetUser);
        if (userWasRemoved) {
            groupRepository.save(targetGroup);
        }
    }

    /**
     * Assign a manager to a {@link UserGroup}
     * @param userLogin the manager user login
     * @param groupId the target group id
     * @return updated {@link UserAccountGroup}
     * @throws ForbiddenException thrown if this operation failed
     */
    public UserAccountGroup addManagerToGroup(@NonNull String userLogin, @NonNull Long groupId) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "add manager to group");

        var currentUser = userAccountService.getCurrentUser().orElseThrow(() -> exception);
        var targetUser = userAccountService.getUserFromLogin(userLogin).orElseThrow(() -> exception);
        var targetUserProfile = targetUser.getProfile();
        if (UserProfile.SUPER_ADMIN.equals(targetUserProfile) || UserProfile.AUDITOR.equals(targetUserProfile)) {
            throw exception;
        }
        var targetGroup = groupRepository.findById(groupId).orElseThrow(() -> exception);

        var userWasAdded = targetGroup.getManagers().add(targetUser);
        if (userWasAdded) {
            targetGroup = groupRepository.save(targetGroup);
        }
        return groupMapper.getUserAccountGroupFromUserGroup(targetGroup, currentUser);
    }

    /**
     * Remove a manager from a {@link UserGroup}
     * @param userLogin the manager user login
     * @param groupId the target group id
     * @throws ForbiddenException thrown if this operation failed
     */
    public void removeManagerFromGroup(@NonNull String userLogin, @NonNull Long groupId) throws ForbiddenException {
        var exception = new ForbiddenException(GROUP, "remove manager from group");

        var targetUser = userAccountService.getUserFromLogin(userLogin).orElseThrow(() -> exception);
        var targetUserProfile = targetUser.getProfile();
        if (UserProfile.SUPER_ADMIN.equals(targetUserProfile) || UserProfile.AUDITOR.equals(targetUserProfile)) {
            throw exception;
        }
        var targetGroup = groupRepository.findById(groupId).orElseThrow(() -> exception);

        var userWasRemoved = targetGroup.getManagers().remove(targetUser);
        if (userWasRemoved) {
            groupRepository.save(targetGroup);
        }
    }
}
