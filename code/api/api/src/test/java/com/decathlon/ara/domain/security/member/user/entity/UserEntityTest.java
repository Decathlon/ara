package com.decathlon.ara.domain.security.member.user.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEntityTest {

    @Test
    void getMatchingAuthorities_returnSuperAdminProfileAuthority_whenUserProfileIsSuperAdminAndNoScopeFound() {
        // Given
        var user = new UserEntity();
        user.setProfile(UserEntity.UserEntityProfile.SUPER_ADMIN);
        user.setRolesOnProjectWhenScopedUser(null);

        // When

        // Then
        var authorities = user.getMatchingAuthorities();
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder("USER_PROFILE:SUPER_ADMIN");
    }

    @Test
    void getMatchingAuthorities_returnAuditorProfileAuthority_whenUserProfileIsAuditorAndNoScopeFound() {
        // Given
        var user = new UserEntity();
        user.setProfile(UserEntity.UserEntityProfile.AUDITOR);
        user.setRolesOnProjectWhenScopedUser(null);

        // When

        // Then
        var authorities = user.getMatchingAuthorities();
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder("USER_PROFILE:AUDITOR");
    }

    @Test
    void getMatchingAuthorities_returnScopedProfileAuthority_whenUserProfileIsScopedAndNoScopeFound() {
        // Given
        var user = new UserEntity();
        user.setProfile(UserEntity.UserEntityProfile.SCOPED_USER);
        user.setRolesOnProjectWhenScopedUser(null);

        // When

        // Then
        var authorities = user.getMatchingAuthorities();
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder("USER_PROFILE:SCOPED_USER");
    }

    @Test
    void getMatchingAuthorities_returnScopedProfileAuthority_whenUserHasNeitherProfileNorScopes() {
        // Given
        var user = new UserEntity();
        user.setProfile(null);
        user.setRolesOnProjectWhenScopedUser(null);

        // When

        // Then
        var authorities = user.getMatchingAuthorities();
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder("USER_PROFILE:SCOPED_USER");
    }

    @Test
    void getMatchingAuthorities_returnProfileAndScopedAuthorities_whenUserProfileIsScopedAndSomeScopesFound() {
        // Given
        var user = new UserEntity();
        user.setProfile(UserEntity.UserEntityProfile.SCOPED_USER);

        var scope1 = mock(UserEntityRoleOnProject.class);
        var scope2 = mock(UserEntityRoleOnProject.class);
        var scope3 = mock(UserEntityRoleOnProject.class);
        var scopes = List.of(scope1, scope2, scope3);
        var authorityValue1 = "authority1";
        var authorityValue2 = "authority2";
        var authorityValue3 = "authority3";
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        user.setRolesOnProjectWhenScopedUser(scopes);

        // When
        when(authority1.getAuthority()).thenReturn(authorityValue1);
        when(authority2.getAuthority()).thenReturn(authorityValue2);
        when(authority3.getAuthority()).thenReturn(authorityValue3);

        when(scope1.getMatchingAuthority()).thenReturn(authority1);
        when(scope2.getMatchingAuthority()).thenReturn(authority2);
        when(scope3.getMatchingAuthority()).thenReturn(authority3);

        // Then
        var authorities = user.getMatchingAuthorities();
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        "USER_PROFILE:SCOPED_USER",
                        authorityValue1,
                        authorityValue2,
                        authorityValue3
                );
    }

    @Test
    void getProfileFromString_returnSuperAdminProfile_whenStringIsSuperAdmin() {
        // Given
        var profileAsString = "SUPER_ADMIN";

        // When

        // Then
        var profile = UserEntity.UserEntityProfile.getProfileFromString(profileAsString);
        assertThat(profile)
                .isPresent()
                .contains(UserEntity.UserEntityProfile.SUPER_ADMIN);
    }

    @Test
    void getProfileFromString_returnAuditorProfile_whenStringIsAuditor() {
        // Given
        var profileAsString = "AUDITOR";

        // When

        // Then
        var profile = UserEntity.UserEntityProfile.getProfileFromString(profileAsString);
        assertThat(profile)
                .isPresent()
                .contains(UserEntity.UserEntityProfile.AUDITOR);
    }

    @Test
    void getProfileFromString_returnScopedUserProfile_whenStringIsScopedUser() {
        // Given
        var profileAsString = "SCOPED_USER";

        // When

        // Then
        var profile = UserEntity.UserEntityProfile.getProfileFromString(profileAsString);
        assertThat(profile)
                .isPresent()
                .contains(UserEntity.UserEntityProfile.SCOPED_USER);
    }

    @Test
    void getProfileFromString_returnScopedUserProfile_whenStringIsScopedUserButWithDifferentCase() {
        // Given
        var profileAsString = "sCoPeD_UsEr";

        // When

        // Then
        var profile = UserEntity.UserEntityProfile.getProfileFromString(profileAsString);
        assertThat(profile)
                .isPresent()
                .contains(UserEntity.UserEntityProfile.SCOPED_USER);
    }

    @Test
    void getProfileFromString_returnEmptyOptional_whenStringIsNull() {
        // Given

        // When

        // Then
        var profile = UserEntity.UserEntityProfile.getProfileFromString(null);
        assertThat(profile).isNotPresent();
    }

    @Test
    void getProfileFromString_returnEmptyOptional_whenProfileIsUnknown() {
        // Given
        var profileAsString = "unknown-profile";

        // When

        // Then
        var profile = UserEntity.UserEntityProfile.getProfileFromString(profileAsString);
        assertThat(profile).isNotPresent();
    }
}
