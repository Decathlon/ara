package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    void getCurrentUserAccountFromCurrentUser_returnCurrentUserAccount_whenCurrentUserHasAnyProfile(UserProfile currentUserProfile) {
        // Given
        var currentUser = mock(User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        var defaultProject = mock(Project.class);
        var defaultProjectCode = "default-project-code";

        var convertedScopes = mock(List.class);

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
        when(currentUser.getLogin()).thenReturn(userLogin);
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(currentUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(currentUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(currentUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(currentUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(currentUser.getDefaultProject()).thenReturn(Optional.of(defaultProject));
        when(defaultProject.getCode()).thenReturn(defaultProjectCode);

        when(currentUser.getProfile()).thenReturn(currentUserProfile);

        when(projectScopeMapper.getCurrentUserAccountScopesFromCurrentUser(currentUser)).thenReturn(convertedScopes);

        when(currentUser.getManagedGroups()).thenReturn(managedGroups);
        when(managedGroup1.getId()).thenReturn(managedGroupId1);
        when(managedGroup2.getId()).thenReturn(managedGroupId2);
        when(managedGroup3.getId()).thenReturn(managedGroupId3);

        when(currentUser.getMembershipGroups()).thenReturn(membershipGroups);
        when(membershipGroup1.getId()).thenReturn(membershipGroupId1);
        when(membershipGroup2.getId()).thenReturn(membershipGroupId2);

        // Then
        var convertedUser = userMapper.getCurrentUserAccountFromCurrentUser(currentUser);
        assertThat(convertedUser)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "defaultProjectCode",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl",
                        "scopes"
                )
                .containsExactly(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(currentUserProfile.name()),
                        defaultProjectCode,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl,
                        convertedScopes
                );
        assertThat(convertedUser.getManagedGroupIds()).containsExactlyInAnyOrder(managedGroupId1, managedGroupId2, managedGroupId3).hasSize(3);
        assertThat(convertedUser.getMembershipGroupIds()).containsExactlyInAnyOrder(membershipGroupId1, membershipGroupId2).hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = UserProfile.class)
    void getUserAccountFromAnotherUser_returnAnotherUserAccount_whenTargetUserHasAnyProfile(UserProfile targetUserProfile) {
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

        var convertedScopes = mock(List.class);

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

        var currentUser = mock(User.class);

        // When
        when(targetUser.getLogin()).thenReturn(userLogin);
        when(targetUser.getProviderName()).thenReturn(providerName);
        when(targetUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(targetUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(targetUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(targetUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(targetUser.getDefaultProject()).thenReturn(Optional.of(defaultProject));
        when(defaultProject.getCode()).thenReturn(defaultProjectCode);

        when(targetUser.getProfile()).thenReturn(targetUserProfile);

        when(projectScopeMapper.getUserAccountScopesFromAnotherUser(targetUser, currentUser)).thenReturn(convertedScopes);

        when(targetUser.getManagedGroups()).thenReturn(managedGroups);
        when(managedGroup1.getId()).thenReturn(managedGroupId1);
        when(managedGroup2.getId()).thenReturn(managedGroupId2);
        when(managedGroup3.getId()).thenReturn(managedGroupId3);

        when(targetUser.getMembershipGroups()).thenReturn(membershipGroups);
        when(membershipGroup1.getId()).thenReturn(membershipGroupId1);
        when(membershipGroup2.getId()).thenReturn(membershipGroupId2);

        // Then
        var convertedUser = userMapper.getUserAccountFromAnotherUser(targetUser, currentUser);
        assertThat(convertedUser)
                .extracting(
                        "providerName",
                        "login",
                        "profile",
                        "defaultProjectCode",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl",
                        "scopes"
                )
                .containsExactly(
                        providerName,
                        userLogin,
                        UserAccountProfile.valueOf(targetUserProfile.name()),
                        defaultProjectCode,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl,
                        convertedScopes
                );
        assertThat(convertedUser.getManagedGroupIds()).containsExactlyInAnyOrder(managedGroupId1, managedGroupId2, managedGroupId3).hasSize(3);
        assertThat(convertedUser.getMembershipGroupIds()).containsExactlyInAnyOrder(membershipGroupId1, membershipGroupId2).hasSize(2);
    }
}
