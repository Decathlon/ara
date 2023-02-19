package com.decathlon.ara.security.service.user.group;

import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.repository.security.member.user.group.UserGroupRepository;
import com.decathlon.ara.security.dto.user.group.UserAccountGroup;
import com.decathlon.ara.security.mapper.UserGroupMapper;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.decathlon.ara.Entities.GROUP;

@Service
@Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
}
