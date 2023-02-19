package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.security.dto.user.group.UserAccountGroup;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class UserGroupMapper {

    private final UserMapper userMapper;

    private final ProjectScopeMapper projectScopeMapper;

    public UserGroupMapper(
            UserMapper userMapper,
            ProjectScopeMapper projectScopeMapper
    ) {
        this.userMapper = userMapper;
        this.projectScopeMapper = projectScopeMapper;
    }

    /**
     * Convert a {@link UserGroup} into a {@link UserAccountGroup}.
     * Note that the scopes displayed depends on the read user profile.
     * @param userGroup the {@link UserGroup} to convert
     * @param readUser the read {@link User}
     * @return the converted {@link UserAccountGroup}
     */
    public UserAccountGroup getUserAccountGroupFromUserGroup(@NonNull UserGroup userGroup, @NonNull User readUser) {
        var groupId = userGroup.getId();
        var groupName = userGroup.getName();

        var creationUserLogin = userGroup.getCreationUser() != null ? userGroup.getCreationUser().getLogin() : null;
        var creationDate = userGroup.getCreationDate() != null ? Date.from(userGroup.getCreationDate().toInstant()) : null;

        var convertedGroup = new UserAccountGroup(userGroup.getProviderName(), groupId, groupName, creationDate, creationUserLogin);
        convertedGroup.setDescription(userGroup.getDescription());

        var updateUser = userGroup.getUpdateUser();
        if (updateUser != null) {
            convertedGroup.setUpdateUserLogin(updateUser.getLogin());
        }

        var updateDate = userGroup.getUpdateDate();
        if (updateDate != null) {
            convertedGroup.setUpdateDate(Date.from(updateDate.toInstant()));
        }

        var memberScopes = userGroup.getScopes()
                .stream()
                .map(ProjectScope.class::cast)
                .collect(Collectors.toSet());
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromProjectScopes(memberScopes);
        convertedGroup.setScopes(convertedScopes);

        var convertedMembers = userGroup.getMembers()
                .stream()
                .map(member -> userMapper.getPartialScopeAccessUserAccountFromUser(member, readUser))
                .toList();
        convertedGroup.setMembers(convertedMembers);

        var convertedManagers = userGroup.getManagers()
                .stream()
                .map(manager -> userMapper.getPartialScopeAccessUserAccountFromUser(manager, readUser))
                .toList();
        convertedGroup.setManagers(convertedManagers);

        return convertedGroup;
    }
}
