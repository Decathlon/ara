package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.account.UserProjectScope;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.domain.security.member.user.group.UserGroupProjectScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Component
public class ProjectScopeMapper {

    /**
     * Convert all the {@link UserProjectScope} actually applied to the current {@link User}, i.e. scopes matching:
     * - projects inherited from its own and from the groups he is a member of,
     * - the higher roles
     * @param currentUser the current {@link User}
     * @return a list of {@link UserAccountScope}
     */
    public List<UserAccountScope> getCurrentUserAccountScopesFromCurrentUser(@NonNull User currentUser) {
        return getActuallyAppliedUserAccountScopesFromUser(currentUser);
    }

    private List<UserAccountScope> getActuallyAppliedUserAccountScopesFromUser(@NonNull User user) {
        var userProfile = user.getProfile();
        if (UserProfile.SUPER_ADMIN.equals(userProfile) || UserProfile.AUDITOR.equals(userProfile)) {
            return new ArrayList<>();
        }

        var scopesFromUser = CollectionUtils.isEmpty(user.getScopes()) ? new ArrayList<UserProjectScope>() : user.getScopes();
        var allGroupsTheUserIsAMemberOf = CollectionUtils.isEmpty(user.getMembershipGroups()) ? new HashSet<UserGroup>() : user.getMembershipGroups();
        var scopesFromGroupsStream = allGroupsTheUserIsAMemberOf.stream()
                .map(UserGroup::getScopes)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream);
        return Stream.concat(scopesFromUser.stream(), scopesFromGroupsStream)
                .filter(scope -> Objects.nonNull(scope.getProject()))
                .filter(scope -> Objects.nonNull(scope.getRole()))
                .map(ProjectScopeMapper::getUserAccountScopeFromProjectScope)
                .collect(groupingBy(UserAccountScope::getProject, minBy(Comparator.comparing(UserAccountScope::getRole))))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static UserAccountScope getUserAccountScopeFromProjectScope(@NonNull ProjectScope projectScope) {
        var projectCode = projectScope.getProject().getCode();
        var userAccountRole = getUserAccountScopeRoleFromProjectRole(projectScope.getRole());
        return new UserAccountScope(projectCode, userAccountRole);
    }

    private static UserAccountScopeRole getUserAccountScopeRoleFromProjectRole(@NonNull ProjectRole projectRole) {
        return UserAccountScopeRole.valueOf(projectRole.name());
    }

    /**
     * Convert all the {@link UserProjectScope} actually applied to a target {@link User}, i.e. scopes matching:
     * - projects inherited from its own and from the groups he is a member of,
     * - the higher roles
     * Please note that the result depends on the current {@link UserProfile}:
     * - a super admin and an auditor can access all the target user scopes
     * - a scoped user can only access the scopes that share the same projects
     * @param targetUser the target {@link User}
     * @param currentUser the current {@link User}
     * @return a list of {@link UserAccountScope}
     */
    public List<UserAccountScope> getUserAccountScopesFromAnotherUser(@NonNull User targetUser, @NonNull User currentUser) {
        var targetScopes = getActuallyAppliedUserAccountScopesFromUser(targetUser);
        return getTheTargetUserAccountScopesTheCurrentUserCanAccess(currentUser, targetScopes);
    }

    private List<UserAccountScope> getTheTargetUserAccountScopesTheCurrentUserCanAccess(@NonNull User currentUser, @NonNull List<UserAccountScope> targetScopes) {
        var currentUserProfile = currentUser.getProfile();
        if (UserProfile.SUPER_ADMIN.equals(currentUserProfile) || UserProfile.AUDITOR.equals(currentUserProfile)) {
            return targetScopes;
        }

        var scopesFromCurrentUser = CollectionUtils.isEmpty(currentUser.getScopes()) ? new ArrayList<UserProjectScope>() : currentUser.getScopes();
        var allGroupsTheCurrentUserIsAMemberOf = CollectionUtils.isEmpty(currentUser.getMembershipGroups()) ? new HashSet<UserGroup>() : currentUser.getMembershipGroups();
        var projectCodesFromCurrentUserScopesStream = scopesFromCurrentUser.stream()
                .map(ProjectScope::getProject)
                .filter(Objects::nonNull)
                .map(Project::getCode);
        var projectCodesFromCurrentUserGroupScopesStream = allGroupsTheCurrentUserIsAMemberOf.stream()
                .map(UserGroup::getScopes)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(ProjectScope::getProject)
                .filter(Objects::nonNull)
                .map(Project::getCode);
        var allTheProjectCodesTheCurrentUserCanAccess = Stream.concat(projectCodesFromCurrentUserScopesStream, projectCodesFromCurrentUserGroupScopesStream).filter(StringUtils::isNotBlank).distinct().toList();
        return targetScopes.stream().filter(scope -> allTheProjectCodesTheCurrentUserCanAccess.contains(scope.getProject())).toList();
    }

    /**
     * Convert all the {@link UserGroupProjectScope} of a target {@link UserGroup}.
     * Please note that the result depends on the current {@link UserProfile}:
     * - a super admin and an auditor can access all the target group scopes
     * - a scoped user can only access the scopes that share the same projects
     * @param targetGroup the target {@link UserGroup}
     * @param currentUser the current {@link User}
     * @return a list of {@link UserAccountScope}
     */
    public List<UserAccountScope> getUserAccountScopesFromUserGroup(@NonNull UserGroup targetGroup, @NonNull User currentUser) {
        var allGroupScopes = CollectionUtils.isEmpty(targetGroup.getScopes()) ? new ArrayList<UserGroupProjectScope>() : targetGroup.getScopes();
        var allConvertedGroupScopes = allGroupScopes.stream().map(ProjectScopeMapper::getUserAccountScopeFromProjectScope).toList();

        var currentUserProfile = currentUser.getProfile();
        if (UserProfile.SUPER_ADMIN.equals(currentUserProfile) || UserProfile.AUDITOR.equals(currentUserProfile)) {
            return allConvertedGroupScopes;
        }

        return getTheTargetUserAccountScopesTheCurrentUserCanAccess(currentUser, allConvertedGroupScopes);
    }
}
