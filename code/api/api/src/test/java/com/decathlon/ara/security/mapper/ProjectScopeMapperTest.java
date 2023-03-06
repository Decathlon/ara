package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.account.UserProjectScope;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.domain.security.member.user.group.UserGroupProjectScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectScopeMapperTest {

    @InjectMocks
    private ProjectScopeMapper projectScopeMapper;

    @Test
    void getCurrentUserAccountScopesFromCurrentUser_returnEmptyList_whenCurrentUserScopesIsNull() {
        // Given
        var currentUser = mock(User.class);

        // When
        when(currentUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(currentUser.getScopes()).thenReturn(null);


        // Then
        var convertedScopes = projectScopeMapper.getCurrentUserAccountScopesFromCurrentUser(currentUser);
        assertThat(convertedScopes).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = UserProfile.class, names = {"SUPER_ADMIN", "AUDITOR"})
    void getCurrentUserAccountScopesFromCurrentUser_returnEmptyList_whenCurrentUserIsEitherSuperAdminOrAuditor(UserProfile currentUserProfile) {
        // Given
        var currentUser = mock(User.class);

        // When
        when(currentUser.getProfile()).thenReturn(currentUserProfile);

        // Then
        var convertedScopes = projectScopeMapper.getCurrentUserAccountScopesFromCurrentUser(currentUser);
        assertThat(convertedScopes).isEmpty();
    }

    @Test
    void getCurrentUserAccountScopesFromCurrentUser_returnScopesHavingMaximalRoles_whenCurrentUserIsScopedUserAndIsMemberOfAFewGroups() {
        // Given
        var currentUser = mock(User.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code-1";
        var project2 = mock(Project.class);
        var projectCode2 = "project-code-2";
        var project3 = mock(Project.class);
        var projectCode3 = "project-code-3";
        var project4 = mock(Project.class);
        var projectCode4 = "project-code-4";
        var project5 = mock(Project.class);
        var projectCode5 = "project-code-5";
        var project6 = mock(Project.class);
        var projectCode6 = "project-code-6";

        var userScope1 = mock(UserProjectScope.class);
        var userScope2 = mock(UserProjectScope.class);
        var userScope3 = mock(UserProjectScope.class);
        var userScope4 = mock(UserProjectScope.class);
        var userScopes = Set.of(userScope1, userScope2, userScope3, userScope4);

        var group1 = mock(UserGroup.class);
        var group1Scope1 = mock(UserGroupProjectScope.class);
        var group1Scopes = Set.of(group1Scope1);

        var group2 = mock(UserGroup.class);
        var group2Scope1 = mock(UserGroupProjectScope.class);
        var group2Scope2 = mock(UserGroupProjectScope.class);
        var group2Scope3 = mock(UserGroupProjectScope.class);
        var group2Scopes = Set.of(group2Scope1, group2Scope2, group2Scope3);

        var group3 = mock(UserGroup.class);
        var group3Scope1 = mock(UserGroupProjectScope.class);
        var group3Scope2 = mock(UserGroupProjectScope.class);
        var group3Scopes = Set.of(group3Scope1, group3Scope2);

        var group4 = mock(UserGroup.class);

        var groups = Set.of(group1, group2, group3, group4);

        // When
        when(currentUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(currentUser.getScopes()).thenReturn(userScopes);
        when(userScope1.getProject()).thenReturn(project1);
        when(userScope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(userScope2.getProject()).thenReturn(project2);
        when(userScope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(userScope3.getProject()).thenReturn(project3);
        when(userScope3.getRole()).thenReturn(ProjectRole.ADMIN);
        when(userScope4.getProject()).thenReturn(project4);
        when(userScope4.getRole()).thenReturn(ProjectRole.MAINTAINER);

        when(currentUser.getMembershipGroups()).thenReturn(groups);
        when(group1.getScopes()).thenReturn(group1Scopes);
        when(group1Scope1.getProject()).thenReturn(project4);
        when(group1Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(group2.getScopes()).thenReturn(group2Scopes);
        when(group2Scope1.getProject()).thenReturn(project2);
        when(group2Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(group2Scope2.getProject()).thenReturn(project4);
        when(group2Scope2.getRole()).thenReturn(ProjectRole.ADMIN);
        when(group2Scope3.getProject()).thenReturn(project5);
        when(group2Scope3.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(group3.getScopes()).thenReturn(group3Scopes);
        when(group3Scope1.getProject()).thenReturn(project3);
        when(group3Scope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(group3Scope2.getProject()).thenReturn(project6);
        when(group3Scope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(group4.getScopes()).thenReturn(null);

        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);

        // Then
        var convertedScopes = projectScopeMapper.getCurrentUserAccountScopesFromCurrentUser(currentUser);
        assertThat(convertedScopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode2, UserAccountScopeRole.MEMBER),
                        tuple(projectCode3, UserAccountScopeRole.ADMIN),
                        tuple(projectCode4, UserAccountScopeRole.ADMIN),
                        tuple(projectCode5, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode6, UserAccountScopeRole.MEMBER)
                );
    }

    @Test
    void getUserAccountScopesFromAnotherUser_returnEmptyList_whenTargetUserScopesIsNull() {
        // Given
        var targetUser = mock(User.class);
        var currentUser = mock(User.class);

        // When
        when(targetUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(targetUser.getScopes()).thenReturn(null);


        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromAnotherUser(targetUser, currentUser);
        assertThat(convertedScopes).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = UserProfile.class, names = {"SUPER_ADMIN", "AUDITOR"})
    void getUserAccountScopesFromAnotherUser_returnEmptyList_whenTargetUserIsEitherSuperAdminOrAuditor(UserProfile targetUserProfile) {
        // Given
        var targetUser = mock(User.class);
        var currentUser = mock(User.class);

        // When
        when(targetUser.getProfile()).thenReturn(targetUserProfile);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromAnotherUser(targetUser, currentUser);
        assertThat(convertedScopes).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = UserProfile.class, names = {"SUPER_ADMIN", "AUDITOR"})
    void getUserAccountScopesFromAnotherUser_returnAllScopesHavingMaximalRoles_whenTargetUserIsScopedUserAndIsMemberOfAFewGroupsButCurrentUserIsEitherSuperAdminOrAuditor(UserProfile currentUserProfile) {
        // Given
        var targetUser = mock(User.class);
        var currentUser = mock(User.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code-1";
        var project2 = mock(Project.class);
        var projectCode2 = "project-code-2";
        var project3 = mock(Project.class);
        var projectCode3 = "project-code-3";
        var project4 = mock(Project.class);
        var projectCode4 = "project-code-4";
        var project5 = mock(Project.class);
        var projectCode5 = "project-code-5";
        var project6 = mock(Project.class);
        var projectCode6 = "project-code-6";

        var targetUserScope1 = mock(UserProjectScope.class);
        var targetUserScope2 = mock(UserProjectScope.class);
        var targetUserScope3 = mock(UserProjectScope.class);
        var targetUserScope4 = mock(UserProjectScope.class);
        var targetUserScopes = Set.of(targetUserScope1, targetUserScope2, targetUserScope3, targetUserScope4);

        var targetUserGroup1 = mock(UserGroup.class);
        var targetUserGroup1Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup1Scopes = Set.of(targetUserGroup1Scope1);

        var targetUserGroup2 = mock(UserGroup.class);
        var targetUserGroup2Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scope2 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scope3 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scopes = Set.of(targetUserGroup2Scope1, targetUserGroup2Scope2, targetUserGroup2Scope3);

        var targetUserGroup3 = mock(UserGroup.class);
        var targetUserGroup3Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup3Scope2 = mock(UserGroupProjectScope.class);
        var targetUserGroup3Scopes = Set.of(targetUserGroup3Scope1, targetUserGroup3Scope2);

        var targetUserGroup4 = mock(UserGroup.class);

        var targetUserGroups = Set.of(targetUserGroup1, targetUserGroup2, targetUserGroup3, targetUserGroup4);

        // When
        when(currentUser.getProfile()).thenReturn(currentUserProfile);

        when(targetUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(targetUser.getScopes()).thenReturn(targetUserScopes);
        when(targetUserScope1.getProject()).thenReturn(project1);
        when(targetUserScope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserScope2.getProject()).thenReturn(project2);
        when(targetUserScope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserScope3.getProject()).thenReturn(project3);
        when(targetUserScope3.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetUserScope4.getProject()).thenReturn(project4);
        when(targetUserScope4.getRole()).thenReturn(ProjectRole.MAINTAINER);

        when(targetUser.getMembershipGroups()).thenReturn(targetUserGroups);
        when(targetUserGroup1.getScopes()).thenReturn(targetUserGroup1Scopes);
        when(targetUserGroup1Scope1.getProject()).thenReturn(project4);
        when(targetUserGroup1Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup2.getScopes()).thenReturn(targetUserGroup2Scopes);
        when(targetUserGroup2Scope1.getProject()).thenReturn(project2);
        when(targetUserGroup2Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup2Scope2.getProject()).thenReturn(project4);
        when(targetUserGroup2Scope2.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetUserGroup2Scope3.getProject()).thenReturn(project5);
        when(targetUserGroup2Scope3.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserGroup3.getScopes()).thenReturn(targetUserGroup3Scopes);
        when(targetUserGroup3Scope1.getProject()).thenReturn(project3);
        when(targetUserGroup3Scope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserGroup3Scope2.getProject()).thenReturn(project6);
        when(targetUserGroup3Scope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup4.getScopes()).thenReturn(null);

        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromAnotherUser(targetUser, currentUser);
        assertThat(convertedScopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode2, UserAccountScopeRole.MEMBER),
                        tuple(projectCode3, UserAccountScopeRole.ADMIN),
                        tuple(projectCode4, UserAccountScopeRole.ADMIN),
                        tuple(projectCode5, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode6, UserAccountScopeRole.MEMBER)
                );
    }

    @Test
    void getUserAccountScopesFromAnotherUser_returnEmptyList_whenTargetUserAndCurrentUserAreScopedUsersButCurrentUserHasNoScopes() {
        // Given
        var project1 = mock(Project.class);
        var projectCode1 = "project-code-1";
        var project2 = mock(Project.class);
        var projectCode2 = "project-code-2";
        var project3 = mock(Project.class);
        var projectCode3 = "project-code-3";
        var project4 = mock(Project.class);
        var projectCode4 = "project-code-4";
        var project5 = mock(Project.class);
        var projectCode5 = "project-code-5";
        var project6 = mock(Project.class);
        var projectCode6 = "project-code-6";

        // target user
        var targetUser = mock(User.class);

        var targetUserScope1 = mock(UserProjectScope.class);
        var targetUserScope2 = mock(UserProjectScope.class);
        var targetUserScope3 = mock(UserProjectScope.class);
        var targetUserScope4 = mock(UserProjectScope.class);
        var targetUserScopes = Set.of(targetUserScope1, targetUserScope2, targetUserScope3, targetUserScope4);

        var targetUserGroup1 = mock(UserGroup.class);
        var targetUserGroup1Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup1Scopes = Set.of(targetUserGroup1Scope1);

        var targetUserGroup2 = mock(UserGroup.class);
        var targetUserGroup2Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scope2 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scope3 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scopes = Set.of(targetUserGroup2Scope1, targetUserGroup2Scope2, targetUserGroup2Scope3);

        var targetUserGroup3 = mock(UserGroup.class);
        var targetUserGroup3Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup3Scope2 = mock(UserGroupProjectScope.class);
        var targetUserGroup3Scopes = Set.of(targetUserGroup3Scope1, targetUserGroup3Scope2);

        var targetUserGroup4 = mock(UserGroup.class);

        var targetUserGroups = Set.of(targetUserGroup1, targetUserGroup2, targetUserGroup3, targetUserGroup4);

        // current user
        var currentUser = mock(User.class);

        // When
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);

        // target user
        when(targetUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(targetUser.getScopes()).thenReturn(targetUserScopes);
        when(targetUserScope1.getProject()).thenReturn(project1);
        when(targetUserScope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserScope2.getProject()).thenReturn(project2);
        when(targetUserScope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserScope3.getProject()).thenReturn(project3);
        when(targetUserScope3.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetUserScope4.getProject()).thenReturn(project4);
        when(targetUserScope4.getRole()).thenReturn(ProjectRole.MAINTAINER);

        when(targetUser.getMembershipGroups()).thenReturn(targetUserGroups);
        when(targetUserGroup1.getScopes()).thenReturn(targetUserGroup1Scopes);
        when(targetUserGroup1Scope1.getProject()).thenReturn(project4);
        when(targetUserGroup1Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup2.getScopes()).thenReturn(targetUserGroup2Scopes);
        when(targetUserGroup2Scope1.getProject()).thenReturn(project2);
        when(targetUserGroup2Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup2Scope2.getProject()).thenReturn(project4);
        when(targetUserGroup2Scope2.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetUserGroup2Scope3.getProject()).thenReturn(project5);
        when(targetUserGroup2Scope3.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserGroup3.getScopes()).thenReturn(targetUserGroup3Scopes);
        when(targetUserGroup3Scope1.getProject()).thenReturn(project3);
        when(targetUserGroup3Scope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserGroup3Scope2.getProject()).thenReturn(project6);
        when(targetUserGroup3Scope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup4.getScopes()).thenReturn(null);

        // current user
        when(currentUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(currentUser.getScopes()).thenReturn(null);
        when(currentUser.getMembershipGroups()).thenReturn(null);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromAnotherUser(targetUser, currentUser);
        assertThat(convertedScopes).isEmpty();
    }

    @Test
    void getUserAccountScopesFromAnotherUser_returnSharedScopesHavingMaximalRoles_whenTargetUserIsScopedUserAndIsMemberOfAFewGroupsAndCurrentUserIsAlsoScopedUser() {
        // Given
        var project1 = mock(Project.class);
        var projectCode1 = "project-code-1";
        var project2 = mock(Project.class);
        var projectCode2 = "project-code-2";
        var project3 = mock(Project.class);
        var projectCode3 = "project-code-3";
        var project4 = mock(Project.class);
        var projectCode4 = "project-code-4";
        var project5 = mock(Project.class);
        var projectCode5 = "project-code-5";
        var project6 = mock(Project.class);
        var projectCode6 = "project-code-6";
        var project7 = mock(Project.class);
        var projectCode7 = "project-code-7";
        var project8 = mock(Project.class);
        var projectCode8 = "project-code-8";

        // target user
        var targetUser = mock(User.class);

        var targetUserScope1 = mock(UserProjectScope.class);
        var targetUserScope2 = mock(UserProjectScope.class);
        var targetUserScope3 = mock(UserProjectScope.class);
        var targetUserScope4 = mock(UserProjectScope.class);
        var targetUserScopes = Set.of(targetUserScope1, targetUserScope2, targetUserScope3, targetUserScope4);

        var targetUserGroup1 = mock(UserGroup.class);
        var targetUserGroup1Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup1Scopes = Set.of(targetUserGroup1Scope1);

        var targetUserGroup2 = mock(UserGroup.class);
        var targetUserGroup2Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scope2 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scope3 = mock(UserGroupProjectScope.class);
        var targetUserGroup2Scopes = Set.of(targetUserGroup2Scope1, targetUserGroup2Scope2, targetUserGroup2Scope3);

        var targetUserGroup3 = mock(UserGroup.class);
        var targetUserGroup3Scope1 = mock(UserGroupProjectScope.class);
        var targetUserGroup3Scope2 = mock(UserGroupProjectScope.class);
        var targetUserGroup3Scopes = Set.of(targetUserGroup3Scope1, targetUserGroup3Scope2);

        var targetUserGroup4 = mock(UserGroup.class);

        var targetUserGroups = Set.of(targetUserGroup1, targetUserGroup2, targetUserGroup3, targetUserGroup4);

        // current user
        var currentUser = mock(User.class);

        var currentUserScope1 = mock(UserProjectScope.class);
        var currentUserScope2 = mock(UserProjectScope.class);
        var currentUserScope3 = mock(UserProjectScope.class);
        var currentUserScopes = Set.of(currentUserScope1, currentUserScope2, currentUserScope3);

        var currentUserGroup1 = mock(UserGroup.class);
        var currentUserGroup1Scope1 = mock(UserGroupProjectScope.class);
        var currentUserGroup1Scope2 = mock(UserGroupProjectScope.class);
        var currentUserGroup1Scope3 = mock(UserGroupProjectScope.class);
        var currentUserGroup1Scopes = Set.of(currentUserGroup1Scope1, currentUserGroup1Scope2, currentUserGroup1Scope3);

        var currentUserGroup2 = mock(UserGroup.class);
        var currentUserGroup2Scope1 = mock(UserGroupProjectScope.class);
        var currentUserGroup2Scope2 = mock(UserGroupProjectScope.class);
        var currentUserGroup2Scopes = Set.of(currentUserGroup2Scope1, currentUserGroup2Scope2);

        var currentUserGroup3 = mock(UserGroup.class);
        var currentUserGroup3Scope1 = mock(UserGroupProjectScope.class);
        var currentUserGroup3Scopes = Set.of(currentUserGroup3Scope1);

        var currentUserGroup4 = mock(UserGroup.class);

        var currentUserGroups = Set.of(currentUserGroup1, currentUserGroup2, currentUserGroup3, currentUserGroup4);

        // When
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);
        when(project7.getCode()).thenReturn(projectCode7);
        when(project8.getCode()).thenReturn(projectCode8);

        // target user
        when(targetUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(targetUser.getScopes()).thenReturn(targetUserScopes);
        when(targetUserScope1.getProject()).thenReturn(project1);
        when(targetUserScope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserScope2.getProject()).thenReturn(project2);
        when(targetUserScope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserScope3.getProject()).thenReturn(project3);
        when(targetUserScope3.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetUserScope4.getProject()).thenReturn(project4);
        when(targetUserScope4.getRole()).thenReturn(ProjectRole.MAINTAINER);

        when(targetUser.getMembershipGroups()).thenReturn(targetUserGroups);
        when(targetUserGroup1.getScopes()).thenReturn(targetUserGroup1Scopes);
        when(targetUserGroup1Scope1.getProject()).thenReturn(project4);
        when(targetUserGroup1Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup2.getScopes()).thenReturn(targetUserGroup2Scopes);
        when(targetUserGroup2Scope1.getProject()).thenReturn(project2);
        when(targetUserGroup2Scope1.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup2Scope2.getProject()).thenReturn(project4);
        when(targetUserGroup2Scope2.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetUserGroup2Scope3.getProject()).thenReturn(project5);
        when(targetUserGroup2Scope3.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserGroup3.getScopes()).thenReturn(targetUserGroup3Scopes);
        when(targetUserGroup3Scope1.getProject()).thenReturn(project3);
        when(targetUserGroup3Scope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetUserGroup3Scope2.getProject()).thenReturn(project6);
        when(targetUserGroup3Scope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetUserGroup4.getScopes()).thenReturn(null);

        // current user
        when(currentUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(currentUser.getScopes()).thenReturn(currentUserScopes);
        when(currentUserScope1.getProject()).thenReturn(project1);
        when(currentUserScope2.getProject()).thenReturn(project2);
        when(currentUserScope3.getProject()).thenReturn(project7);

        when(currentUser.getMembershipGroups()).thenReturn(currentUserGroups);
        when(currentUserGroup1.getScopes()).thenReturn(currentUserGroup1Scopes);
        when(currentUserGroup1Scope1.getProject()).thenReturn(project1);
        when(currentUserGroup1Scope2.getProject()).thenReturn(project3);
        when(currentUserGroup1Scope3.getProject()).thenReturn(project7);
        when(currentUserGroup2.getScopes()).thenReturn(currentUserGroup2Scopes);
        when(currentUserGroup2Scope1.getProject()).thenReturn(project3);
        when(currentUserGroup2Scope2.getProject()).thenReturn(project8);
        when(currentUserGroup3.getScopes()).thenReturn(currentUserGroup3Scopes);
        when(currentUserGroup3Scope1.getProject()).thenReturn(project1);
        when(currentUserGroup4.getScopes()).thenReturn(null);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromAnotherUser(targetUser, currentUser);
        assertThat(convertedScopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode2, UserAccountScopeRole.MEMBER),
                        tuple(projectCode3, UserAccountScopeRole.ADMIN)
                );
    }

    @Test
    void getUserAccountScopesFromUserGroup_returnEmptyList_whenTargetGroupHasNoScopes() {
        // Given
        var targetGroup = mock(UserGroup.class);
        var currentUser = mock(User.class);

        // When
        when(targetGroup.getScopes()).thenReturn(null);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromUserGroup(targetGroup, currentUser);
        assertThat(convertedScopes).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = UserProfile.class, names = {"SUPER_ADMIN", "AUDITOR"})
    void getUserAccountScopesFromUserGroup_returnAllGroupScopes_whenCurrentUserIsEitherSuperAdminOrAuditor(UserProfile currentUserProfile) {
        // Given
        var targetGroup = mock(UserGroup.class);
        var currentUser = mock(User.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code-1";
        var project2 = mock(Project.class);
        var projectCode2 = "project-code-2";
        var project3 = mock(Project.class);
        var projectCode3 = "project-code-3";
        var project4 = mock(Project.class);
        var projectCode4 = "project-code-4";
        var project5 = mock(Project.class);
        var projectCode5 = "project-code-5";
        var project6 = mock(Project.class);
        var projectCode6 = "project-code-6";

        var targetGroupScope1 = mock(UserGroupProjectScope.class);
        var targetGroupScope2 = mock(UserGroupProjectScope.class);
        var targetGroupScope3 = mock(UserGroupProjectScope.class);
        var targetGroupScope4 = mock(UserGroupProjectScope.class);
        var targetGroupScope5 = mock(UserGroupProjectScope.class);
        var targetGroupScope6 = mock(UserGroupProjectScope.class);
        var targetGroupScopes = Set.of(
                targetGroupScope1,
                targetGroupScope2,
                targetGroupScope3,
                targetGroupScope4,
                targetGroupScope5,
                targetGroupScope6
        );

        // When
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);

        // target group
        when(targetGroup.getScopes()).thenReturn(targetGroupScopes);
        when(targetGroupScope1.getProject()).thenReturn(project1);
        when(targetGroupScope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetGroupScope2.getProject()).thenReturn(project2);
        when(targetGroupScope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetGroupScope3.getProject()).thenReturn(project3);
        when(targetGroupScope3.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetGroupScope4.getProject()).thenReturn(project4);
        when(targetGroupScope4.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetGroupScope5.getProject()).thenReturn(project5);
        when(targetGroupScope5.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetGroupScope6.getProject()).thenReturn(project6);
        when(targetGroupScope6.getRole()).thenReturn(ProjectRole.MEMBER);

        // current user
        when(currentUser.getProfile()).thenReturn(currentUserProfile);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromUserGroup(targetGroup, currentUser);
        assertThat(convertedScopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode2, UserAccountScopeRole.MEMBER),
                        tuple(projectCode3, UserAccountScopeRole.ADMIN),
                        tuple(projectCode4, UserAccountScopeRole.ADMIN),
                        tuple(projectCode5, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode6, UserAccountScopeRole.MEMBER)
                );
    }

    @Test
    void getUserAccountScopesFromUserGroup_returnSharedScopesHavingMaximalRoles_whenCurrentUserIsScopedUser() {
        // Given
        var project1 = mock(Project.class);
        var projectCode1 = "project-code-1";
        var project2 = mock(Project.class);
        var projectCode2 = "project-code-2";
        var project3 = mock(Project.class);
        var projectCode3 = "project-code-3";
        var project4 = mock(Project.class);
        var projectCode4 = "project-code-4";
        var project5 = mock(Project.class);
        var projectCode5 = "project-code-5";
        var project6 = mock(Project.class);
        var projectCode6 = "project-code-6";
        var project7 = mock(Project.class);
        var projectCode7 = "project-code-7";
        var project8 = mock(Project.class);
        var projectCode8 = "project-code-8";

        // target group
        var targetGroup = mock(UserGroup.class);

        var targetGroupScope1 = mock(UserGroupProjectScope.class);
        var targetGroupScope2 = mock(UserGroupProjectScope.class);
        var targetGroupScope3 = mock(UserGroupProjectScope.class);
        var targetGroupScope4 = mock(UserGroupProjectScope.class);
        var targetGroupScope5 = mock(UserGroupProjectScope.class);
        var targetGroupScope6 = mock(UserGroupProjectScope.class);
        var targetGroupScopes = Set.of(
                targetGroupScope1,
                targetGroupScope2,
                targetGroupScope3,
                targetGroupScope4,
                targetGroupScope5,
                targetGroupScope6
        );

        // current user
        var currentUser = mock(User.class);

        var currentUserScope1 = mock(UserProjectScope.class);
        var currentUserScope2 = mock(UserProjectScope.class);
        var currentUserScope3 = mock(UserProjectScope.class);
        var currentUserScopes = Set.of(currentUserScope1, currentUserScope2, currentUserScope3);

        var currentUserGroup1 = mock(UserGroup.class);
        var currentUserGroup1Scope1 = mock(UserGroupProjectScope.class);
        var currentUserGroup1Scope2 = mock(UserGroupProjectScope.class);
        var currentUserGroup1Scope3 = mock(UserGroupProjectScope.class);
        var currentUserGroup1Scopes = Set.of(currentUserGroup1Scope1, currentUserGroup1Scope2, currentUserGroup1Scope3);

        var currentUserGroup2 = mock(UserGroup.class);
        var currentUserGroup2Scope1 = mock(UserGroupProjectScope.class);
        var currentUserGroup2Scope2 = mock(UserGroupProjectScope.class);
        var currentUserGroup2Scopes = Set.of(currentUserGroup2Scope1, currentUserGroup2Scope2);

        var currentUserGroup3 = mock(UserGroup.class);
        var currentUserGroup3Scope1 = mock(UserGroupProjectScope.class);
        var currentUserGroup3Scopes = Set.of(currentUserGroup3Scope1);

        var currentUserGroup4 = mock(UserGroup.class);

        var currentUserGroups = Set.of(currentUserGroup1, currentUserGroup2, currentUserGroup3, currentUserGroup4);

        // When
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);
        when(project7.getCode()).thenReturn(projectCode7);
        when(project8.getCode()).thenReturn(projectCode8);

        // target group
        when(targetGroup.getScopes()).thenReturn(targetGroupScopes);
        when(targetGroupScope1.getProject()).thenReturn(project1);
        when(targetGroupScope1.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetGroupScope2.getProject()).thenReturn(project2);
        when(targetGroupScope2.getRole()).thenReturn(ProjectRole.MEMBER);
        when(targetGroupScope3.getProject()).thenReturn(project3);
        when(targetGroupScope3.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetGroupScope4.getProject()).thenReturn(project4);
        when(targetGroupScope4.getRole()).thenReturn(ProjectRole.ADMIN);
        when(targetGroupScope5.getProject()).thenReturn(project5);
        when(targetGroupScope5.getRole()).thenReturn(ProjectRole.MAINTAINER);
        when(targetGroupScope6.getProject()).thenReturn(project6);
        when(targetGroupScope6.getRole()).thenReturn(ProjectRole.MEMBER);

        // current user
        when(currentUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(currentUser.getScopes()).thenReturn(currentUserScopes);
        when(currentUserScope1.getProject()).thenReturn(project1);
        when(currentUserScope2.getProject()).thenReturn(project2);
        when(currentUserScope3.getProject()).thenReturn(project7);

        when(currentUser.getMembershipGroups()).thenReturn(currentUserGroups);
        when(currentUserGroup1.getScopes()).thenReturn(currentUserGroup1Scopes);
        when(currentUserGroup1Scope1.getProject()).thenReturn(project1);
        when(currentUserGroup1Scope2.getProject()).thenReturn(project3);
        when(currentUserGroup1Scope3.getProject()).thenReturn(project7);
        when(currentUserGroup2.getScopes()).thenReturn(currentUserGroup2Scopes);
        when(currentUserGroup2Scope1.getProject()).thenReturn(project3);
        when(currentUserGroup2Scope2.getProject()).thenReturn(project8);
        when(currentUserGroup3.getScopes()).thenReturn(currentUserGroup3Scopes);
        when(currentUserGroup3Scope1.getProject()).thenReturn(project1);
        when(currentUserGroup4.getScopes()).thenReturn(null);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromUserGroup(targetGroup, currentUser);
        assertThat(convertedScopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode2, UserAccountScopeRole.MEMBER),
                        tuple(projectCode3, UserAccountScopeRole.ADMIN)
                );
    }
}
