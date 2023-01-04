package com.decathlon.ara.security.dto.user.scope;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserAccountScopeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void userAccountScopeFactory_returnEmptyOptional_whenProjectCodeIsBlank(String projectCode) {
        // Given
        var roleAsString = "ADMIN";

        // When

        // Then
        var scope = UserAccountScope.userAccountScopeFactory(projectCode, roleAsString);
        assertThat(scope).isNotPresent();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void userAccountScopeFactory_returnEmptyOptional_whenRoleIsBlank(String roleAsString) {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        var scope = UserAccountScope.userAccountScopeFactory(projectCode, roleAsString);
        assertThat(scope).isNotPresent();
    }

    @Test
    void userAccountScopeFactory_returnEmptyOptional_whenRoleIsIncorrect() {
        // Given
        var projectCode = "project-code";
        var roleAsString = "unknown-role";

        // When

        // Then
        var scope = UserAccountScope.userAccountScopeFactory(projectCode, roleAsString);
        assertThat(scope).isNotPresent();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MAINTAINER", "MEMBER"})
    void userAccountScopeFactory_returnUserAccountScope_whenRoleIsCorrect(String roleAsString) {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        var scope = UserAccountScope.userAccountScopeFactory(projectCode, roleAsString);
        assertThat(scope).isPresent();
        assertThat(scope.get())
                .extracting("project", "role")
                .contains(projectCode, UserAccountScopeRole.valueOf(roleAsString));
    }

}
