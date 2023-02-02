package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.role.ProjectRole;
import com.decathlon.ara.domain.security.member.user.User;
import com.decathlon.ara.domain.security.member.user.UserProfile;
import com.decathlon.ara.domain.security.member.user.UserScope;
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
     * Convert a ({@link User}) into a ({@link UserAccount}).
     * @param user the user
     * @return the converted user account
     */
    public UserAccount getUserAccountFromUser(@NonNull User user) {
        var providerName = user.getProviderName();
        var login = user.getLogin();

        var userAccount = new UserAccount(providerName, login);

        var userProfile = user.getProfile() != null ? user.getProfile() : UserProfile.SCOPED_USER;
        var userAccountProfile = UserAccountProfile.valueOf(userProfile.name());
        userAccount.setProfile(userAccountProfile);

        var firstName = user.getFirstName();
        firstName.ifPresent(userAccount::setFirstName);

        var lastName = user.getLastName();
        lastName.ifPresent(userAccount::setLastName);

        var email = user.getEmail();
        email.ifPresent(userAccount::setEmail);

        var pictureUrl = user.getPictureUrl();
        pictureUrl.ifPresent(userAccount::setPictureUrl);

        var defaultProjectCode = user.getDefaultProject().map(Project::getCode);
        defaultProjectCode.ifPresent(userAccount::setDefaultProjectCode);

        var userAccountScopes = getUserAccountScopesFromUserScopes(user.getScopes());
        userAccount.setScopes(userAccountScopes);

        return userAccount;
    }

    private List<UserAccountScope> getUserAccountScopesFromUserScopes(@NonNull List<UserScope> userScopes) {
        return CollectionUtils.isNotEmpty(userScopes) ?
                userScopes.stream()
                        .filter(role -> role.getProject() != null)
                        .filter(role -> role.getRole() != null)
                        .map(this::getUserAccountScopeFromUserScope)
                        .toList():
                new ArrayList<>();
    }

    private UserAccountScope getUserAccountScopeFromUserScope(@NonNull UserScope userScope) {
        var projectCode = userScope.getProject().getCode();
        var userAccountRole = getUserAccountScopeRoleFromProjectRole(userScope.getRole());
        return new UserAccountScope(projectCode, userAccountRole);
    }

    private UserAccountScopeRole getUserAccountScopeRoleFromProjectRole(@NonNull ProjectRole projectRole) {
        return UserAccountScopeRole.valueOf(projectRole.name());
    }
}
