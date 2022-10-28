package com.decathlon.ara.security.configuration.data.providers;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.security.configuration.data.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.provider.ProviderConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UsersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2ProvidersConfigurationTest {

    @InjectMocks
    private OAuth2ProvidersConfiguration providersConfiguration;

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnNoUser_whenProviderIsNull() {
        // Given
        String providerName = null;
        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);

        // When

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnNoUser_whenLoginIsNull() {
        // Given
        var providerName = "provider-name";
        String login = null;
        var projectRepository = mock(ProjectRepository.class);

        // When

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnNoUser_whenSetupIsNull() {
        // Given
        var providerName = "provider";
        var login = "login";
        var projectRepository = mock(ProjectRepository.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", null);

        // When

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnNoUser_whenProviderGivenButNotFound() {
        // Given
        var providerName = "unknown-provider";
        var login = "login";
        var projectRepository = mock(ProjectRepository.class);

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "registration1";
        var providerName2 = "registration2";
        var providerName3 = "registration3";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName3);

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnNoUser_whenProviderFoundButNotTheUser() {
        // Given
        var providerName = "registration3";
        var login = "login";
        var projectRepository = mock(ProjectRepository.class);

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "registration1";
        var providerName2 = "registration2";
        var providerName3 = "registration3";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getMatchingUserEntityFromLogin(login, projectRepository)).thenReturn(Optional.empty());
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName3);

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnUser_whenProviderAndUserFound() {
        // Given
        var providerName = "registration3";
        var login = "login";
        var projectRepository = mock(ProjectRepository.class);

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "registration1";
        var providerName2 = "registration2";
        var providerName3 = "registration3";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        var user = mock(UserEntity.class);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getMatchingUserEntityFromLogin(login, projectRepository)).thenReturn(Optional.of(user));
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName3);

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).contains(user);
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnUserAndIgnoreNullProviders_whenProviderAndUserFoundButSomeProvidersWereNull() {
        // Given
        var providerName = "registration3";
        var login = "login";
        var projectRepository = mock(ProjectRepository.class);

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "registration1";
        var providerName3 = "registration3";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        var user = mock(UserEntity.class);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(null);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getMatchingUserEntityFromLogin(login, projectRepository)).thenReturn(Optional.of(user));
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider3.getRegistration()).thenReturn(providerName3);

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).contains(user);
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnFirstNonEmptyUser_whenManyProviderAndUserFound() {
        // Given
        var providerName = "registration";
        var login = "login";
        var projectRepository = mock(ProjectRepository.class);

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "registration";
        var providerName2 = "another-registration";
        var providerName3 = "registration";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        var user = mock(UserEntity.class);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup1.getMatchingUserEntityFromLogin(login, projectRepository)).thenReturn(Optional.empty());
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getMatchingUserEntityFromLogin(login, projectRepository)).thenReturn(Optional.of(user));
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName3);

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).contains(user);
    }

    @Test
    void getMatchingUserEntityFromProviderNameAndLogin_returnUser_whenProviderAndUserFoundButNotTheSameCase() {
        // Given
        var providerName = "ReGiStRaTiOn3";
        var login = "login";
        var projectRepository = mock(ProjectRepository.class);

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "registration1";
        var providerName2 = "registration2";
        var providerName3 = "rEgIsTrAtIoN3";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        var user = mock(UserEntity.class);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getMatchingUserEntityFromLogin(login, projectRepository)).thenReturn(Optional.of(user));
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName3);

        // Then
        var matchingUser = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository);
        assertThat(matchingUser).contains(user);
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnEmptyMap_whenProviderNameIsBlank() {
        // Given

        // When

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(null);
        assertThat(actualCustomAttributes).isEmpty();
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnEmptyMap_whenNoSetupFound() {
        // Given
        var providerName = "registration";

        ReflectionTestUtils.setField(providersConfiguration, "setup", null);

        // When

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        assertThat(actualCustomAttributes).isEmpty();
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnEmptyMap_whenProviderNotFound() {
        // Given
        var providerName = "unknown-provider-name";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "provider1";
        var providerName2 = "provider2";
        var providerName3 = "provider3";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName3);

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        assertThat(actualCustomAttributes).isEmpty();
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnEmptyMap_whenProviderFoundButUsersConfigurationNull() {
        // Given
        var providerName = "provider-name";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "provider1";
        var providerName2 = "provider2";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(null);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName);

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        assertThat(actualCustomAttributes).isEmpty();
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnEmptyMap_whenProviderFoundButUsersConfigurationCustomAttributesNull() {
        // Given
        var providerName = "provider-name";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "provider1";
        var providerName2 = "provider2";

        var users = mock(UsersConfiguration.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(users);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(users.getCustomAttributes()).thenReturn(null);

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        assertThat(actualCustomAttributes).isEmpty();
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnCustomAttributes_whenProviderAndUsersConfigurationCustomAttributesFound() {
        // Given
        var providerName = "provider-name";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "provider1";
        var providerName2 = "provider2";

        var users = mock(UsersConfiguration.class);
        Map<String, String> customAttributes = mock(Map.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(users);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(users.getCustomAttributes()).thenReturn(customAttributes);

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        assertThat(actualCustomAttributes).isSameAs(customAttributes);
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnCustomAttributesAndIgnoreNullProviders_whenProviderAndUsersConfigurationCustomAttributesFoundButSomeProvidersWereNull() {
        // Given
        var providerName = "provider-name";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "provider1";

        var users = mock(UsersConfiguration.class);
        Map<String, String> customAttributes = mock(Map.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(null);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(users);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(users.getCustomAttributes()).thenReturn(customAttributes);

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        assertThat(actualCustomAttributes).isSameAs(customAttributes);
    }

    @Test
    void getUsersCustomAttributesFromProviderName_returnCustomAttributesAndIgnoreProviderNameCase_whenProviderAndUsersConfigurationCustomAttributesFoundButProviderNameCaseIsDifferent() {
        // Given
        var providerName = "provider-name";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "provider1";
        var providerName2 = "provider2";

        var users = mock(UsersConfiguration.class);
        Map<String, String> customAttributes = mock(Map.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(users);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(provider3.getRegistration()).thenReturn("pRoViDeR-NaMe");
        when(users.getCustomAttributes()).thenReturn(customAttributes);

        // Then
        var actualCustomAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        assertThat(actualCustomAttributes).isSameAs(customAttributes);
    }
}
