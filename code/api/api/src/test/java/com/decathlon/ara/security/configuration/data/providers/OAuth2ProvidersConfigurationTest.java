package com.decathlon.ara.security.configuration.data.providers;

import com.decathlon.ara.security.configuration.data.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.provider.ProviderConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UsersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2ProvidersConfigurationTest {

    @InjectMocks
    private OAuth2ProvidersConfiguration providersConfiguration;

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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getUserProfileConfiguration_returnEmptyOptional_whenProviderNameIsBlank(String providerName) {
        // Given
        var login = "login";

        // When

        // Then
        var userProfile = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(userProfile).isNotPresent();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getUserProfileConfiguration_returnEmptyOptional_whenUserLoginIsBlank(String login) {
        // Given
        var providerName = "provider";

        // When

        // Then
        var userProfile = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(userProfile).isNotPresent();
    }

    @Test
    void getUserProfileConfiguration_returnEmptyOptional_whenSetupIsNull() {
        // Given
        var providerName = "provider";
        var login = "login";

        ReflectionTestUtils.setField(providersConfiguration, "setup", null);

        // When

        // Then
        var userProfile = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(userProfile).isNotPresent();
    }

    @Test
    void getUserProfileConfiguration_returnEmptyOptional_whenProviderGivenButNotFound() {
        // Given
        var providerName = "unknown-provider";
        var login = "login";

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
        var matchingUser = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getUserProfileConfiguration_returnEmptyOptional_whenProviderFoundButUsersConfigurationIsNull() {
        // Given
        var providerName = "matching-provider";
        var login = "login";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(setup3.getUsers()).thenReturn(null);

        // Then
        var matchingUser = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getUserProfileConfiguration_returnEmptyOptional_whenProviderFoundButProfilesAreNull() {
        // Given
        var providerName = "matching-provider";
        var login = "login";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(null);

        // Then
        var matchingUser = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getUserProfileConfiguration_returnEmptyOptional_whenProviderFoundButNotTheUser() {
        // Given
        var providerName = "matching-provider";
        var login = "login";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);
        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var login1 = "login-1";
        var login2 = "login-2";
        var login3 = "login-3";
        var profiles = List.of(profile1, profile2, profile3);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn(login1);
        when(profile2.getLogin()).thenReturn(login2);
        when(profile3.getLogin()).thenReturn(login3);

        // Then
        var matchingUser = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(matchingUser).isNotPresent();
    }

    @Test
    void getUserProfileConfiguration_returnProfileConfiguration_whenProviderAndUserFound() {
        // Given
        var providerName = "matching-provider";
        var login = "login";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);
        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var login1 = "login-1";
        var login2 = "login-2";
        var profiles = List.of(profile1, profile2, profile3);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn(login1);
        when(profile2.getLogin()).thenReturn(login2);
        when(profile3.getLogin()).thenReturn(login);

        // Then
        var matchingUser = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(matchingUser).contains(profile3);
    }

    @Test
    void getUserProfileConfiguration_returnProfileConfiguration_whenProviderAndUserFoundButSomeProvidersWereNull() {
        // Given
        var providerName = "matching-provider";
        var login = "login";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);
        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var login1 = "login-1";
        var login2 = "login-2";
        var profiles = List.of(profile1, profile2, profile3);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(null);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn(login1);
        when(profile2.getLogin()).thenReturn(login2);
        when(profile3.getLogin()).thenReturn(login);

        // Then
        var matchingUser = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(matchingUser).contains(profile3);
    }

    @Test
    void getUserProfileConfiguration_returnProfileConfiguration_whenProviderAndUserFoundButProviderNamesDoesNotShareTheSameCase() {
        // Given
        var providerName = "matching-provider";
        var providerNameWithDifferentCase = "mAtChInG-PrOvIdEr";
        var login = "login";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);
        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var login1 = "login-1";
        var login2 = "login-2";
        var profiles = List.of(profile1, profile2, profile3);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(provider3.getRegistration()).thenReturn(providerNameWithDifferentCase);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn(login1);
        when(profile2.getLogin()).thenReturn(login2);
        when(profile3.getLogin()).thenReturn(login);

        // Then
        var matchingUser = providersConfiguration.getUserProfileConfiguration(providerName, login);
        assertThat(matchingUser).contains(profile3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void createNewProjectsIfNotFoundAtUsersInit_returnFalse_whenProviderNameIsBlank(String providerName) {
        // Given

        // Where

        // Then
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isFalse();
    }

    @Test
    void createNewProjectsIfNotFoundAtUsersInit_returnFalse_whenSetupIsNull() {
        // Given
        var providerName = "provider";

        ReflectionTestUtils.setField(providersConfiguration, "setup", null);

        // When

        // Then
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isFalse();
    }

    @Test
    void createNewProjectsIfNotFoundAtUsersInit_returnFalse_whenProviderGivenButNotFound() {
        // Given
        var providerName = "unknown-provider";

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
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isFalse();
    }

    @Test
    void createNewProjectsIfNotFoundAtUsersInit_returnFalse_whenProviderFoundButUsersConfigurationIsNull() {
        // Given
        var providerName = "matching-provider";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(setup3.getUsers()).thenReturn(null);

        // Then
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isFalse();
    }

    @Test
    void createNewProjectsIfNotFoundAtUsersInit_returnFalse_whenProviderFoundButCannotCreateProjectOnInit() {
        // Given
        var providerName = "matching-provider";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(usersConfiguration.getCreateNewProjectOnInit()).thenReturn(false);

        // Then
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isFalse();
    }

    @Test
    void createNewProjectsIfNotFoundAtUsersInit_returnTrue_whenProviderFoundAndCanCreateProjectOnInit() {
        // Given
        var providerName = "matching-provider";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(usersConfiguration.getCreateNewProjectOnInit()).thenReturn(true);

        // Then
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isTrue();
    }

    @Test
    void createNewProjectsIfNotFoundAtUsersInit_returnTrueAndIgnoreNullProviders_whenProviderFoundAndCanCreateProjectOnInitButSomeProvidersWereNull() {
        // Given
        var providerName = "matching-provider";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";

        var usersConfiguration = mock(UsersConfiguration.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(null);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider3.getRegistration()).thenReturn(providerName);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(usersConfiguration.getCreateNewProjectOnInit()).thenReturn(true);

        // Then
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isTrue();
    }

    @Test
    void createNewProjectsIfNotFoundAtUsersInit_returnTrueAndIgnoreProviderNameCase_whenProviderFoundAndCanCreateProjectOnInitButProviderNamesDoesNotShareTheSameCase() {
        // Given
        var providerName = "matching-provider";
        var providerNameWithDifferentCase = "mAtChInG-PrOvIdEr";

        var setup1 = mock(ProviderSetupConfiguration.class);
        var setup2 = mock(ProviderSetupConfiguration.class);
        var setup3 = mock(ProviderSetupConfiguration.class);
        var setups = List.of(setup1, setup2, setup3);

        var provider1 = mock(ProviderConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var providerName1 = "another-provider-1";
        var providerName2 = "another-provider-2";

        var usersConfiguration = mock(UsersConfiguration.class);

        ReflectionTestUtils.setField(providersConfiguration, "setup", setups);

        // When
        when(setup1.getProvider()).thenReturn(provider1);
        when(provider1.getRegistration()).thenReturn(providerName1);
        when(setup2.getProvider()).thenReturn(provider2);
        when(provider2.getRegistration()).thenReturn(providerName2);
        when(setup3.getProvider()).thenReturn(provider3);
        when(provider3.getRegistration()).thenReturn(providerNameWithDifferentCase);
        when(setup3.getUsers()).thenReturn(usersConfiguration);
        when(usersConfiguration.getCreateNewProjectOnInit()).thenReturn(true);

        // Then
        var canCreateProject = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        assertThat(canCreateProject).isTrue();
    }

}
