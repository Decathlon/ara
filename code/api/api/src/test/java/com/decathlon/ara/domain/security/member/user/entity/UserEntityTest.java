package com.decathlon.ara.domain.security.member.user.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserEntityTest {

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
