package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.account.UserProjectScope;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private ProjectScopeMapper projectScopeMapper;

    @InjectMocks
    private UserMapper userMapper;

    @ParameterizedTest
    @EnumSource(value = UserProfile.class)
    void getFullScopeAccessUserAccountFromUser_returnFullUserAccount_whenUserHasAnyProfile(UserProfile targetProfile) {
        // Given
        var targetUser = mock(User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var defaultProject = mock(Project.class);
        var defaultProjectCode = "default-project-code";

        var scope1 = mock(UserProjectScope.class);
        var scope2 = mock(UserProjectScope.class);
        var scope3 = mock(UserProjectScope.class);
        var userScopes = Set.of(scope1, scope2, scope3);
        var projectScopes = Set.of(scope1, scope2, (ProjectScope) scope3);

        var convertedScope1 = mock(UserAccountScope.class);
        var convertedScope2 = mock(UserAccountScope.class);
        var convertedScope3 = mock(UserAccountScope.class);
        var convertedScopes = List.of(convertedScope1, convertedScope2, convertedScope3);

        var managedGroupId1 = 11L;
        var managedGroupId2 = 12L;
        var managedGroupId3 = 13L;
        var managedGroup1 = mock(UserGroup.class);
        var managedGroup2 = mock(UserGroup.class);
        var managedGroup3 = mock(UserGroup.class);
        var managedGroups = Set.of(managedGroup1, managedGroup2, managedGroup3);

        var membershipGroupId1 = 21L;
        var membershipGroupId2 = 22L;
        var membershipGroup1 = mock(UserGroup.class);
        var membershipGroup2 = mock(UserGroup.class);
        var membershipGroups = Set.of(membershipGroup1, membershipGroup2);

        // When
        when(targetUser.getLogin()).thenReturn(userLogin);
        when(targetUser.getProviderName()).thenReturn(providerName);
        when(targetUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(targetUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(targetUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(targetUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(targetUser.getDefaultProject()).thenReturn(Optional.of(defaultProject));
        when(defaultProject.getCode()).thenReturn(defaultProjectCode);

        when(targetUser.getProfile()).thenReturn(targetProfile);

        when(targetUser.getScopes()).thenReturn(userScopes);
        when(projectScopeMapper.getUserAccountScopesFromProjectScopes(projectScopes)).thenReturn(convertedScopes);

        when(targetUser.getManagedGroups()).thenReturn(managedGroups);
        when(managedGroup1.getId()).thenReturn(managedGroupId1);
        when(managedGroup2.getId()).thenReturn(managedGroupId2);
        when(managedGroup3.getId()).thenReturn(managedGroupId3);

        when(targetUser.getMembershipGroups()).thenReturn(membershipGroups);
        when(membershipGroup1.getId()).thenReturn(membershipGroupId1);
        when(membershipGroup2.getId()).thenReturn(membershipGroupId2);

        // Then
        var convertedUser = userMapper.getFullScopeAccessUserAccountFromUser(targetUser);
        assertThat(convertedUser)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "defaultProjectCode",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .containsExactly(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(targetProfile.name()),
                        defaultProjectCode,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
        assertThat(convertedUser.getScopes()).containsExactlyInAnyOrderElementsOf(convertedScopes);
        assertThat(convertedUser.getManagedGroupIds()).containsExactlyInAnyOrder(managedGroupId1, managedGroupId2, managedGroupId3).hasSize(3);
        assertThat(convertedUser.getMembershipGroupIds()).containsExactlyInAnyOrder(membershipGroupId1, membershipGroupId2).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("argumentsForReadUserThatAreEitherSuperAdminOrAuditor")
    void getPartialScopeAccessUserAccountFromUser_returnFullUserAccount_whenUserIsEitherASuperAdminOrAnAuditor(UserProfile targetProfile, UserProfile readProfile) {
        // Given
        var targetUser = mock(User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var defaultProject = mock(Project.class);
        var defaultProjectCode = "default-project-code";

        var scope1 = mock(UserProjectScope.class);
        var scope2 = mock(UserProjectScope.class);
        var scope3 = mock(UserProjectScope.class);
        var userScopes = Set.of(scope1, scope2, scope3);
        var projectScopes = Set.of(scope1, scope2, (ProjectScope) scope3);

        var convertedScope1 = mock(UserAccountScope.class);
        var convertedScope2 = mock(UserAccountScope.class);
        var convertedScope3 = mock(UserAccountScope.class);
        var convertedScopes = List.of(convertedScope1, convertedScope2, convertedScope3);

        var managedGroupId1 = 11L;
        var managedGroupId2 = 12L;
        var managedGroupId3 = 13L;
        var managedGroup1 = mock(UserGroup.class);
        var managedGroup2 = mock(UserGroup.class);
        var managedGroup3 = mock(UserGroup.class);
        var managedGroups = Set.of(managedGroup1, managedGroup2, managedGroup3);

        var membershipGroupId1 = 21L;
        var membershipGroupId2 = 22L;
        var membershipGroup1 = mock(UserGroup.class);
        var membershipGroup2 = mock(UserGroup.class);
        var membershipGroups = Set.of(membershipGroup1, membershipGroup2);

        var readUser = mock(User.class);

        // When
        when(targetUser.getLogin()).thenReturn(userLogin);
        when(targetUser.getProviderName()).thenReturn(providerName);
        when(targetUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(targetUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(targetUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(targetUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(targetUser.getDefaultProject()).thenReturn(Optional.of(defaultProject));
        when(defaultProject.getCode()).thenReturn(defaultProjectCode);

        when(targetUser.getProfile()).thenReturn(targetProfile);

        when(targetUser.getScopes()).thenReturn(userScopes);
        when(projectScopeMapper.getUserAccountScopesFromProjectScopes(projectScopes)).thenReturn(convertedScopes);

        when(targetUser.getManagedGroups()).thenReturn(managedGroups);
        when(managedGroup1.getId()).thenReturn(managedGroupId1);
        when(managedGroup2.getId()).thenReturn(managedGroupId2);
        when(managedGroup3.getId()).thenReturn(managedGroupId3);

        when(targetUser.getMembershipGroups()).thenReturn(membershipGroups);
        when(membershipGroup1.getId()).thenReturn(membershipGroupId1);
        when(membershipGroup2.getId()).thenReturn(membershipGroupId2);

        when(readUser.getProfile()).thenReturn(readProfile);

        // Then
        var convertedUser = userMapper.getPartialScopeAccessUserAccountFromUser(targetUser, readUser);
        assertThat(convertedUser)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "defaultProjectCode",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .containsExactly(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(targetProfile.name()),
                        defaultProjectCode,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
        assertThat(convertedUser.getScopes()).containsExactlyInAnyOrderElementsOf(convertedScopes);
        assertThat(convertedUser.getManagedGroupIds()).containsExactlyInAnyOrder(managedGroupId1, managedGroupId2, managedGroupId3).hasSize(3);
        assertThat(convertedUser.getMembershipGroupIds()).containsExactlyInAnyOrder(membershipGroupId1, membershipGroupId2).hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = UserProfile.class)
    void getPartialScopeAccessUserAccountFromUser_returnPartialUserAccount_whenReadUserIsAScopedUser(UserProfile targetProfile) {
        // Given
        var targetUser = mock(User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var defaultProject = mock(Project.class);
        var defaultProjectCode = "default-project-code";

        var scope1 = mock(UserProjectScope.class);
        var scope2 = mock(UserProjectScope.class);
        var scope3 = mock(UserProjectScope.class);
        var scope4 = mock(UserProjectScope.class);
        var scope5 = mock(UserProjectScope.class);

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

        var targetUserScopes = Set.of(scope1, scope2, scope3);
        var readUserScopes = Set.of(scope1, scope3, scope4, scope5);
        var scopesToConvert = Set.of(scope1, (ProjectScope) scope3);

        var convertedScopeA = mock(UserAccountScope.class);
        var convertedScopeB = mock(UserAccountScope.class);
        var convertedScopeC = mock(UserAccountScope.class);
        var convertedScopeD = mock(UserAccountScope.class);
        var convertedScopes = List.of(convertedScopeA, convertedScopeB, convertedScopeC, convertedScopeD);

        var managedGroupId1 = 11L;
        var managedGroupId2 = 12L;
        var managedGroupId3 = 13L;
        var managedGroup1 = mock(UserGroup.class);
        var managedGroup2 = mock(UserGroup.class);
        var managedGroup3 = mock(UserGroup.class);
        var managedGroups = Set.of(managedGroup1, managedGroup2, managedGroup3);

        var membershipGroupId1 = 21L;
        var membershipGroupId2 = 22L;
        var membershipGroup1 = mock(UserGroup.class);
        var membershipGroup2 = mock(UserGroup.class);
        var membershipGroups = Set.of(membershipGroup1, membershipGroup2);

        var readUser = mock(User.class);

        // When
        when(targetUser.getLogin()).thenReturn(userLogin);
        when(targetUser.getProviderName()).thenReturn(providerName);
        when(targetUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(targetUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(targetUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(targetUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(targetUser.getDefaultProject()).thenReturn(Optional.of(defaultProject));
        when(defaultProject.getCode()).thenReturn(defaultProjectCode);

        when(targetUser.getProfile()).thenReturn(targetProfile);

        when(targetUser.getScopes()).thenReturn(targetUserScopes);
        when(projectScopeMapper.getUserAccountScopesFromProjectScopes(scopesToConvert)).thenReturn(convertedScopes);

        when(targetUser.getManagedGroups()).thenReturn(managedGroups);
        when(managedGroup1.getId()).thenReturn(managedGroupId1);
        when(managedGroup2.getId()).thenReturn(managedGroupId2);
        when(managedGroup3.getId()).thenReturn(managedGroupId3);

        when(targetUser.getMembershipGroups()).thenReturn(membershipGroups);
        when(membershipGroup1.getId()).thenReturn(membershipGroupId1);
        when(membershipGroup2.getId()).thenReturn(membershipGroupId2);

        when(readUser.getProfile()).thenReturn(UserProfile.SCOPED_USER);
        when(readUser.getScopes()).thenReturn(readUserScopes);

        when(scope1.getProject()).thenReturn(project1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(scope2.getProject()).thenReturn(project2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(scope3.getProject()).thenReturn(project3);
        when(project3.getCode()).thenReturn(projectCode3);
        when(scope4.getProject()).thenReturn(project4);
        when(project4.getCode()).thenReturn(projectCode4);
        when(scope5.getProject()).thenReturn(project5);
        when(project5.getCode()).thenReturn(projectCode5);

        // Then
        var convertedUser = userMapper.getPartialScopeAccessUserAccountFromUser(targetUser, readUser);
        assertThat(convertedUser)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "defaultProjectCode",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl"
                )
                .containsExactly(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(targetProfile.name()),
                        defaultProjectCode,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl
                );
        assertThat(convertedUser.getScopes()).containsExactlyInAnyOrderElementsOf(convertedScopes);
        assertThat(convertedUser.getManagedGroupIds()).containsExactlyInAnyOrder(managedGroupId1, managedGroupId2, managedGroupId3).hasSize(3);
        assertThat(convertedUser.getMembershipGroupIds()).containsExactlyInAnyOrder(membershipGroupId1, membershipGroupId2).hasSize(2);
    }

    private static Stream<Arguments> argumentsForReadUserThatAreEitherSuperAdminOrAuditor() {
        var targetProfiles = Arrays.stream(UserProfile.values()).toList();
        var readProfiles = List.of(UserProfile.SUPER_ADMIN, UserProfile.AUDITOR);
        return targetProfiles.stream().flatMap(targetProfile -> readProfiles.stream().map(readProfile -> Arguments.of(targetProfile, readProfile)));
    }
}
