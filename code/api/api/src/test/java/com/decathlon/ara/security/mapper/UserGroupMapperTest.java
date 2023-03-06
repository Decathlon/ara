package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import com.decathlon.ara.security.dto.user.UserAccount;
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
        var targetGroup = mock(UserGroup.class);

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

        var convertedScopes = mock(List.class);

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

        var currentUser = mock(User.class);

        // When
        when(targetGroup.getProviderName()).thenReturn(providerName);
        when(targetGroup.getId()).thenReturn(id);
        when(targetGroup.getName()).thenReturn(name);
        when(targetGroup.getDescription()).thenReturn(description);

        when(targetGroup.getCreationDate()).thenReturn(creationDate);
        when(targetGroup.getCreationUser()).thenReturn(creationUser);
        when(creationUser.getLogin()).thenReturn(creationUserLogin);
        when(targetGroup.getUpdateDate()).thenReturn(updateDate);
        when(targetGroup.getUpdateUser()).thenReturn(updateUser);
        when(updateUser.getLogin()).thenReturn(updateUserLogin);

        when(projectScopeMapper.getUserAccountScopesFromUserGroup(targetGroup, currentUser)).thenReturn(convertedScopes);

        when(targetGroup.getMembers()).thenReturn(members);
        when(userMapper.getUserAccountFromAnotherUser(member1, currentUser)).thenReturn(convertedMember1);
        when(userMapper.getUserAccountFromAnotherUser(member2, currentUser)).thenReturn(convertedMember2);
        when(userMapper.getUserAccountFromAnotherUser(member3, currentUser)).thenReturn(convertedMember3);

        when(targetGroup.getManagers()).thenReturn(managers);
        when(userMapper.getUserAccountFromAnotherUser(manager1, currentUser)).thenReturn(convertedManager1);
        when(userMapper.getUserAccountFromAnotherUser(manager2, currentUser)).thenReturn(convertedManager2);
        when(userMapper.getUserAccountFromAnotherUser(manager3, currentUser)).thenReturn(convertedManager3);

        // Then
        var convertedGroup = groupMapper.getUserAccountGroupFromUserGroup(targetGroup, currentUser);
        assertThat(convertedGroup)
                .extracting(
                        "providerName",
                        "id",
                        "name",
                        "description",
                        "creationDate",
                        "creationUserLogin",
                        "updateDate",
                        "updateUserLogin",
                        "scopes"
                )
                .contains(
                        providerName,
                        id,
                        name,
                        description,
                        Date.from(creationDate.toInstant()),
                        creationUserLogin,
                        Date.from(updateDate.toInstant()),
                        updateUserLogin,
                        convertedScopes
                );
        assertThat(convertedGroup.getMembers()).containsExactlyInAnyOrder(convertedMember1, convertedMember2, convertedMember3);
        assertThat(convertedGroup.getManagers()).containsExactlyInAnyOrder(convertedManager1, convertedManager2, convertedManager3);
    }
}
