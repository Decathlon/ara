package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.account.UserProjectScope;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final ProjectScopeMapper projectScopeMapper;

    public UserMapper(ProjectScopeMapper projectScopeMapper) {
        this.projectScopeMapper = projectScopeMapper;
    }

    /**
     * Convert a ({@link User}) into a ({@link UserAccount}).
     * @param targetUser the {@link User} to convert
     * @return the converted {@link UserAccount}
     */
    public UserAccount getFullScopeAccessUserAccountFromUser(@NonNull User targetUser) {
        var userAccount = getUserAccountWithoutScope(targetUser);

        var convertedScopes = getUserAccountScopesFromUserProjectScopes(targetUser.getScopes());
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

    private List<UserAccountScope> getUserAccountScopesFromUserProjectScopes(Collection<UserProjectScope> userProjectScopes) {
        var projectScopes = userProjectScopes.stream()
                .map(ProjectScope.class::cast)
                .collect(Collectors.toSet());
        return projectScopeMapper.getUserAccountScopesFromProjectScopes(projectScopes);
    }

    /**
     * Convert a ({@link User}) into a ({@link UserAccount}).
     * Note that depending on its profile, the readUser can fully or only partially access the targetUser scopes:
     * - A super admin or an auditor can access all the targetUser scopes
     * - A scoped readUser can only access scopes sharing the same project with the targetUser
     * @param targetUser the {@link User} to convert
     * @param readUser the {@link User} accessing the target {@link User}
     * @return the converted {@link UserAccount}
     */
    public UserAccount getPartialScopeAccessUserAccountFromUser(@NonNull User targetUser, @NonNull User readUser) {
        var userAccount = getUserAccountWithoutScope(targetUser);

        var readProfile = readUser.getProfile();

        if (UserProfile.SCOPED_USER.equals(readProfile)) {

            var allowedScopeProjectCodes = readUser.getScopes().stream().map(ProjectScope::getProject).map(Project::getCode).toList();
            var convertedScopes = getUserAccountScopesFromUserProjectScopesAndAllowedScopeProjectCodes(targetUser.getScopes(), allowedScopeProjectCodes);
            userAccount.setScopes(convertedScopes);

            return userAccount;
        }
        var convertedScopes = getUserAccountScopesFromUserProjectScopes(targetUser.getScopes());
        userAccount.setScopes(convertedScopes);

        return userAccount;
    }

    private List<UserAccountScope> getUserAccountScopesFromUserProjectScopesAndAllowedScopeProjectCodes(
            Collection<UserProjectScope> userProjectScopes,
            Collection<String> allowedScopeProjectCodes
    ) {
        var projectScopes = userProjectScopes.stream()
                .map(ProjectScope.class::cast)
                .filter(scope -> allowedScopeProjectCodes.contains(scope.getProject().getCode()))
                .collect(Collectors.toSet());
        return projectScopeMapper.getUserAccountScopesFromProjectScopes(projectScopes);
    }
}
