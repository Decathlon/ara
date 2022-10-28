package com.decathlon.ara.security.configuration.data.providers.setup;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.security.configuration.data.providers.setup.provider.ProviderConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserScopeConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UsersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderSetupConfigurationTest {

    @InjectMocks
    private ProviderSetupConfiguration setupConfiguration;

    @Test
    void getMatchingUserEntityFromLogin_returnNoUser_whenLoginNull() {
        // Given
        String login = null;
        var projectRepository = mock(ProjectRepository.class);

        // When

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isNotPresent();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnNoUser_whenUsersConfigurationNull() {
        // Given
        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", null);

        // When

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isNotPresent();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnNoUser_whenUsersConfigurationProfilesNull() {
        // Given
        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);

        // When
        when(usersConfiguration.getProfiles()).thenReturn(null);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isNotPresent();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnNoUser_whenProviderConfigurationIsNull() {
        // Given
        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", null);

        // When
        when(usersConfiguration.getProfiles()).thenReturn(profiles);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isNotPresent();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnNoUser_whenProviderConfigurationRegistrationIsNull() {
        // Given
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(providerConfiguration.getRegistration()).thenReturn(null);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isNotPresent();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnNoUser_whenLoginNotFound() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "unknown-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isNotPresent();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnSuperAdminUser_whenSuperAdminUserFound() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");
        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("super_admin");
        when(matchingProfile.getScopes()).thenReturn(null);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SUPER_ADMIN
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser()).isEmpty();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnAuditorUser_whenAuditorUserFound() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");
        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("auditor");
        when(matchingProfile.getScopes()).thenReturn(null);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.AUDITOR
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser()).isEmpty();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"scoped_user", "sCoPeD_UsEr", "   "})
    void getMatchingUserEntityFromLogin_returnScopedUser_whenScopedUserFound(String profile) {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");
        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn(profile);
        when(matchingProfile.getScopes()).thenReturn(null);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser()).isEmpty();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "admin,maintainer,member",
            "aDmIn,mAiNtAiNeR,mEmBeR",
            "admin,maintainer,",
            "admin,maintainer,unknown-scope"
    })
    void getMatchingUserEntityFromLogin_returnScopedUserWithScopes_whenScopedUserAndScopesFound(String admin, String maintainer, String member) {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3);

        var projectCode1 = "prj1";
        var projectCode2 = "prj2";
        var projectCode3 = "prj3";
        var projectCode4 = "prj4";
        var projectCode5 = "prj5";
        var projectCode6 = "prj6";
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var project4 = mock(Project.class);
        var project5 = mock(Project.class);
        var project6 = mock(Project.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("scoped_user");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        when(scope1.getScope()).thenReturn(admin);
        when(scope1.getProjects()).thenReturn(List.of(projectCode1, projectCode2, projectCode3));
        when(scope2.getScope()).thenReturn(maintainer);
        when(scope2.getProjects()).thenReturn(List.of(projectCode4, projectCode5));
        when(scope3.getScope()).thenReturn(member);
        when(scope3.getProjects()).thenReturn(List.of(projectCode6));

        when(projectRepository.findByCode(projectCode1)).thenReturn(Optional.of(project1));
        when(projectRepository.findByCode(projectCode2)).thenReturn(Optional.of(project2));
        when(projectRepository.findByCode(projectCode3)).thenReturn(Optional.of(project3));
        when(projectRepository.findByCode(projectCode4)).thenReturn(Optional.of(project4));
        when(projectRepository.findByCode(projectCode5)).thenReturn(Optional.of(project5));
        when(projectRepository.findByCode(projectCode6)).thenReturn(Optional.of(project6));

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser())
                .extracting(
                        "userEntity.providerName",
                        "userEntity.login",
                        "project",
                        "role"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                providerName,
                                login,
                                project1,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project2,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project3,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project4,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project5,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project6,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER
                        )
                );
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, times(1)).findByCode(projectCode1);
        verify(projectRepository, times(1)).findByCode(projectCode2);
        verify(projectRepository, times(1)).findByCode(projectCode3);
        verify(projectRepository, times(1)).findByCode(projectCode4);
        verify(projectRepository, times(1)).findByCode(projectCode5);
        verify(projectRepository, times(1)).findByCode(projectCode6);
    }

    @Test
    void getMatchingUserEntityFromLogin_returnSuperAdminWithoutScopes_whenSuperAdminButScopesFound() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("super_admin");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SUPER_ADMIN
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser()).isEmpty();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, never()).findByCode(anyString());
    }

    @Test
    void getMatchingUserEntityFromLogin_returnScopedUserWithScopesAndMergeDuplicatedScopes_whenScopedUserAndScopesFoundButSomeScopesWereDuplicates() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var duplicatedScope = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3, duplicatedScope);

        var projectCode1 = "prj1";
        var projectCode2 = "prj2";
        var projectCode3 = "prj3";
        var projectCode4 = "prj4";
        var projectCode5 = "prj5";
        var projectCode6 = "prj6";
        var projectCode7 = "prj7";
        var projectCode8 = "prj8";
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var project4 = mock(Project.class);
        var project5 = mock(Project.class);
        var project6 = mock(Project.class);
        var project7 = mock(Project.class);
        var project8 = mock(Project.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("scoped_user");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        when(scope1.getScope()).thenReturn("admin");
        when(scope1.getProjects()).thenReturn(List.of(projectCode1, projectCode2, projectCode3));
        when(scope2.getScope()).thenReturn("maintainer");
        when(scope2.getProjects()).thenReturn(List.of(projectCode4, projectCode5));
        when(scope3.getScope()).thenReturn("member");
        when(scope3.getProjects()).thenReturn(List.of(projectCode6));
        when(duplicatedScope.getScope()).thenReturn("ADMIN");
        when(duplicatedScope.getProjects()).thenReturn(List.of(projectCode7, projectCode8));

        when(projectRepository.findByCode(projectCode1)).thenReturn(Optional.of(project1));
        when(projectRepository.findByCode(projectCode2)).thenReturn(Optional.of(project2));
        when(projectRepository.findByCode(projectCode3)).thenReturn(Optional.of(project3));
        when(projectRepository.findByCode(projectCode4)).thenReturn(Optional.of(project4));
        when(projectRepository.findByCode(projectCode5)).thenReturn(Optional.of(project5));
        when(projectRepository.findByCode(projectCode6)).thenReturn(Optional.of(project6));
        when(projectRepository.findByCode(projectCode7)).thenReturn(Optional.of(project7));
        when(projectRepository.findByCode(projectCode8)).thenReturn(Optional.of(project8));

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser())
                .extracting(
                        "userEntity.providerName",
                        "userEntity.login",
                        "project",
                        "role"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                providerName,
                                login,
                                project1,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project2,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project3,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project4,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project5,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project6,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER
                        ),
                        tuple(
                                providerName,
                                login,
                                project7,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project8,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        )
                );
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, times(1)).findByCode(projectCode1);
        verify(projectRepository, times(1)).findByCode(projectCode2);
        verify(projectRepository, times(1)).findByCode(projectCode3);
        verify(projectRepository, times(1)).findByCode(projectCode4);
        verify(projectRepository, times(1)).findByCode(projectCode5);
        verify(projectRepository, times(1)).findByCode(projectCode6);
    }

    @Test
    void getMatchingUserEntityFromLogin_returnScopedUserWithScopesHavingAtLeastOneProject_whenScopedUserAndScopesFoundButSomeHavingNoProjects() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3);

        var projectCode1 = "prj1";
        var projectCode2 = "prj2";
        var projectCode3 = "prj3";
        var projectCode4 = "prj4";
        var projectCode5 = "prj5";
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var project4 = mock(Project.class);
        var project5 = mock(Project.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("scoped_user");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        when(scope1.getScope()).thenReturn("admin");
        when(scope1.getProjects()).thenReturn(List.of(projectCode1, projectCode2, projectCode3));
        when(scope2.getScope()).thenReturn("maintainer");
        when(scope2.getProjects()).thenReturn(List.of(projectCode4, projectCode5));
        when(scope3.getProjects()).thenReturn(null);

        when(projectRepository.findByCode(projectCode1)).thenReturn(Optional.of(project1));
        when(projectRepository.findByCode(projectCode2)).thenReturn(Optional.of(project2));
        when(projectRepository.findByCode(projectCode3)).thenReturn(Optional.of(project3));
        when(projectRepository.findByCode(projectCode4)).thenReturn(Optional.of(project4));
        when(projectRepository.findByCode(projectCode5)).thenReturn(Optional.of(project5));

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser())
                .extracting(
                        "userEntity.providerName",
                        "userEntity.login",
                        "project",
                        "role"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                providerName,
                                login,
                                project1,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project2,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project3,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project4,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project5,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        )
                );
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, times(1)).findByCode(projectCode1);
        verify(projectRepository, times(1)).findByCode(projectCode2);
        verify(projectRepository, times(1)).findByCode(projectCode3);
        verify(projectRepository, times(1)).findByCode(projectCode4);
        verify(projectRepository, times(1)).findByCode(projectCode5);
    }

    @Test
    void getMatchingUserEntityFromLogin_returnScopedUserWithScopesAndMergeDuplicatedProjects_whenScopedUserAndScopesFoundButSomeProjectsWereDuplicates() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3);

        var projectCode1 = "prj1";
        var projectCode2 = "prj2";
        var projectCode3 = "prj3";
        var projectCode4 = "prj4";
        var projectCode5 = "prj5";
        var projectCode6 = "prj6";
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var project4 = mock(Project.class);
        var project5 = mock(Project.class);
        var project6 = mock(Project.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("scoped_user");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        when(scope1.getScope()).thenReturn("admin");
        when(scope1.getProjects()).thenReturn(List.of(projectCode1, projectCode2, projectCode3, projectCode2, projectCode1));
        when(scope2.getScope()).thenReturn("maintainer");
        when(scope2.getProjects()).thenReturn(List.of(projectCode4, projectCode5, projectCode5));
        when(scope3.getScope()).thenReturn("member");
        when(scope3.getProjects()).thenReturn(List.of(projectCode6, projectCode6));

        when(projectRepository.findByCode(projectCode1)).thenReturn(Optional.of(project1));
        when(projectRepository.findByCode(projectCode2)).thenReturn(Optional.of(project2));
        when(projectRepository.findByCode(projectCode3)).thenReturn(Optional.of(project3));
        when(projectRepository.findByCode(projectCode4)).thenReturn(Optional.of(project4));
        when(projectRepository.findByCode(projectCode5)).thenReturn(Optional.of(project5));
        when(projectRepository.findByCode(projectCode6)).thenReturn(Optional.of(project6));

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser())
                .extracting(
                        "userEntity.providerName",
                        "userEntity.login",
                        "project",
                        "role"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                providerName,
                                login,
                                project1,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project2,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project3,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project4,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project5,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project6,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER
                        )
                );
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, times(1)).findByCode(projectCode1);
        verify(projectRepository, times(1)).findByCode(projectCode2);
        verify(projectRepository, times(1)).findByCode(projectCode3);
        verify(projectRepository, times(1)).findByCode(projectCode4);
        verify(projectRepository, times(1)).findByCode(projectCode5);
        verify(projectRepository, times(1)).findByCode(projectCode6);
    }

    @Test
    void getMatchingUserEntityFromLogin_returnScopedUserWithScopesAndCreateNewProjectsWhenNotFound_whenScopedUserAndScopesFoundButSomeProjectCodesWereUnknownAndCreatingNewProjectsOnInitOptionIsTrue() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3);

        var projectCode1 = "prj1";
        var projectCode2 = "prj2";
        var projectCode3 = "prj3";
        var projectCode4 = "prj4";
        var projectCode5 = "prj5";
        var projectCode6 = "prj6";
        var unknownProjectCode = "unknown-project_to   save         @&:1   )%*";
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var project4 = mock(Project.class);
        var project5 = mock(Project.class);
        var project6 = mock(Project.class);
        var newlySavedProject = mock(Project.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("scoped_user");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        when(scope1.getScope()).thenReturn("admin");
        when(scope1.getProjects()).thenReturn(List.of(projectCode1, projectCode2, projectCode3));
        when(scope2.getScope()).thenReturn("maintainer");
        when(scope2.getProjects()).thenReturn(List.of(projectCode4, projectCode5, unknownProjectCode));
        when(scope3.getScope()).thenReturn("member");
        when(scope3.getProjects()).thenReturn(List.of(projectCode6));

        when(projectRepository.findByCode(projectCode1)).thenReturn(Optional.of(project1));
        when(projectRepository.findByCode(projectCode2)).thenReturn(Optional.of(project2));
        when(projectRepository.findByCode(projectCode3)).thenReturn(Optional.of(project3));
        when(projectRepository.findByCode(projectCode4)).thenReturn(Optional.of(project4));
        when(projectRepository.findByCode(projectCode5)).thenReturn(Optional.of(project5));
        when(projectRepository.findByCode(projectCode6)).thenReturn(Optional.of(project6));
        when(projectRepository.findByCode(unknownProjectCode)).thenReturn(Optional.empty());

        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);

        when(usersConfiguration.getCreateNewProjectOnInit()).thenReturn(true);

        when(projectRepository.save(any(Project.class))).thenReturn(newlySavedProject);
        when(newlySavedProject.getCode()).thenReturn("retrieved-code");
        when(newlySavedProject.getName()).thenReturn("retrieved-name");
        when(newlySavedProject.getId()).thenReturn(123L);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser())
                .extracting(
                        "userEntity.providerName",
                        "userEntity.login",
                        "project.code",
                        "role"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                providerName,
                                login,
                                projectCode1,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode2,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode3,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode4,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode5,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode6,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER
                        ),
                        tuple(
                                providerName,
                                login,
                                "retrieved-code",
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        )
                );
        var savedProjects = user.get().getRolesOnProjectWhenScopedUser()
                .stream()
                .map(UserEntityRoleOnProject::getProject)
                .filter(project -> "retrieved-code".equals(project.getCode()))
                .toList();
        assertThat(savedProjects)
                .extracting(
                        "id",
                        "code",
                        "name"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                123L,
                                "retrieved-code",
                                "retrieved-name"
                        )
                );
        var savedProjectArgumentCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, times(1)).save(savedProjectArgumentCaptor.capture());
        assertThat(savedProjectArgumentCaptor.getValue())
                .extracting(
                        "code",
                        "name"
                )
                .contains(
                        unknownProjectCode,
                        "Unknown Project To Save 1 (generated)"
                );
        verify(projectRepository, times(1)).findByCode(projectCode1);
        verify(projectRepository, times(1)).findByCode(projectCode2);
        verify(projectRepository, times(1)).findByCode(projectCode3);
        verify(projectRepository, times(1)).findByCode(projectCode4);
        verify(projectRepository, times(1)).findByCode(projectCode5);
        verify(projectRepository, times(1)).findByCode(projectCode6);
    }

    @Test
    void getMatchingUserEntityFromLogin_returnScopedUserWithScopesAndIgnoreUnknownProjectCodes_whenScopedUserAndScopesFoundButSomeProjectCodesWereUnknownAndCreatingNewProjectsOnInitOptionIsFalse() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3);

        var projectCode1 = "prj1";
        var projectCode2 = "prj2";
        var projectCode3 = "prj3";
        var projectCode4 = "prj4";
        var projectCode5 = "prj5";
        var projectCode6 = "prj6";
        var unknownProjectCode = "unknown-project";
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var project4 = mock(Project.class);
        var project5 = mock(Project.class);
        var project6 = mock(Project.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("scoped_user");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        when(scope1.getScope()).thenReturn("admin");
        when(scope1.getProjects()).thenReturn(List.of(projectCode1, projectCode2, projectCode3));
        when(scope2.getScope()).thenReturn("maintainer");
        when(scope2.getProjects()).thenReturn(List.of(projectCode4, projectCode5, unknownProjectCode));
        when(scope3.getScope()).thenReturn("member");
        when(scope3.getProjects()).thenReturn(List.of(projectCode6));

        when(projectRepository.findByCode(projectCode1)).thenReturn(Optional.of(project1));
        when(projectRepository.findByCode(projectCode2)).thenReturn(Optional.of(project2));
        when(projectRepository.findByCode(projectCode3)).thenReturn(Optional.of(project3));
        when(projectRepository.findByCode(projectCode4)).thenReturn(Optional.of(project4));
        when(projectRepository.findByCode(projectCode5)).thenReturn(Optional.of(project5));
        when(projectRepository.findByCode(projectCode6)).thenReturn(Optional.of(project6));
        when(projectRepository.findByCode(unknownProjectCode)).thenReturn(Optional.empty());

        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(project4.getCode()).thenReturn(projectCode4);
        when(project5.getCode()).thenReturn(projectCode5);
        when(project6.getCode()).thenReturn(projectCode6);

        when(usersConfiguration.getCreateNewProjectOnInit()).thenReturn(false);

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser())
                .extracting(
                        "userEntity.providerName",
                        "userEntity.login",
                        "project.code",
                        "role"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                providerName,
                                login,
                                projectCode1,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode2,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode3,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode4,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode5,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                projectCode6,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER
                        )
                );
        var newProjectNames = user.get().getRolesOnProjectWhenScopedUser()
                .stream()
                .map(UserEntityRoleOnProject::getProject)
                .filter(project -> unknownProjectCode.equals(project.getCode()))
                .map(Project::getName)
                .toList();
        assertThat(newProjectNames).isEmpty();
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, times(1)).findByCode(projectCode1);
        verify(projectRepository, times(1)).findByCode(projectCode2);
        verify(projectRepository, times(1)).findByCode(projectCode3);
        verify(projectRepository, times(1)).findByCode(projectCode4);
        verify(projectRepository, times(1)).findByCode(projectCode5);
        verify(projectRepository, times(1)).findByCode(projectCode6);
    }

    @Test
    void getMatchingUserEntityFromLogin_returnScopedUserWithScopesKeepingHighestScope_whenScopedUserAndScopesFoundButSomeProjectHavingMultipleScopes() {
        // Given
        var providerName = "registration-name";
        var providerConfiguration = mock(ProviderConfiguration.class);

        var login = "user-login";
        var projectRepository = mock(ProjectRepository.class);
        var usersConfiguration = mock(UsersConfiguration.class);

        var profile1 = mock(UserProfileConfiguration.class);
        var profile2 = mock(UserProfileConfiguration.class);
        var profile3 = mock(UserProfileConfiguration.class);
        var matchingProfile = mock(UserProfileConfiguration.class);
        var profiles = List.of(profile1, profile2, profile3, matchingProfile);

        var scope1 = mock(UserScopeConfiguration.class);
        var scope2 = mock(UserScopeConfiguration.class);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopes = List.of(scope1, scope2, scope3);

        var projectCode1 = "prj1";
        var projectCode2 = "prj2";
        var projectCode3 = "prj3";
        var projectCode4 = "prj4";
        var projectCode5 = "prj5";
        var projectCode6 = "prj6";
        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var project4 = mock(Project.class);
        var project5 = mock(Project.class);
        var project6 = mock(Project.class);

        ReflectionTestUtils.setField(setupConfiguration, "users", usersConfiguration);
        ReflectionTestUtils.setField(setupConfiguration, "provider", providerConfiguration);

        // When
        when(providerConfiguration.getRegistration()).thenReturn(providerName);
        when(usersConfiguration.getProfiles()).thenReturn(profiles);
        when(profile1.getLogin()).thenReturn("login1");
        when(profile2.getLogin()).thenReturn("login2");
        when(profile3.getLogin()).thenReturn("login3");

        when(matchingProfile.getLogin()).thenReturn(login);
        when(matchingProfile.getProfile()).thenReturn("scoped_user");
        when(matchingProfile.getScopes()).thenReturn(scopes);

        when(scope1.getScope()).thenReturn("admin");
        when(scope1.getProjects()).thenReturn(List.of(projectCode1, projectCode2, projectCode3, projectCode5));
        when(scope2.getScope()).thenReturn("maintainer");
        when(scope2.getProjects()).thenReturn(List.of(projectCode4, projectCode5, projectCode6, projectCode1));
        when(scope3.getScope()).thenReturn("member");
        when(scope3.getProjects()).thenReturn(List.of(projectCode6));

        when(projectRepository.findByCode(projectCode1)).thenReturn(Optional.of(project1));
        when(projectRepository.findByCode(projectCode2)).thenReturn(Optional.of(project2));
        when(projectRepository.findByCode(projectCode3)).thenReturn(Optional.of(project3));
        when(projectRepository.findByCode(projectCode4)).thenReturn(Optional.of(project4));
        when(projectRepository.findByCode(projectCode5)).thenReturn(Optional.of(project5));
        when(projectRepository.findByCode(projectCode6)).thenReturn(Optional.of(project6));

        // Then
        var user = setupConfiguration.getMatchingUserEntityFromLogin(login, projectRepository);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "providerName",
                        "login",
                        "profile"
                )
                .contains(
                        providerName,
                        login,
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(user.get().getRolesOnProjectWhenScopedUser())
                .extracting(
                        "userEntity.providerName",
                        "userEntity.login",
                        "project",
                        "role"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                providerName,
                                login,
                                project1,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project2,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project3,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project4,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        ),
                        tuple(
                                providerName,
                                login,
                                project5,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN
                        ),
                        tuple(
                                providerName,
                                login,
                                project6,
                                UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER
                        )
                );
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectRepository, times(1)).findByCode(projectCode1);
        verify(projectRepository, times(1)).findByCode(projectCode2);
        verify(projectRepository, times(1)).findByCode(projectCode3);
        verify(projectRepository, times(1)).findByCode(projectCode4);
        verify(projectRepository, times(1)).findByCode(projectCode5);
        verify(projectRepository, times(1)).findByCode(projectCode6);
    }
}
