package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.domain.security.member.user.group.UserGroupProjectScope;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGroupMapperTest {

    @Mock
    private ProjectScopeMapper projectScopeMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserGroupMapper groupMapper;

    @Test
    void getUserAccountGroupFromUserGroup_returnUserAccountGroup_whenUserGroupIsNotNull() {
        // Given
        var groupToConvert = mock(UserGroup.class);

        var providerName = "provider-name";

        var id = 1L;
        var name = "group-name";
        var description = "group-description";

        var now = ZonedDateTime.now();
        var creationDate = now.minusDays(3);
        var creationUser = mock(User.class);
        var creationUserLogin = "creation-user-login";

        var updateDate = now.minusHours(10);
        var updateUser = mock(User.class);
        var updateUserLogin = "update-user-login";

        var scope1 = mock(UserGroupProjectScope.class);
        var scope2 = mock(UserGroupProjectScope.class);
        var scope3 = mock(UserGroupProjectScope.class);
        var groupScopes = Set.of(scope1, scope2, scope3);
        var projectScopes = Set.of((ProjectScope) scope1, (ProjectScope) scope2, (ProjectScope) scope3);

        var convertedScope1 = mock(UserAccountScope.class);
        var convertedScope2 = mock(UserAccountScope.class);
        var convertedScope3 = mock(UserAccountScope.class);
        var convertedScopes = List.of(convertedScope1, convertedScope2, convertedScope3);

        var member1 = mock(User.class);
        var convertedMember1 = mock(UserAccount.class);
        var member2 = mock(User.class);
        var convertedMember2 = mock(UserAccount.class);
        var member3 = mock(User.class);
        var convertedMember3 = mock(UserAccount.class);
        var members = Set.of(member1, member2, member3);

        var manager1 = mock(User.class);
        var convertedManager1 = mock(UserAccount.class);
        var manager2 = mock(User.class);
        var convertedManager2 = mock(UserAccount.class);
        var manager3 = mock(User.class);
        var convertedManager3 = mock(UserAccount.class);
        var managers = Set.of(manager1, manager2, manager3);

        var readUser = mock(User.class);

        // When
        when(groupToConvert.getProviderName()).thenReturn(providerName);
        when(groupToConvert.getId()).thenReturn(id);
        when(groupToConvert.getName()).thenReturn(name);
        when(groupToConvert.getDescription()).thenReturn(description);

        when(groupToConvert.getCreationDate()).thenReturn(creationDate);
        when(groupToConvert.getCreationUser()).thenReturn(creationUser);
        when(creationUser.getLogin()).thenReturn(creationUserLogin);
        when(groupToConvert.getUpdateDate()).thenReturn(updateDate);
        when(groupToConvert.getUpdateUser()).thenReturn(updateUser);
        when(updateUser.getLogin()).thenReturn(updateUserLogin);

        when(groupToConvert.getScopes()).thenReturn(groupScopes);
        when(projectScopeMapper.getUserAccountScopesFromProjectScopes(projectScopes)).thenReturn(convertedScopes);

        when(groupToConvert.getMembers()).thenReturn(members);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(member1, readUser)).thenReturn(convertedMember1);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(member2, readUser)).thenReturn(convertedMember2);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(member3, readUser)).thenReturn(convertedMember3);

        when(groupToConvert.getManagers()).thenReturn(managers);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(manager1, readUser)).thenReturn(convertedManager1);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(manager2, readUser)).thenReturn(convertedManager2);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(manager3, readUser)).thenReturn(convertedManager3);

        // Then
        var convertedGroup = groupMapper.getUserAccountGroupFromUserGroup(groupToConvert, readUser);
        assertThat(convertedGroup)
                .extracting(
                        "providerName",
                        "id",
                        "name",
                        "description",
                        "creationDate",
                        "creationUserLogin",
                        "updateDate",
                        "updateUserLogin"
                )
                .contains(
                        providerName,
                        id,
                        name,
                        description,
                        Date.from(creationDate.toInstant()),
                        creationUserLogin,
                        Date.from(updateDate.toInstant()),
                        updateUserLogin
                );

        assertThat(convertedGroup.getScopes()).containsExactlyInAnyOrderElementsOf(convertedScopes);
        assertThat(convertedGroup.getMembers()).containsExactlyInAnyOrder(convertedMember1, convertedMember2, convertedMember3);
        assertThat(convertedGroup.getManagers()).containsExactlyInAnyOrder(convertedManager1, convertedManager2, convertedManager3);
    }
}
