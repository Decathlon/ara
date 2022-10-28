package com.decathlon.ara.security.service;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.provider.ProviderConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OAuth2ProvidersConfiguration providersConfiguration;
    
    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void getAuthenticationConfiguration_returnAuthenticationProvidersDTO_whenAllFieldsAreNotNull() {
        // Given
        var loginUrl = "/login/url";
        var logoutUrl = "/logout/url";
        ReflectionTestUtils.setField(authenticationService, "loginStartingUrl", loginUrl);
        ReflectionTestUtils.setField(authenticationService, "logoutProcessingUrl", logoutUrl);

        var providerSetup1 = mock(ProviderSetupConfiguration.class);
        var provider1 = mock(ProviderConfiguration.class);
        var providerSetup2 = mock(ProviderSetupConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var providerSetup3 = mock(ProviderSetupConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var setup = List.of(providerSetup1, providerSetup2, providerSetup3);

        // When
        when(providersConfiguration.getSetup()).thenReturn(setup);
        when(providerSetup1.getProvider()).thenReturn(provider1);
        when(provider1.getDisplayValue()).thenReturn("Provider1");
        when(provider1.getType()).thenReturn("custom");
        when(provider1.getRegistration()).thenReturn("registration1");
        when(providerSetup2.getProvider()).thenReturn(provider2);
        when(provider2.getDisplayValue()).thenReturn("Provider2");
        when(provider2.getType()).thenReturn("google");
        when(provider2.getRegistration()).thenReturn("registration2");
        when(providerSetup3.getProvider()).thenReturn(provider3);
        when(provider3.getDisplayValue()).thenReturn("Provider3");
        when(provider3.getType()).thenReturn("github");
        when(provider3.getRegistration()).thenReturn("registration3");

        // Then
        var authenticationConfiguration = authenticationService.getAuthenticationConfiguration();
        assertThat(authenticationConfiguration)
                .extracting(
                        "loginUrl",
                        "logoutUrl"
                )
                .contains(
                        loginUrl,
                        logoutUrl
                );
        assertThat(authenticationConfiguration.getProviders())
                .extracting(
                        "displayValue",
                        "type",
                        "name"
                )
                .contains(
                        tuple(
                                "Provider1",
                                "custom",
                                "registration1"
                        ),
                        tuple(
                                "Provider2",
                                "google",
                                "registration2"
                        ),
                        tuple(
                                "Provider3",
                                "github",
                                "registration3"
                        )
                );
    }

    @Test
    void getAuthenticationConfiguration_returnAuthenticationProvidersDTOAndIgnoreNullProviders_whenSomeProvidersAreNull() {
        // Given
        var loginUrl = "/login/url";
        var logoutUrl = "/logout/url";
        ReflectionTestUtils.setField(authenticationService, "loginStartingUrl", loginUrl);
        ReflectionTestUtils.setField(authenticationService, "logoutProcessingUrl", logoutUrl);

        var providerSetup1 = mock(ProviderSetupConfiguration.class);
        var provider1 = mock(ProviderConfiguration.class);
        var providerSetup2 = mock(ProviderSetupConfiguration.class);
        var providerSetup3 = mock(ProviderSetupConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var setup = List.of(providerSetup1, providerSetup2, providerSetup3);

        // When
        when(providersConfiguration.getSetup()).thenReturn(setup);
        when(providerSetup1.getProvider()).thenReturn(provider1);
        when(provider1.getDisplayValue()).thenReturn("Provider1");
        when(provider1.getType()).thenReturn("custom");
        when(provider1.getRegistration()).thenReturn("registration1");
        when(providerSetup2.getProvider()).thenReturn(null);
        when(providerSetup3.getProvider()).thenReturn(provider3);
        when(provider3.getDisplayValue()).thenReturn("Provider3");
        when(provider3.getType()).thenReturn("github");
        when(provider3.getRegistration()).thenReturn("registration3");

        // Then
        var authenticationConfiguration = authenticationService.getAuthenticationConfiguration();
        assertThat(authenticationConfiguration)
                .extracting(
                        "loginUrl",
                        "logoutUrl"
                )
                .contains(
                        loginUrl,
                        logoutUrl
                );
        assertThat(authenticationConfiguration.getProviders())
                .extracting(
                        "displayValue",
                        "type",
                        "name"
                )
                .contains(
                        tuple(
                                "Provider1",
                                "custom",
                                "registration1"
                        ),
                        tuple(
                                "Provider3",
                                "github",
                                "registration3"
                        )
                );
    }

    @Test
    void getAuthenticationConfiguration_returnAuthenticationProvidersDTO_whenSetupIsNull() {
        // Given

        // When
        when(providersConfiguration.getSetup()).thenReturn(null);

        // Then
        var authenticationConfiguration = authenticationService.getAuthenticationConfiguration();
        assertThat(authenticationConfiguration.getProviders())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void manageUserAtLogin_returnDatabaseMatchingAuthorities_whenUserFoundInDatabase() {
        // Given
        var oidcUser = mock(OidcUser.class);

        var login = "user_login";
        var provider = "provider_name";

        var userFoundInDatabase = mock(UserEntity.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authorities = Set.of(authority1, authority2, authority3);

        // When
        when(oidcUser.getSubject()).thenReturn(login);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.of(userFoundInDatabase));
        when(userFoundInDatabase.getMatchingAuthorities()).thenReturn(authorities);

        // Then
        var returnedAuthorities = authenticationService.manageUserAtLogin(oidcUser, provider);
        assertThat(returnedAuthorities).containsExactlyInAnyOrder(authority1, authority2, authority3);
        verify(userEntityRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void manageUserAtLogin_returnConfigurationMatchingAuthorities_whenUserNotFoundInDatabaseButFoundInConfigurationFile() {
        // Given
        var oidcUser = mock(OidcUser.class);

        var login = "user_login";
        var provider = "provider_name";
        var email = "user@email.com";
        var firstName = "user_firstName";
        var lastName = "user_lastName";

        var userCreatedFromConfigurationFile = mock(UserEntity.class);
        var authority1 = mock(GrantedAuthority.class);
        var authority2 = mock(GrantedAuthority.class);
        var authority3 = mock(GrantedAuthority.class);
        var authorities = Set.of(authority1, authority2, authority3);

        // When
        when(oidcUser.getSubject()).thenReturn(login);
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getGivenName()).thenReturn(firstName);
        when(oidcUser.getFamilyName()).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(provider, login, projectRepository)).thenReturn(Optional.of(userCreatedFromConfigurationFile));
        when(userCreatedFromConfigurationFile.getMatchingAuthorities()).thenReturn(authorities);

        // Then
        var returnedAuthorities = authenticationService.manageUserAtLogin(oidcUser, provider);
        assertThat(returnedAuthorities).containsExactlyInAnyOrder(authority1, authority2, authority3);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave).isSameAs(userCreatedFromConfigurationFile);
        verify(userToSave).setEmail(email);
        verify(userToSave).setFirstName(firstName);
        verify(userToSave).setLastName(lastName);
    }

    @Test
    void manageUserAtLogin_returnNewUserMatchingAuthorities_whenUserNeitherFoundInDatabaseNorInConfigurationFile() {
        // Given
        var oidcUser = mock(OidcUser.class);

        var login = "user_login";
        var provider = "provider_name";
        var email = "user@email.com";
        var firstName = "user_firstName";
        var lastName = "user_lastName";

        // When
        when(oidcUser.getSubject()).thenReturn(login);
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getGivenName()).thenReturn(firstName);
        when(oidcUser.getFamilyName()).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(provider, login, projectRepository)).thenReturn(Optional.empty());

        // Then
        var actualAuthorities = authenticationService.manageUserAtLogin(oidcUser, provider);
        var expectedAuthority = UserEntity.UserEntityProfile.SCOPED_USER.getMatchingAuthority();
        assertThat(actualAuthorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(expectedAuthority.getAuthority());

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName"
                )
                .contains(
                        login,
                        provider,
                        Optional.of(email),
                        Optional.of(firstName),
                        Optional.of(lastName)
                );
    }

    @Test
    void manageUserAtLogin_returnNewUserMatchingAuthoritiesAndFetchAdditionalDetailsFromAttributes_whenUserNeitherFoundInDatabaseNorInConfigurationFileAndOidcUserDoesNotContainAdditionalDetails() {
        // Given
        var oidcUser = mock(OidcUser.class);

        var login = "user_login";
        var provider = "provider_name";
        var email = "user@email.com";
        var firstName = "user_firstName";
        var lastName = "user_lastName";

        // When
        when(oidcUser.getSubject()).thenReturn(login);
        when(oidcUser.getEmail()).thenReturn(null);
        when(oidcUser.getGivenName()).thenReturn(null);
        when(oidcUser.getFamilyName()).thenReturn(null);
        when(oidcUser.getAttribute("email")).thenReturn(email);
        when(oidcUser.getAttribute("familyName")).thenReturn(firstName);
        when(oidcUser.getAttribute("givenName")).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(provider, login, projectRepository)).thenReturn(Optional.empty());

        // Then
        var actualAuthorities = authenticationService.manageUserAtLogin(oidcUser, provider);
        var expectedAuthority = UserEntity.UserEntityProfile.SCOPED_USER.getMatchingAuthority();
        assertThat(actualAuthorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(expectedAuthority.getAuthority());

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName"
                )
                .contains(
                        login,
                        provider,
                        Optional.of(email),
                        Optional.of(firstName),
                        Optional.of(lastName)
                );
    }

    @Test
    void manageUserAtLogin_returnNewUserMatchingAuthoritiesAndFetchAdditionalDetailsFromCustomAttributes_whenUserNeitherFoundInDatabaseNorInConfigurationFileAndNeitherOidcUserContainsAdditionalDetailsNorItsAttributes() {
        // Given
        var oidcUser = mock(OidcUser.class);

        var login = "user_login";
        var providerName = "provider_name";
        var email = "user@email.com";
        var firstName = "user_firstName";
        var lastName = "user_lastName";

        var emailField = "email";
        var familyNameField = "familyName";
        var givenNameField = "givenName";

        var customEmailField = "customEmail";
        var customFamilyNameField = "customFamilyName";
        var customGivenNameField = "customGivenName";

        // When
        when(oidcUser.getSubject()).thenReturn(login);
        when(oidcUser.getEmail()).thenReturn(null);
        when(oidcUser.getGivenName()).thenReturn(null);
        when(oidcUser.getFamilyName()).thenReturn(null);
        when(oidcUser.getAttribute(emailField)).thenReturn(null);
        when(oidcUser.getAttribute(familyNameField)).thenReturn(null);
        when(oidcUser.getAttribute(givenNameField)).thenReturn(null);
        when(oidcUser.getAttribute(customEmailField)).thenReturn(email);
        when(oidcUser.getAttribute(customFamilyNameField)).thenReturn(firstName);
        when(oidcUser.getAttribute(customGivenNameField)).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, providerName))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, login, projectRepository)).thenReturn(Optional.empty());
        when(providersConfiguration.getUsersCustomAttributesFromProviderName(providerName)).thenReturn(
                Map.ofEntries(
                        entry(emailField, customEmailField),
                        entry(familyNameField, customFamilyNameField),
                        entry(givenNameField, customGivenNameField)
                )
        );

        // Then
        var actualAuthorities = authenticationService.manageUserAtLogin(oidcUser, providerName);
        var expectedAuthority = UserEntity.UserEntityProfile.SCOPED_USER.getMatchingAuthority();
        assertThat(actualAuthorities)
                .extracting("authority")
                .containsExactlyInAnyOrder(expectedAuthority.getAuthority());

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName"
                )
                .contains(
                        login,
                        providerName,
                        Optional.of(email),
                        Optional.of(firstName),
                        Optional.of(lastName)
                );
    }
}
