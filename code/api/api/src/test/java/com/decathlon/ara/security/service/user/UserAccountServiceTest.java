package com.decathlon.ara.security.service.user;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.account.UserProjectScope;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.account.UserRepository;
import com.decathlon.ara.repository.security.member.user.account.UserProjectScopeRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserScopeConfiguration;
import com.decathlon.ara.security.dto.authentication.user.AuthenticatedOAuth2User;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.mapper.UserMapper;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.ProjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;
import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProjectScopeRepository userProjectScopeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OAuth2ProvidersConfiguration providersConfiguration;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private UserAccountService userAccountService;

    @Test
    void getDefaultOAuth2UserService_returnDefaultOAuth2UserService() {
        // Given

        // When
        
        // Then
        var service = userAccountService.getDefaultOAuth2UserService();
        assertThat(service).isNotNull().isExactlyInstanceOf(DefaultOAuth2UserService.class);
    }

    @Test
    void getOidcUserService_returnOidcUserService() {
        // Given

        // When

        // Then
        var service = userAccountService.getOidcUserService();
        assertThat(service).isNotNull().isExactlyInstanceOf(OidcUserService.class);
    }

    @Test
    void getCurrentUser_returnEmptyOptional_whenUserNotFoundInSession() {
        // Given

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.empty());

        // Then
        var fetchedUser = userAccountService.getCurrentUser();
        assertThat(fetchedUser).isNotPresent();
    }

    @Test
    void getCurrentUser_returnEmptyOptional_whenUserNotFoundInDatabase() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        var fetchedUser = userAccountService.getCurrentUser();
        assertThat(fetchedUser).isNotPresent();
    }

    @Test
    void getCurrentUser_returnUser_whenUserFound() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        // Then
        var fetchedUser = userAccountService.getCurrentUser();
        assertThat(fetchedUser).containsSame(user);
    }

    @Test
    void getCurrentUserAccountFromAuthenticatedOAuth2User_returnEmptyOptional_whenUserNotFoundInDatabase() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccountFromAuthenticatedOAuth2User(authenticatedUser);
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccountFromAuthenticatedOAuth2User_returnUserAccount_whenUserFound() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var user = mock(User.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(userMapper.getFullScopeAccessUserAccountFromUser(user)).thenReturn(matchingUserAccount);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccountFromAuthenticatedOAuth2User(authenticatedUser);
        assertThat(fetchedUserAccount).containsSame(matchingUserAccount);
    }

    @Test
    void getCurrentUserAccountFromAuthentication_returnEmptyOptional_whenUserNotFoundInSession() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2UserFromAuthentication(authentication)).thenReturn(Optional.empty());

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccountFromAuthentication(authentication);
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccountFromAuthentication_returnEmptyOptional_whenUserNotFoundInDatabase() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2UserFromAuthentication(authentication)).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccountFromAuthentication(authentication);
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccountFromAuthentication_returnUserAccount_whenUserFound() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var user = mock(User.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2UserFromAuthentication(authentication)).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(userMapper.getFullScopeAccessUserAccountFromUser(user)).thenReturn(matchingUserAccount);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccountFromAuthentication(authentication);
        assertThat(fetchedUserAccount).containsSame(matchingUserAccount);
    }

    @Test
    void getCurrentUserAccount_returnEmptyOptional_whenUserNotFoundInSession() {
        // Given

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.empty());

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount();
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccount_returnEmptyOptional_whenUserNotFoundInDatabase() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount();
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccount_returnUserAccount_whenUserFound() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var user = mock(User.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(userMapper.getFullScopeAccessUserAccountFromUser(user)).thenReturn(matchingUserAccount);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount();
        assertThat(fetchedUserAccount).containsSame(matchingUserAccount);
    }

    @Test
    void createUserAccountFromAuthenticatedOAuth2User_throwForbiddenException_whenUserAlreadyExists() throws NotUniqueException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.existsById(new User.UserId(providerName, userLogin))).thenReturn(true);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.createUserAccountFromAuthenticatedOAuth2User(authenticatedUser));
        verify(userRepository, never()).save(any());
        verify(projectService, never()).createFromCode(anyString(), any());
        verify(userProjectScopeRepository, never()).saveAll(any());
    }

    @Test
    void createUserAccountFromAuthenticatedOAuth2User_saveUserAsScopedUser_whenProfileConfigurationNotFound() throws NotUniqueException, ForbiddenException {
        // Given
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var savedUser = mock(User.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.existsById(new User.UserId(providerName, userLogin))).thenReturn(false);
        when(authenticatedUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(authenticatedUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(authenticatedUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(authenticatedUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(providersConfiguration.getUserProfileConfiguration(providerName, userLogin)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.getFullScopeAccessUserAccountFromUser(savedUser)).thenReturn(matchingUserAccount);

        // Then
        var savedUserAccount = userAccountService.createUserAccountFromAuthenticatedOAuth2User(authenticatedUser);
        assertThat(savedUserAccount).isSameAs(matchingUserAccount);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName",
                        "pictureUrl",
                        "profile"
                )
                .contains(
                        userLogin,
                        providerName,
                        Optional.of(userEmail),
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        Optional.of(userPictureUrl),
                        UserProfile.SCOPED_USER
                );
        assertThat(userToSave.getScopes()).isEmpty();

        verify(projectService, never()).createFromCode(anyString(), any());
        verify(userProjectScopeRepository, never()).saveAll(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN", "super_admin", "SuPeR_AdMiN",
            "AUDITOR", "auditor", "AuDiToR",
            "SCOPED_USER", "scoped_user", "ScOpEd_uSeR"
    })
    void createUserAccountFromAuthenticatedOAuth2User_getProfileAndRolesFromConfigurationAndSaveUserWithoutCreatingUnknownProjects_whenProfileConfigurationIsFoundAndProjectCreationOptionIsFalse(String profileAsString) throws NotUniqueException, ForbiddenException {
        // Given
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var existingProjectCode1 = "existing-project-1";
        var existingProjectCode2 = "existing-project-2";
        var existingProjectCode3 = "existing-project-3";
        var unknownProjectCode1 = "unknown-project-1";
        var unknownProjectCode2 = "unknown-project-2";
        var unknownProjectCode3 = "unknown-project-3";

        var existingProject1 = mock(Project.class);
        var existingProject2 = mock(Project.class);
        var existingProject3 = mock(Project.class);

        var userProfileConfiguration = mock(UserProfileConfiguration.class);
        var scope1 = mock(UserScopeConfiguration.class);
        var scopeAsString1 = "aDmIn";
        var projectCodes1 = List.of(existingProjectCode1, unknownProjectCode1, existingProjectCode2);
        var scope2 = mock(UserScopeConfiguration.class);
        var scopeAsString2 = "MaInTaInEr";
        var projectCodes2 = List.of(existingProjectCode3, unknownProjectCode2);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopeAsString3 = "mEmBeR";
        var projectCodes3 = List.of(unknownProjectCode3);
        var scopes = List.of(scope1, scope2, scope3);

        var savedUser = mock(User.class);
        var savedScope1 = mock(UserProjectScope.class);
        var savedScope2 = mock(UserProjectScope.class);
        var savedScope3 = mock(UserProjectScope.class);
        var savedScopes = List.of(savedScope1, savedScope2, savedScope3);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.existsById(new User.UserId(providerName, userLogin))).thenReturn(false);
        when(authenticatedUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(authenticatedUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(authenticatedUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(authenticatedUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(providersConfiguration.getUserProfileConfiguration(providerName, userLogin)).thenReturn(Optional.of(userProfileConfiguration));
        when(userProfileConfiguration.getProfile()).thenReturn(profileAsString);
        when(userProfileConfiguration.getScopes()).thenReturn(scopes);
        when(scope1.getScope()).thenReturn(scopeAsString1);
        when(scope1.getProjects()).thenReturn(projectCodes1);
        when(scope2.getScope()).thenReturn(scopeAsString2);
        when(scope2.getProjects()).thenReturn(projectCodes2);
        when(scope3.getScope()).thenReturn(scopeAsString3);
        when(scope3.getProjects()).thenReturn(projectCodes3);
        when(providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName)).thenReturn(false);

        when(projectRepository.findByCode(existingProjectCode1)).thenReturn(Optional.of(existingProject1));
        when(projectRepository.findByCode(existingProjectCode2)).thenReturn(Optional.of(existingProject2));
        when(projectRepository.findByCode(existingProjectCode3)).thenReturn(Optional.of(existingProject3));
        when(projectRepository.findByCode(unknownProjectCode1)).thenReturn(Optional.empty());
        when(projectRepository.findByCode(unknownProjectCode2)).thenReturn(Optional.empty());
        when(projectRepository.findByCode(unknownProjectCode3)).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userProjectScopeRepository.saveAll(any())).thenReturn(savedScopes);
        when(userMapper.getFullScopeAccessUserAccountFromUser(savedUser)).thenReturn(matchingUserAccount);

        // Then
        var savedUserAccount = userAccountService.createUserAccountFromAuthenticatedOAuth2User(authenticatedUser);
        assertThat(savedUserAccount).isSameAs(matchingUserAccount);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName",
                        "pictureUrl",
                        "profile"
                )
                .contains(
                        userLogin,
                        providerName,
                        Optional.of(userEmail),
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        Optional.of(userPictureUrl),
                        UserProfile.valueOf(profileAsString.toUpperCase())
                );
        ArgumentCaptor<List<UserProjectScope>> userRolesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(userProjectScopeRepository).saveAll(userRolesArgumentCaptor.capture());
        assertThat(userRolesArgumentCaptor.getValue())
                .extracting("user", "project", "role")
                .containsExactlyInAnyOrder(
                        tuple(savedUser, existingProject1, ProjectRole.ADMIN),
                        tuple(savedUser, existingProject2, ProjectRole.ADMIN),
                        tuple(savedUser, existingProject3, ProjectRole.MAINTAINER)
                );
        verify(projectService, never()).createFromCode(anyString(), any());

        ArgumentCaptor<Set<UserProjectScope>> savedUserProjectScopeArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(savedUser).setScopes(savedUserProjectScopeArgumentCaptor.capture());
        assertThat(savedUserProjectScopeArgumentCaptor.getValue()).containsExactlyInAnyOrder(savedScope1, savedScope2, savedScope3);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN", "super_admin", "SuPeR_AdMiN",
            "AUDITOR", "auditor", "AuDiToR",
            "SCOPED_USER", "scoped_user", "ScOpEd_uSeR"
    })
    void createUserAccountFromAuthenticatedOAuth2User_getProfileAndRolesFromConfigurationAndSaveUserWhileCreatingUnknownProjects_whenProfileConfigurationIsFoundAndProjectCreationOptionIsTrue(String profileAsString) throws NotUniqueException, ForbiddenException {
        // Given
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var existingProjectCode1 = "existing-project-1";
        var existingProjectCode2 = "existing-project-2";
        var existingProjectCode3 = "existing-project-3";
        var unknownProjectCode1 = "unknown-project-1";
        var unknownProjectCode2 = "unknown-project-2";
        var unknownProjectCode3 = "unknown-project-3";

        var existingProject1 = mock(Project.class);
        var existingProject2 = mock(Project.class);
        var existingProject3 = mock(Project.class);
        var unknownProject1 = mock(Project.class);
        var unknownProject2 = mock(Project.class);
        var unknownProject3 = mock(Project.class);

        var userProfileConfiguration = mock(UserProfileConfiguration.class);
        var scope1 = mock(UserScopeConfiguration.class);
        var scopeAsString1 = "aDmIn";
        var projectCodes1 = List.of(existingProjectCode1, unknownProjectCode1, existingProjectCode2);
        var scope2 = mock(UserScopeConfiguration.class);
        var scopeAsString2 = "MaInTaInEr";
        var projectCodes2 = List.of(existingProjectCode3, unknownProjectCode2);
        var scope3 = mock(UserScopeConfiguration.class);
        var scopeAsString3 = "mEmBeR";
        var projectCodes3 = List.of(unknownProjectCode3);
        var scopes = List.of(scope1, scope2, scope3);

        var savedUser = mock(User.class);
        var savedScope1 = mock(UserProjectScope.class);
        var savedScope2 = mock(UserProjectScope.class);
        var savedScope3 = mock(UserProjectScope.class);
        var savedScopes = List.of(savedScope1, savedScope2, savedScope3);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.existsById(new User.UserId(providerName, userLogin))).thenReturn(false);
        when(authenticatedUser.getFirstName()).thenReturn(Optional.of(userFirstName));
        when(authenticatedUser.getLastName()).thenReturn(Optional.of(userLastName));
        when(authenticatedUser.getEmail()).thenReturn(Optional.of(userEmail));
        when(authenticatedUser.getPictureUrl()).thenReturn(Optional.of(userPictureUrl));

        when(providersConfiguration.getUserProfileConfiguration(providerName, userLogin)).thenReturn(Optional.of(userProfileConfiguration));
        when(userProfileConfiguration.getProfile()).thenReturn(profileAsString);
        when(userProfileConfiguration.getScopes()).thenReturn(scopes);
        when(scope1.getScope()).thenReturn(scopeAsString1);
        when(scope1.getProjects()).thenReturn(projectCodes1);
        when(scope2.getScope()).thenReturn(scopeAsString2);
        when(scope2.getProjects()).thenReturn(projectCodes2);
        when(scope3.getScope()).thenReturn(scopeAsString3);
        when(scope3.getProjects()).thenReturn(projectCodes3);
        when(providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName)).thenReturn(true);

        when(projectRepository.findByCode(existingProjectCode1)).thenReturn(Optional.of(existingProject1));
        when(projectRepository.findByCode(existingProjectCode2)).thenReturn(Optional.of(existingProject2));
        when(projectRepository.findByCode(existingProjectCode3)).thenReturn(Optional.of(existingProject3));
        when(projectRepository.findByCode(unknownProjectCode1)).thenReturn(Optional.empty(), Optional.of(unknownProject1));
        when(projectRepository.findByCode(unknownProjectCode2)).thenReturn(Optional.empty(), Optional.of(unknownProject2));
        when(projectRepository.findByCode(unknownProjectCode3)).thenReturn(Optional.empty(), Optional.of(unknownProject3));

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userProjectScopeRepository.saveAll(any())).thenReturn(savedScopes);
        when(userMapper.getFullScopeAccessUserAccountFromUser(savedUser)).thenReturn(matchingUserAccount);

        // Then
        var savedUserAccount = userAccountService.createUserAccountFromAuthenticatedOAuth2User(authenticatedUser);
        assertThat(savedUserAccount).isSameAs(matchingUserAccount);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName",
                        "pictureUrl",
                        "profile"
                )
                .contains(
                        userLogin,
                        providerName,
                        Optional.of(userEmail),
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        Optional.of(userPictureUrl),
                        UserProfile.valueOf(profileAsString.toUpperCase())
                );
        ArgumentCaptor<List<UserProjectScope>> userRolesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(userProjectScopeRepository).saveAll(userRolesArgumentCaptor.capture());
        assertThat(userRolesArgumentCaptor.getValue())
                .extracting("user", "project", "role")
                .containsExactlyInAnyOrder(
                        tuple(savedUser, existingProject1, ProjectRole.ADMIN),
                        tuple(savedUser, existingProject2, ProjectRole.ADMIN),
                        tuple(savedUser, unknownProject1, ProjectRole.ADMIN),
                        tuple(savedUser, existingProject3, ProjectRole.MAINTAINER),
                        tuple(savedUser, unknownProject2, ProjectRole.MAINTAINER),
                        tuple(savedUser, unknownProject3, ProjectRole.MEMBER)
                );
        var unknownProjectCodeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(projectService, times(3)).createFromCode(unknownProjectCodeArgumentCaptor.capture(), eq(savedUser));
        assertThat(unknownProjectCodeArgumentCaptor.getAllValues()).containsExactlyInAnyOrder(unknownProjectCode1, unknownProjectCode2, unknownProjectCode3);

        ArgumentCaptor<Set<UserProjectScope>> savedUserProjectScopeArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(savedUser).setScopes(savedUserProjectScopeArgumentCaptor.capture());
        assertThat(savedUserProjectScopeArgumentCaptor.getValue()).containsExactlyInAnyOrder(savedScope1, savedScope2, savedScope3);
    }

    @Test
    void getCurrentUserProjects_throwForbiddenException_whenUserNotFound() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.getCurrentUserProjects());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void getCurrentUserProjects_throwForbiddenException_whenUserHasNoProfile() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(null);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.getCurrentUserProjects());
        verifyNoInteractions(projectRepository);
    }

    @ParameterizedTest
    @EnumSource(
            value = UserProfile.class,
            names = {"SUPER_ADMIN", "AUDITOR"}
    )
    void getCurrentUserProjects_returnAllProjects_whenUserIsEitherSuperAdminOrAuditor(UserProfile profile) throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var allProjects = List.of(project1, project2, project3);

        var mappedProject1 = mock(ProjectDTO.class);
        var mappedProject2 = mock(ProjectDTO.class);
        var mappedProject3 = mock(ProjectDTO.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(profile);
        when(projectRepository.findAllByOrderByName()).thenReturn(allProjects);
        when(projectMapper.getProjectDTOFromProject(project1)).thenReturn(mappedProject1);
        when(projectMapper.getProjectDTOFromProject(project2)).thenReturn(mappedProject2);
        when(projectMapper.getProjectDTOFromProject(project3)).thenReturn(mappedProject3);

        // Then
        var actualProjects = userAccountService.getCurrentUserProjects();

        var expectedProjects = List.of(mappedProject1, mappedProject2, mappedProject3);
        assertThat(actualProjects).containsExactlyInAnyOrderElementsOf(expectedProjects);
        verify(projectRepository, never()).findByCode(DEMO_PROJECT_CODE);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void getCurrentUserProjects_returnProjectsInCurrentUserScopesWithoutDemoProject_whenUserIsScopedUserButDemoProjectNotFound() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);
        var profile = UserProfile.SCOPED_USER;

        var userScope1 = mock(UserProjectScope.class);
        var userScope2 = mock(UserProjectScope.class);
        var userScope3 = mock(UserProjectScope.class);
        var userScopes = Set.of(userScope1, userScope2, userScope3);

        var projectName1 = "p2";
        var projectName2 = "p3";
        var projectName3 = "p1";

        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);

        var mappedProject1 = mock(ProjectDTO.class);
        var mappedProject2 = mock(ProjectDTO.class);
        var mappedProject3 = mock(ProjectDTO.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(profile);
        when(user.getScopes()).thenReturn(userScopes);
        when(userScope1.getProject()).thenReturn(project1);
        when(project1.getName()).thenReturn(projectName1);
        when(userScope2.getProject()).thenReturn(project2);
        when(project2.getName()).thenReturn(projectName2);
        when(userScope3.getProject()).thenReturn(project3);
        when(project3.getName()).thenReturn(projectName3);
        when(projectRepository.findByCode(DEMO_PROJECT_CODE)).thenReturn(Optional.empty());
        when(projectMapper.getProjectDTOFromProject(project1)).thenReturn(mappedProject1);
        when(projectMapper.getProjectDTOFromProject(project2)).thenReturn(mappedProject2);
        when(projectMapper.getProjectDTOFromProject(project3)).thenReturn(mappedProject3);

        // Then
        var actualProjects = userAccountService.getCurrentUserProjects();

        assertThat(actualProjects).containsExactly(mappedProject3, mappedProject1, mappedProject2);
        verify(projectRepository, never()).findAllByOrderByName();
        verify(projectRepository, times(1)).findByCode(DEMO_PROJECT_CODE);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void getCurrentUserProjects_returnProjectsInCurrentUserScopesWithDemoProject_whenUserIsScopedUserAndDemoProjectIsFound() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);
        var profile = UserProfile.SCOPED_USER;

        var userScope1 = mock(UserProjectScope.class);
        var userScope2 = mock(UserProjectScope.class);
        var userScope3 = mock(UserProjectScope.class);
        var userScopes = Set.of(userScope1, userScope2, userScope3);

        var projectName1 = "p2";
        var projectName2 = "p3";
        var projectName3 = "p1";

        var project1 = mock(Project.class);
        var project2 = mock(Project.class);
        var project3 = mock(Project.class);
        var demoProject = mock(Project.class);

        var mappedProject1 = mock(ProjectDTO.class);
        var mappedProject2 = mock(ProjectDTO.class);
        var mappedProject3 = mock(ProjectDTO.class);
        var mappedDemoProject = mock(ProjectDTO.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(profile);
        when(user.getScopes()).thenReturn(userScopes);
        when(userScope1.getProject()).thenReturn(project1);
        when(project1.getName()).thenReturn(projectName1);
        when(userScope2.getProject()).thenReturn(project2);
        when(project2.getName()).thenReturn(projectName2);
        when(userScope3.getProject()).thenReturn(project3);
        when(project3.getName()).thenReturn(projectName3);
        when(projectRepository.findByCode(DEMO_PROJECT_CODE)).thenReturn(Optional.of(demoProject));
        when(demoProject.getName()).thenReturn(DEMO_PROJECT_NAME);
        when(projectMapper.getProjectDTOFromProject(project1)).thenReturn(mappedProject1);
        when(projectMapper.getProjectDTOFromProject(project2)).thenReturn(mappedProject2);
        when(projectMapper.getProjectDTOFromProject(project3)).thenReturn(mappedProject3);
        when(projectMapper.getProjectDTOFromProject(demoProject)).thenReturn(mappedDemoProject);

        // Then
        var actualProjects = userAccountService.getCurrentUserProjects();

        assertThat(actualProjects).containsExactly(mappedDemoProject, mappedProject3, mappedProject1, mappedProject2);
        verify(projectRepository, never()).findAllByOrderByName();
        verify(projectRepository, times(1)).findByCode(DEMO_PROJECT_CODE);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void removeCurrentUserScope_throwForbiddenException_whenUserNotFoundInSession() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var project = mock(Project.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeCurrentUserScope(projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void removeCurrentUserScope_throwForbiddenException_whenUserNotFoundInDatabase() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeCurrentUserScope(projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeCurrentUserScope_throwForbiddenException_whenProjectCodeIsBlank(String projectCode) throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeCurrentUserScope(projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void removeCurrentUserScope_throwForbiddenException_whenProjectIsNotFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeCurrentUserScope(projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void removeCurrentUserScope_deleteUserScopeAndRefreshAuthorities_whenUserAndProjectFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var userLogin = "user-login";
        var providerName = "provider-name";

        var project = mock(Project.class);
        var projectId = 1L;

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        when(user.getLogin()).thenReturn(userLogin);
        when(user.getProviderName()).thenReturn(providerName);
        when(project.getId()).thenReturn(projectId);

        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));

        // Then
        userAccountService.removeCurrentUserScope(projectCode);
        var roleToDeleteArgumentCaptor = ArgumentCaptor.forClass(UserProjectScope.UserProjectScopeId.class);
        verify(userProjectScopeRepository).deleteById(roleToDeleteArgumentCaptor.capture());
        assertThat(roleToDeleteArgumentCaptor.getValue())
                .extracting(
                        "projectId",
                        "userId.login",
                        "userId.providerName"
                )
                .contains(
                        projectId,
                        userLogin,
                        providerName
                );

        verify(userSessionService).refreshCurrentUserAuthorities();
    }

    @Test
    void updateCurrentUserProjectScope_throwForbiddenException_whenUserNotFoundInSession() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateCurrentUserProjectScope(projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void updateCurrentUserProjectScope_throwForbiddenException_whenUserNotFoundInDatabase() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        var userLogin = "user-login";
        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateCurrentUserProjectScope(projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateCurrentUserProjectScope_throwForbiddenException_whenProjectCodeIsBlank(String projectCode) throws ForbiddenException {
        // Given
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        var userLogin = "user-login";
        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateCurrentUserProjectScope(projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @Test
    void updateCurrentUserProjectScope_throwForbiddenException_whenProjectNotFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        var userLogin = "user-login";
        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateCurrentUserProjectScope(projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
        verify(userSessionService, never()).refreshCurrentUserAuthorities();
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void updateCurrentUserProjectScope_updateProjectScope_whenProjectFoundInUserScope(UserAccountScopeRole roleToUpdate) throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var userLogin = "user-login";
        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var role1 = mock(ProjectRole.class);
        var scope1 = new UserProjectScope(user, project1, role1);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var role2 = mock(ProjectRole.class);
        var scope2 = new UserProjectScope(user, project2, role2);
        var project3 = mock(Project.class);
        var role3 = mock(ProjectRole.class);
        var scope3 = new UserProjectScope(user, project3, role3);
        var scopes = Set.of(scope1, scope2, scope3);

        var project = mock(Project.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getScopes()).thenReturn(scopes);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode);

        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(project.getCode()).thenReturn(projectCode);

        // Then
        userAccountService.updateCurrentUserProjectScope(projectCode, roleToUpdate);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        var expectedRole = ProjectRole.valueOf(roleToUpdate.name());
        assertThat(capturedUserToSave.getScopes())
                .extracting("role", "project", "user")
                .containsExactlyInAnyOrder(
                        tuple(role1, project1, user),
                        tuple(role2, project2, user),
                        tuple(expectedRole, project3, user)
                )
                .hasSize(3);

        verify(userSessionService).refreshCurrentUserAuthorities();
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void updateCurrentUserProjectScope_addProjectScope_whenProjectNotFoundInUserScope(UserAccountScopeRole roleToUpdate) throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var userLogin = "user-login";
        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var role1 = mock(ProjectRole.class);
        var scope1 = new UserProjectScope(user, project1, role1);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var role2 = mock(ProjectRole.class);
        var scope2 = new UserProjectScope(user, project2, role2);
        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";
        var role3 = mock(ProjectRole.class);
        var scope3 = new UserProjectScope(user, project3, role3);
        var scopes = new HashSet<UserProjectScope>() {{add(scope1); add(scope2); add(scope3);}};

        var project = mock(Project.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getScopes()).thenReturn(scopes);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);

        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(project.getCode()).thenReturn(projectCode);

        // Then
        userAccountService.updateCurrentUserProjectScope(projectCode, roleToUpdate);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        var expectedRole = ProjectRole.valueOf(roleToUpdate.name());
        assertThat(capturedUserToSave.getScopes())
                .extracting("role", "project", "user")
                .containsExactlyInAnyOrder(
                        tuple(role1, project1, user),
                        tuple(role2, project2, user),
                        tuple(role3, project3, user),
                        tuple(expectedRole, project, user)
                )
                .hasSize(4);

        verify(userSessionService).refreshCurrentUserAuthorities();
    }

    @Test
    void clearDefaultProject_throwForbiddenException_whenUserNotFound() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.clearDefaultProject());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void clearDefaultProject_saveUserDefaultProjectAsNull_whenUserFound() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var defaultProjectToReplace = mock(Project.class);
        var existingUser = new User(providerName, userLogin);
        existingUser.setDefaultProject(defaultProjectToReplace);

        var updatedUser = mock(User.class);
        var updatedAccount = mock(UserAccount.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.getFullScopeAccessUserAccountFromUser(updatedUser)).thenReturn(updatedAccount);

        // Then
        var actualAccount = userAccountService.clearDefaultProject();
        assertThat(actualAccount).isSameAs(updatedAccount);

        var userToUpdateArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userToUpdateArgumentCaptor.capture());
        var capturedUser = userToUpdateArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(existingUser);
        assertThat(capturedUser.getDefaultProject()).isNotPresent();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateDefaultProject_throwForbiddenException_whenProjectCodeIsBlank(String projectCode) {
        // Given

        // When

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateDefaultProject(projectCode));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateDefaultProject_throwForbiddenException_whenProjectNotFound() {
        // Given
        var projectCode = "unknown-project-code";

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateDefaultProject(projectCode));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateDefaultProject_throwForbiddenException_whenUserNotFound() {
        // Given
        var projectCode = "unknown-project-code";
        var newDefaultProject = mock(Project.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(newDefaultProject));
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateDefaultProject(projectCode));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateDefaultProject_saveUserNewDefaultProject_whenProjectAndUserFound() throws ForbiddenException {
        // Given
        var projectCode = "unknown-project-code";
        var newDefaultProject = mock(Project.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var defaultProjectToReplace = mock(Project.class);
        var existingUser = new User(providerName, userLogin);
        existingUser.setDefaultProject(defaultProjectToReplace);

        var updatedUser = mock(User.class);
        var updatedAccount = mock(UserAccount.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(newDefaultProject));
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.getFullScopeAccessUserAccountFromUser(updatedUser)).thenReturn(updatedAccount);

        // Then
        var actualAccount = userAccountService.updateDefaultProject(projectCode);
        assertThat(actualAccount).isSameAs(updatedAccount);

        var userToUpdateArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userToUpdateArgumentCaptor.capture());
        var capturedUser = userToUpdateArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(existingUser);
        assertThat(capturedUser.getDefaultProject()).containsSame(newDefaultProject);
    }

    @Test
    void getAllUserAccounts_returnAllUserAccountsForTheCurrentProviderName() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var providerName = "provider-name";

        var user1 = mock(User.class);
        var user2 = mock(User.class);
        var user3 = mock(User.class);
        var users = List.of(user1, user2, user3);

        var matchingUserAccount1 = mock(UserAccount.class);
        var matchingUserAccount2 = mock(UserAccount.class);
        var matchingUserAccount3 = mock(UserAccount.class);

        // When
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userRepository.findAllByProviderName(providerName)).thenReturn(users);
        when(userMapper.getFullScopeAccessUserAccountFromUser(user1)).thenReturn(matchingUserAccount1);
        when(userMapper.getFullScopeAccessUserAccountFromUser(user2)).thenReturn(matchingUserAccount2);
        when(userMapper.getFullScopeAccessUserAccountFromUser(user3)).thenReturn(matchingUserAccount3);

        // Then
        var accounts = userAccountService.getAllUserAccounts(authentication);
        assertThat(accounts).containsExactlyInAnyOrder(matchingUserAccount1, matchingUserAccount2, matchingUserAccount3);
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void getAllScopedUserAccounts_returnAllScopedUserAccountsForTheCurrentProviderName_whenRoleIsPresent(UserAccountScopeRole role) throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var currentUser = mock(User.class);

        var matchingUserRole = ProjectRole.valueOf(role.name());

        var user1 = mock(User.class);
        var user2 = mock(User.class);
        var user3 = mock(User.class);
        var users = List.of(user1, user2, user3);

        var matchingUserAccount1 = mock(UserAccount.class);
        var matchingUserAccount2 = mock(UserAccount.class);
        var matchingUserAccount3 = mock(UserAccount.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findAllScopedUsersByProviderName(providerName, null, matchingUserRole)).thenReturn(users);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user1, currentUser)).thenReturn(matchingUserAccount1);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user2, currentUser)).thenReturn(matchingUserAccount2);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user3, currentUser)).thenReturn(matchingUserAccount3);

        // Then
        var accounts = userAccountService.getAllScopedUserAccounts(Optional.of(role));
        assertThat(accounts).containsExactlyInAnyOrder(matchingUserAccount1, matchingUserAccount2, matchingUserAccount3);
    }

    @Test
    void getAllScopedUserAccounts_returnAllScopedUserAccountsForTheCurrentProviderName_whenRoleIsNotPresent() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var currentUser = mock(User.class);

        var user1 = mock(User.class);
        var user2 = mock(User.class);
        var user3 = mock(User.class);
        var users = List.of(user1, user2, user3);

        var matchingUserAccount1 = mock(UserAccount.class);
        var matchingUserAccount2 = mock(UserAccount.class);
        var matchingUserAccount3 = mock(UserAccount.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findAllScopedUsersByProviderName(providerName, null, null)).thenReturn(users);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user1, currentUser)).thenReturn(matchingUserAccount1);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user2, currentUser)).thenReturn(matchingUserAccount2);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user3, currentUser)).thenReturn(matchingUserAccount3);

        // Then
        var accounts = userAccountService.getAllScopedUserAccounts(Optional.empty());
        assertThat(accounts).containsExactlyInAnyOrder(matchingUserAccount1, matchingUserAccount2, matchingUserAccount3);
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void getAllScopedUserAccountsOnProject_returnAllScopedUserAccountsForTheCurrentProviderName_whenRoleIsPresent(UserAccountScopeRole role) throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var currentUser = mock(User.class);

        var projectCode = "project-code";

        var matchingUserRole = ProjectRole.valueOf(role.name());

        var user1 = mock(User.class);
        var user2 = mock(User.class);
        var user3 = mock(User.class);
        var users = List.of(user1, user2, user3);

        var matchingUserAccount1 = mock(UserAccount.class);
        var matchingUserAccount2 = mock(UserAccount.class);
        var matchingUserAccount3 = mock(UserAccount.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findAllScopedUsersByProviderName(providerName, projectCode, matchingUserRole)).thenReturn(users);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user1, currentUser)).thenReturn(matchingUserAccount1);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user2, currentUser)).thenReturn(matchingUserAccount2);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user3, currentUser)).thenReturn(matchingUserAccount3);

        // Then
        var accounts = userAccountService.getAllScopedUserAccountsOnProject(projectCode, Optional.of(role));
        assertThat(accounts).containsExactlyInAnyOrder(matchingUserAccount1, matchingUserAccount2, matchingUserAccount3);
    }

    @Test
    void getAllScopedUserAccountsOnProject_returnAllScopedUserAccountsForTheCurrentProviderName_whenRoleIsNotPresent() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var currentUser = mock(User.class);

        var projectCode = "project-code";

        var user1 = mock(User.class);
        var user2 = mock(User.class);
        var user3 = mock(User.class);
        var users = List.of(user1, user2, user3);

        var matchingUserAccount1 = mock(UserAccount.class);
        var matchingUserAccount2 = mock(UserAccount.class);
        var matchingUserAccount3 = mock(UserAccount.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(authenticatedUser.getLogin()).thenReturn(userLogin);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(currentUser));
        when(currentUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findAllScopedUsersByProviderName(providerName, projectCode, null)).thenReturn(users);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user1, currentUser)).thenReturn(matchingUserAccount1);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user2, currentUser)).thenReturn(matchingUserAccount2);
        when(userMapper.getPartialScopeAccessUserAccountFromUser(user3, currentUser)).thenReturn(matchingUserAccount3);

        // Then
        var accounts = userAccountService.getAllScopedUserAccountsOnProject(projectCode, Optional.empty());
        assertThat(accounts).containsExactlyInAnyOrder(matchingUserAccount1, matchingUserAccount2, matchingUserAccount3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateUserProjectScope_throwForbiddenException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        // When

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProjectScope(userLogin, projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProjectScope_throwForbiddenException_whenUserNotFoundInSession() {
        // Given
        var userLogin = "user-login";
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProjectScope(userLogin, projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProjectScope_throwForbiddenException_whenUserNotFoundInDatabase() {
        // Given
        var userLogin = "user-login";
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProjectScope(userLogin, projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateUserProjectScope_throwForbiddenException_whenProjectCodeIsBlank(String projectCode) {
        // Given
        var userLogin = "user-login";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProjectScope(userLogin, projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProjectScope_throwForbiddenException_whenProjectNotFound() {
        // Given
        var userLogin = "user-login";
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProjectScope(userLogin, projectCode, roleToUpdate));
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void updateUserProjectScope_updateProjectScope_whenProjectFoundInUserScope(UserAccountScopeRole roleToUpdate) throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var projectCode = "project-code";

        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var role1 = mock(ProjectRole.class);
        var scope1 = new UserProjectScope(user, project1, role1);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var role2 = mock(ProjectRole.class);
        var scope2 = new UserProjectScope(user, project2, role2);
        var project3 = mock(Project.class);
        var role3 = mock(ProjectRole.class);
        var scope3 = new UserProjectScope(user, project3, role3);
        var scopes = Set.of(scope1, scope2, scope3);

        var project = mock(Project.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getScopes()).thenReturn(scopes);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode);

        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(project.getCode()).thenReturn(projectCode);

        // Then
        userAccountService.updateUserProjectScope(userLogin, projectCode, roleToUpdate);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        var expectedRole = ProjectRole.valueOf(roleToUpdate.name());
        assertThat(capturedUserToSave.getScopes())
                .extracting("role", "project", "user")
                .containsExactlyInAnyOrder(
                        tuple(role1, project1, user),
                        tuple(role2, project2, user),
                        tuple(expectedRole, project3, user)
                )
                .hasSize(3);
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void updateUserProjectScope_addProjectScope_whenProjectNotFoundInUserScope(UserAccountScopeRole roleToUpdate) throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var projectCode = "project-code";

        var providerName = "provider-name";
        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var role1 = mock(ProjectRole.class);
        var scope1 = new UserProjectScope(user, project1, role1);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var role2 = mock(ProjectRole.class);
        var scope2 = new UserProjectScope(user, project2, role2);
        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";
        var role3 = mock(ProjectRole.class);
        var scope3 = new UserProjectScope(user, project3, role3);
        var scopes = new HashSet<UserProjectScope>() {{add(scope1); add(scope2); add(scope3);}};

        var project = mock(Project.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(user.getScopes()).thenReturn(scopes);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);

        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(project.getCode()).thenReturn(projectCode);

        // Then
        userAccountService.updateUserProjectScope(userLogin, projectCode, roleToUpdate);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        var expectedRole = ProjectRole.valueOf(roleToUpdate.name());
        assertThat(capturedUserToSave.getScopes())
                .extracting("role", "project", "user")
                .containsExactlyInAnyOrder(
                        tuple(role1, project1, user),
                        tuple(role2, project2, user),
                        tuple(role3, project3, user),
                        tuple(expectedRole, project, user)
                )
                .hasSize(4);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeUserScope_throwForbiddenException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeUserScope(userLogin, projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
    }

    @Test
    void removeUserScope_throwForbiddenException_whenUserNotFoundInSession() {
        // Given
        var userLogin = "user-login";

        var projectCode = "project-code";

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeUserScope(userLogin, projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
    }

    @Test
    void removeUserScope_throwForbiddenException_whenUserNotFoundInDatabase() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        var projectCode = "project-code";

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeUserScope(userLogin, projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeUserScope_throwForbiddenException_whenProjectCodeIsBlank(String projectCode) {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeUserScope(userLogin, projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
    }

    @Test
    void removeUserScope_throwForbiddenException_whenProjectIsNotFound() {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        var projectCode = "project-code";

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeUserScope(userLogin, projectCode));
        verify(userProjectScopeRepository, never()).deleteById(any(UserProjectScope.UserProjectScopeId.class));
    }

    @Test
    void removeUserScope_deleteUserScopeAndRefreshAuthorities_whenUserAndProjectFound() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = mock(User.class);

        var project = mock(Project.class);
        var projectCode = "project-code";
        var projectId = 1L;

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        when(user.getLogin()).thenReturn(userLogin);
        when(user.getProviderName()).thenReturn(providerName);
        when(project.getId()).thenReturn(projectId);

        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));

        // Then
        userAccountService.removeUserScope(userLogin, projectCode);
        var scopeToDeleteArgumentCaptor = ArgumentCaptor.forClass(UserProjectScope.UserProjectScopeId.class);
        verify(userProjectScopeRepository).deleteById(scopeToDeleteArgumentCaptor.capture());
        assertThat(scopeToDeleteArgumentCaptor.getValue())
                .extracting(
                        "projectId",
                        "userId.login",
                        "userId.providerName"
                )
                .contains(
                        projectId,
                        userLogin,
                        providerName
                );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateUserProfile_throwForbiddenException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var profile = UserAccountProfile.SUPER_ADMIN;

        // When

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProfile(userLogin, profile));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserProfile_throwForbiddenException_whenUserNotFoundInSession() {
        // Given
        var userLogin = "user-login";
        var profile = UserAccountProfile.SUPER_ADMIN;

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProfile(userLogin, profile));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserProfile_throwForbiddenException_whenUserNotFoundInDatabase() {
        // Given
        var profile = UserAccountProfile.SUPER_ADMIN;
        var userLogin = "user-login";
        var providerName = "provider-name";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateUserProfile(userLogin, profile));
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountProfile.class)
    void updateUserProfile_updateUserProfile_whenUserFound(UserAccountProfile profile) throws ForbiddenException {
        // Given
        var providerName = "provider-name";
        var userLogin = "user-login";

        var authenticatedUser = mock(AuthenticatedOAuth2User.class);
        var user = new User(providerName, userLogin);

        // When
        when(userSessionService.getCurrentAuthenticatedOAuth2User()).thenReturn(Optional.of(authenticatedUser));
        when(authenticatedUser.getProviderName()).thenReturn(providerName);
        when(userRepository.findById(new User.UserId(providerName, userLogin))).thenReturn(Optional.of(user));

        // Then
        userAccountService.updateUserProfile(userLogin, profile);
        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userToSaveArgumentCaptor.capture());
        assertThat(userToSaveArgumentCaptor.getValue().getProfile()).isEqualTo(UserProfile.valueOf(profile.name()));
    }
}
