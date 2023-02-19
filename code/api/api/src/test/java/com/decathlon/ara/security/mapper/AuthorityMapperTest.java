package com.decathlon.ara.security.mapper;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;

import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;
import static com.decathlon.ara.security.mapper.AuthorityMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorityMapperTest {

    @InjectMocks
    private AuthorityMapper authorityMapper;

    @ParameterizedTest
    @EnumSource(value = UserAccountProfile.class)
    void getGrantedAuthoritiesFromUserAccount_returnProfileAndScopedAuthorities_whenUserAccountHasProfileAndScopes(UserAccountProfile profile) {
        // Given
        var userAccount = mock(UserAccount.class);

        var projectCode1 = "project-code1";
        var role1 = UserAccountScopeRole.ADMIN;
        var scope1 = mock(UserAccountScope.class);

        var projectCode2 = "project-code2";
        var role2 = UserAccountScopeRole.MAINTAINER;
        var scope2 = mock(UserAccountScope.class);

        var projectCode3 = "project-code3";
        var role3 = UserAccountScopeRole.MEMBER;
        var scope3 = mock(UserAccountScope.class);

        var scopes = List.of(scope1, scope2, scope3);

        var managedGroupId1 = 1L;
        var managedGroupId2 = 2L;
        var managedGroupId3 = 3L;
        var managedGroupIds = List.of(managedGroupId1, managedGroupId2, managedGroupId3);

        // When
        when(userAccount.getProfile()).thenReturn(profile);
        when(userAccount.getScopes()).thenReturn(scopes);
        when(scope1.getProject()).thenReturn(projectCode1);
        when(scope1.getRole()).thenReturn(role1);
        when(scope2.getProject()).thenReturn(projectCode2);
        when(scope2.getRole()).thenReturn(role2);
        when(scope3.getProject()).thenReturn(projectCode3);
        when(scope3.getRole()).thenReturn(role3);
        when(userAccount.getManagedGroupIds()).thenReturn(managedGroupIds);

        // Then
        var authorities = authorityMapper.getGrantedAuthoritiesFromUserAccount(userAccount);
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        String.format("%s%s", AUTHORITY_USER_PROFILE_PREFIX, profile),
                        String.format("%s%s:ADMIN", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode1),
                        String.format("%s%s:MAINTAINER", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode2),
                        String.format("%s%s:MEMBER", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode3),
                        String.format("%s%d", AUTHORITY_MANAGED_GROUP_PREFIX, managedGroupId1),
                        String.format("%s%d", AUTHORITY_MANAGED_GROUP_PREFIX, managedGroupId2),
                        String.format("%s%d", AUTHORITY_MANAGED_GROUP_PREFIX, managedGroupId3)
                );
    }

    @Test
    void getUserAccountProfileFromAuthorities_returnEmptyOptional_whenAuthoritiesNull() {
        // Given

        // When

        // Then
        var profile = authorityMapper.getUserAccountProfileFromAuthorities(null);
        assertThat(profile).isNotPresent();
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountProfile.class)
    void getUserAccountProfileFromAuthorities_returnProfile_whenProfileFound(UserAccountProfile userProfile) {
        // Given
        var authorityAsString = String.format("%s%s", AUTHORITY_USER_PROFILE_PREFIX, userProfile.name());
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        // When
        when(authority.getAuthority()).thenReturn(authorityAsString);

        // Then
        var profile = authorityMapper.getUserAccountProfileFromAuthorities(authorities);
        assertThat(profile).isPresent().contains(userProfile);
    }

    @Test
    void getUserAccountProfileFromAuthorities_returnEmptyOptional_whenProfileFoundButUnknown() {
        // Given
        var authorityAsString = String.format("%sUNKNOWN_PROFILE", AUTHORITY_USER_PROFILE_PREFIX);
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        // When
        when(authority.getAuthority()).thenReturn(authorityAsString);

        // Then
        var profile = authorityMapper.getUserAccountProfileFromAuthorities(authorities);
        assertThat(profile).isNotPresent();
    }

    @Test
    void getUserAccountProfileFromAuthorities_returnEmptyOptional_whenProfileNotFound() {
        // Given
        var authorityAsString = "any_other_authority";
        var authority = mock(GrantedAuthority.class);
        var authorities = Set.of(authority);

        // When
        when(authority.getAuthority()).thenReturn(authorityAsString);

        // Then
        var profile = authorityMapper.getUserAccountProfileFromAuthorities(authorities);
        assertThat(profile).isNotPresent();
    }

    @Test
    void getUserAccountScopesFromAuthorities_returnEmptyList_whenAuthoritiesNull() {
        // Given

        // When

        // Then
        var scopes = authorityMapper.getUserAccountScopesFromAuthorities(null);
        assertThat(scopes).isEmpty();
    }

    @Test
    void getUserAccountScopesFromAuthorities_returnUserAccountScopesPlusDemoProjectScope_whenScopedProjectAuthoritiesFound() {
        // Given
        var projectCode1 = "project-code1";
        var projectCode2 = "project-code2";
        var projectCode3 = "project-code3";
        var projectCode4 = "project-code4";

        var authorityAsString1 = AUTHORITY_USER_PROFILE_PREFIX;
        var authority1 = mock(GrantedAuthority.class);
        var authorityAsString2 = String.format("%s%s:MEMBER", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode1);
        var authority2 = mock(GrantedAuthority.class);
        var authorityAsString3 = String.format("%s%s:mAiNtAiNeR", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode2);
        var authority3 = mock(GrantedAuthority.class);
        var authorityAsString4 = String.format("%s%s:admin", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode3);
        var authority4 = mock(GrantedAuthority.class);
        var authorityAsString5 = String.format("%s%s:not-a-role", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCode4);
        var authority5 = mock(GrantedAuthority.class);
        var authorityAsString6 = "unknown_authority";
        var authority6 = mock(GrantedAuthority.class);
        var authorities = Set.of(authority1, authority2, authority3, authority4, authority5, authority6);

        // When
        when(authority1.getAuthority()).thenReturn(authorityAsString1);
        when(authority2.getAuthority()).thenReturn(authorityAsString2);
        when(authority3.getAuthority()).thenReturn(authorityAsString3);
        when(authority4.getAuthority()).thenReturn(authorityAsString4);
        when(authority5.getAuthority()).thenReturn(authorityAsString5);
        when(authority6.getAuthority()).thenReturn(authorityAsString6);

        // Then
        var scopes = authorityMapper.getUserAccountScopesFromAuthorities(authorities);
        assertThat(scopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.MEMBER),
                        tuple(projectCode2, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode3, UserAccountScopeRole.ADMIN),
                        tuple(DEMO_PROJECT_CODE, UserAccountScopeRole.ADMIN)
                );
    }

    @Test
    void getManagedUserAccountGroupIdsFromAuthorities_returnGroupIds_whenManagedGroupAuthoritiesFound() {
        // Given
        var groupId1 = 1L;
        var groupId2 = 2L;
        var groupId3 = 3L;

        var authorityAsString1 = AUTHORITY_USER_PROFILE_PREFIX;
        var authority1 = mock(GrantedAuthority.class);
        var authorityAsString2 = String.format("%s%s:%s", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, "project-code1", UserAccountScopeRole.MEMBER);
        var authority2 = mock(GrantedAuthority.class);
        var authorityAsString3 = String.format("%s%s:%s", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, "project-code2", UserAccountScopeRole.MAINTAINER);
        var authority3 = mock(GrantedAuthority.class);
        var authorityAsString4 = String.format("%s%s:%s", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, "project-code3", UserAccountScopeRole.ADMIN);
        var authority4 = mock(GrantedAuthority.class);
        var authorityAsString5 = String.format("%s%d", AUTHORITY_MANAGED_GROUP_PREFIX, groupId1);
        var authority5 = mock(GrantedAuthority.class);
        var authorityAsString6 = String.format("%s%d", AUTHORITY_MANAGED_GROUP_PREFIX, groupId2);
        var authority6 = mock(GrantedAuthority.class);
        var authorityAsString7 = String.format("%s%d", AUTHORITY_MANAGED_GROUP_PREFIX, groupId3);
        var authority7 = mock(GrantedAuthority.class);
        var authorities = Set.of(authority1, authority2, authority3, authority4, authority5, authority6, authority7);

        // When
        when(authority1.getAuthority()).thenReturn(authorityAsString1);
        when(authority2.getAuthority()).thenReturn(authorityAsString2);
        when(authority3.getAuthority()).thenReturn(authorityAsString3);
        when(authority4.getAuthority()).thenReturn(authorityAsString4);
        when(authority5.getAuthority()).thenReturn(authorityAsString5);
        when(authority6.getAuthority()).thenReturn(authorityAsString6);
        when(authority7.getAuthority()).thenReturn(authorityAsString7);

        // Then
        var managedGroupIds = authorityMapper.getManagedUserAccountGroupIdsFromAuthorities(authorities);
        assertThat(managedGroupIds).containsExactlyInAnyOrder(groupId1, groupId2, groupId3);
    }
}
