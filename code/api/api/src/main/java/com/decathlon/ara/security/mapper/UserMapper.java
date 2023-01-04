package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {

    /**
     * Convert a persisted user ({@link UserEntity}) into a user account ({@link UserAccount}).
     * @param persistedUser the persisted user
     * @return the converted user account
     */
    public UserAccount getUserAccountFromPersistedUser(@NonNull UserEntity persistedUser) {
        var providerName = persistedUser.getProviderName();
        var login = persistedUser.getLogin();

        var userAccount = new UserAccount(providerName, login);

        var persistedProfile = persistedUser.getProfile() != null ? persistedUser.getProfile() : UserEntity.UserEntityProfile.SCOPED_USER;
        var userAccountProfile = UserAccountProfile.valueOf(persistedProfile.name());
        userAccount.setProfile(userAccountProfile);

        var firstName = persistedUser.getFirstName();
        firstName.ifPresent(userAccount::setFirstName);

        var lastName = persistedUser.getLastName();
        lastName.ifPresent(userAccount::setLastName);

        var email = persistedUser.getEmail();
        email.ifPresent(userAccount::setEmail);

        var pictureUrl = persistedUser.getPictureUrl();
        pictureUrl.ifPresent(userAccount::setPictureUrl);

        var defaultProjectCode = persistedUser.getDefaultProject().map(Project::getCode);
        defaultProjectCode.ifPresent(userAccount::setDefaultProjectCode);

        var userAccountScopes = getUserAccountScopesFromPersistedUserRoles(persistedUser.getRolesOnProjectWhenScopedUser());
        userAccount.setScopes(userAccountScopes);

        return userAccount;
    }

    private List<UserAccountScope> getUserAccountScopesFromPersistedUserRoles(@NonNull List<UserEntityRoleOnProject> persistedUserRoles) {
        return CollectionUtils.isNotEmpty(persistedUserRoles) ?
                persistedUserRoles.stream()
                        .filter(role -> role.getProject() != null)
                        .filter(role -> role.getRole() != null)
                        .map(this::getUserAccountScopeFromPersistedUserRole)
                        .toList():
                new ArrayList<>();
    }

    private UserAccountScope getUserAccountScopeFromPersistedUserRole(@NonNull UserEntityRoleOnProject persistedUserRole) {
        var projectCode = persistedUserRole.getProject().getCode();
        var userAccountRole = getUserAccountScopeRoleFromScopedUserRoleOnProject(persistedUserRole.getRole());
        return new UserAccountScope(projectCode, userAccountRole);
    }

    private UserAccountScopeRole getUserAccountScopeRoleFromScopedUserRoleOnProject(@NonNull UserEntityRoleOnProject.ScopedUserRoleOnProject scopedUserRoleOnProject) {
        return UserAccountScopeRole.valueOf(scopedUserRoleOnProject.name());
    }
}
