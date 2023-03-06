package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {

    private final ProjectScopeMapper projectScopeMapper;

    public UserMapper(ProjectScopeMapper projectScopeMapper) {
        this.projectScopeMapper = projectScopeMapper;
    }

    /**
     * Convert the current ({@link User}) into a ({@link UserAccount}).
     * @param currentUser the (current) {@link User} to convert
     * @return the converted {@link UserAccount}
     */
    public UserAccount getCurrentUserAccountFromCurrentUser(@NonNull User currentUser) {
        var userAccount = getUserAccountWithoutScope(currentUser);

        var convertedScopes = projectScopeMapper.getCurrentUserAccountScopesFromCurrentUser(currentUser);
        userAccount.setScopes(convertedScopes);

        return userAccount;
    }

    private UserAccount getUserAccountWithoutScope(@NonNull User targetUser) {
        var providerName = targetUser.getProviderName();
        var login = targetUser.getLogin();

        var userAccount = new UserAccount(providerName, login);

        var userProfile = targetUser.getProfile() != null ? targetUser.getProfile() : UserProfile.SCOPED_USER;
        var userAccountProfile = UserAccountProfile.valueOf(userProfile.name());
        userAccount.setProfile(userAccountProfile);

        var firstName = targetUser.getFirstName();
        firstName.ifPresent(userAccount::setFirstName);

        var lastName = targetUser.getLastName();
        lastName.ifPresent(userAccount::setLastName);

        var email = targetUser.getEmail();
        email.ifPresent(userAccount::setEmail);

        var pictureUrl = targetUser.getPictureUrl();
        pictureUrl.ifPresent(userAccount::setPictureUrl);

        var defaultProjectCode = targetUser.getDefaultProject().map(Project::getCode);
        defaultProjectCode.ifPresent(userAccount::setDefaultProjectCode);

        var managedGroups = targetUser.getManagedGroups();
        List<Long> managedGroupIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(managedGroups)) {
            managedGroupIds = managedGroups.stream().map(UserGroup::getId).toList();
        }
        userAccount.setManagedGroupIds(managedGroupIds);

        var membershipGroups = targetUser.getMembershipGroups();
        List<Long> membershipGroupIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(membershipGroups)) {
            membershipGroupIds = membershipGroups.stream().map(UserGroup::getId).toList();
        }
        userAccount.setMembershipGroupIds(membershipGroupIds);
        return userAccount;
    }

    /**
     * Convert a ({@link User}) (other than the current user) into a ({@link UserAccount}).
     * Note that depending on its profile, the current user can fully or only partially access the targetUser scopes:
     * - A super admin or an auditor can access all the targetUser scopes
     * - A scoped user can only access scopes sharing the same project with the targetUser
     * @param targetUser the {@link User} to convert
     * @param currentUser the current {@link User} (accessing the target {@link User})
     * @return the converted {@link UserAccount}
     */
    public UserAccount getUserAccountFromAnotherUser(@NonNull User targetUser, @NonNull User currentUser) {
        var userAccount = getUserAccountWithoutScope(targetUser);

        var convertedScopes = projectScopeMapper.getUserAccountScopesFromAnotherUser(targetUser, currentUser);
        userAccount.setScopes(convertedScopes);

        return userAccount;
    }
}
