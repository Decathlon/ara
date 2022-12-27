package com.decathlon.ara.security.service.user;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRoleOnProjectRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserScopeConfiguration;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.security.service.user.strategy.UserAccountStrategy;
import com.decathlon.ara.security.service.user.strategy.select.UserStrategySelector;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotUniqueException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserStrategySelector userStrategySelector;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private UserEntityRoleOnProjectRepository userEntityRoleOnProjectRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OAuth2ProvidersConfiguration providersConfiguration;

    @Mock
    private ProjectService projectService;

    @Mock
    private AuthorityService authorityService;

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
    void getCurrentUserAccount_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount();
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccount_returnEmptyOptional_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount();
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccount_returnEmptyOptional_whenUserIsNull() {
        // Given
        String providerName = "provider-name";

        // When

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount(null, providerName);
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getCurrentUserAccount_returnEmptyOptional_whenProviderNameIsBlank(String providerName) {
        // Given
        var oauth2User = mock(OAuth2User.class);

        // When

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount(oauth2User, providerName);
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccount_returnEmptyOptional_whenUserNotFound() {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var accountStrategy = mock(UserAccountStrategy.class);

        // When
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(accountStrategy);
        when(accountStrategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount(oauth2User, providerName);
        assertThat(fetchedUserAccount).isNotPresent();
    }

    @Test
    void getCurrentUserAccount1_returnUserAccount_whenUserFound() {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var accountStrategy = mock(UserAccountStrategy.class);

        var userEntity = mock(UserEntity.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(accountStrategy);
        when(accountStrategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(userEntity));
        when(accountStrategy.getUserAccount(oauth2User, userEntity)).thenReturn(matchingUserAccount);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount(oauth2User, providerName);
        assertThat(fetchedUserAccount).containsSame(matchingUserAccount);
    }

    @Test
    void getCurrentUserAccount2_returnUserAccount_whenUserFound() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var accountStrategy = mock(UserAccountStrategy.class);

        var userEntity = mock(UserEntity.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(accountStrategy);
        when(accountStrategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(userEntity));
        when(accountStrategy.getUserAccount(oauth2User, userEntity)).thenReturn(matchingUserAccount);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount(authentication);
        assertThat(fetchedUserAccount).containsSame(matchingUserAccount);
    }

    @Test
    void getCurrentUserAccount3_returnUserAccount_whenUserFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var accountStrategy = mock(UserAccountStrategy.class);

        var userEntity = mock(UserEntity.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(accountStrategy);
        when(accountStrategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(userEntity));
        when(accountStrategy.getUserAccount(oauth2User, userEntity)).thenReturn(matchingUserAccount);

        // Then
        var fetchedUserAccount = userAccountService.getCurrentUserAccount();
        assertThat(fetchedUserAccount).containsSame(matchingUserAccount);
    }

    @Test
    void createUserAccount_saveUserAsScopedUser_whenProfileConfigurationNotFound() throws NotUniqueException {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var savedUser = mock(UserEntity.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(strategy.getFirstName(oauth2User)).thenReturn(Optional.of(userFirstName));
        when(strategy.getLastName(oauth2User)).thenReturn(Optional.of(userLastName));
        when(strategy.getEmail(oauth2User)).thenReturn(Optional.of(userEmail));
        when(providersConfiguration.getUserProfileConfiguration(providerName, userLogin)).thenReturn(Optional.empty());
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(strategy.getUserAccount(oauth2User, savedUser)).thenReturn(matchingUserAccount);

        // Then
        var savedUserAccount = userAccountService.createUserAccount(oauth2User, providerName);
        assertThat(savedUserAccount).isSameAs(matchingUserAccount);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName",
                        "profile"
                )
                .contains(
                        userLogin,
                        providerName,
                        Optional.of(userEmail),
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        UserEntity.UserEntityProfile.SCOPED_USER
                );
        assertThat(userToSave.getRolesOnProjectWhenScopedUser()).isEmpty();

        verify(projectService, never()).createFromCode(anyString(), any());
        verify(userEntityRoleOnProjectRepository, never()).saveAll(any());
        verify(userEntityRepository, never()).findById(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN", "super_admin", "SuPeR_AdMiN",
            "AUDITOR", "auditor", "AuDiToR",
            "SCOPED_USER", "scoped_user", "ScOpEd_uSeR"
    })
    void createUserAccount_getProfileAndRolesFromConfigurationAndSaveUserWithoutCreatingUnknownProjects_whenProfileConfigurationIsFoundAndProjectCreationOptionIsFalse(String profileAsString) throws NotUniqueException {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

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

        var savedUser = mock(UserEntity.class);
        var finalSavedUser = mock(UserEntity.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(strategy.getFirstName(oauth2User)).thenReturn(Optional.of(userFirstName));
        when(strategy.getLastName(oauth2User)).thenReturn(Optional.of(userLastName));
        when(strategy.getEmail(oauth2User)).thenReturn(Optional.of(userEmail));

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

        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(finalSavedUser));
        when(strategy.getUserAccount(oauth2User, finalSavedUser)).thenReturn(matchingUserAccount);

        // Then
        var savedUserAccount = userAccountService.createUserAccount(oauth2User, providerName);
        assertThat(savedUserAccount).isSameAs(matchingUserAccount);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName",
                        "profile"
                )
                .contains(
                        userLogin,
                        providerName,
                        Optional.of(userEmail),
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        UserEntity.UserEntityProfile.valueOf(profileAsString.toUpperCase())
                );
        ArgumentCaptor<List<UserEntityRoleOnProject>> userRolesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(userEntityRoleOnProjectRepository).saveAll(userRolesArgumentCaptor.capture());
        assertThat(userRolesArgumentCaptor.getValue())
                .extracting("userEntity", "project", "role")
                .containsExactlyInAnyOrder(
                        tuple(savedUser, existingProject1, UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN),
                        tuple(savedUser, existingProject2, UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN),
                        tuple(savedUser, existingProject3, UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER)
                );
        verify(projectService, never()).createFromCode(anyString(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN", "super_admin", "SuPeR_AdMiN",
            "AUDITOR", "auditor", "AuDiToR",
            "SCOPED_USER", "scoped_user", "ScOpEd_uSeR"
    })
    void createUserAccount_getProfileAndRolesFromConfigurationAndSaveUserWhileCreatingUnknownProjects_whenProfileConfigurationIsFoundAndProjectCreationOptionIsTrue(String profileAsString) throws NotUniqueException {
        // Given
        var oauth2User = mock(OAuth2User.class);
        var userLogin = "user-login";
        var userFirstName = "user-firstname";
        var userLastName = "user-lastname";
        var userEmail = "user-email";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

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

        var savedUser = mock(UserEntity.class);
        var finalSavedUser = mock(UserEntity.class);
        var matchingUserAccount = mock(UserAccount.class);

        // When
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(oauth2User)).thenReturn(userLogin);
        when(strategy.getFirstName(oauth2User)).thenReturn(Optional.of(userFirstName));
        when(strategy.getLastName(oauth2User)).thenReturn(Optional.of(userLastName));
        when(strategy.getEmail(oauth2User)).thenReturn(Optional.of(userEmail));

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

        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(finalSavedUser));
        when(strategy.getUserAccount(oauth2User, finalSavedUser)).thenReturn(matchingUserAccount);

        // Then
        var savedUserAccount = userAccountService.createUserAccount(oauth2User, providerName);
        assertThat(savedUserAccount).isSameAs(matchingUserAccount);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave)
                .extracting(
                        "login",
                        "providerName",
                        "email",
                        "firstName",
                        "lastName",
                        "profile"
                )
                .contains(
                        userLogin,
                        providerName,
                        Optional.of(userEmail),
                        Optional.of(userFirstName),
                        Optional.of(userLastName),
                        UserEntity.UserEntityProfile.valueOf(profileAsString.toUpperCase())
                );
        ArgumentCaptor<List<UserEntityRoleOnProject>> userRolesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(userEntityRoleOnProjectRepository).saveAll(userRolesArgumentCaptor.capture());
        assertThat(userRolesArgumentCaptor.getValue())
                .extracting("userEntity", "project", "role")
                .containsExactlyInAnyOrder(
                        tuple(savedUser, existingProject1, UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN),
                        tuple(savedUser, existingProject2, UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN),
                        tuple(savedUser, unknownProject1, UserEntityRoleOnProject.ScopedUserRoleOnProject.ADMIN),
                        tuple(savedUser, existingProject3, UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER),
                        tuple(savedUser, unknownProject2, UserEntityRoleOnProject.ScopedUserRoleOnProject.MAINTAINER),
                        tuple(savedUser, unknownProject3, UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER)
                );
        var unknownProjectCodeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(projectService, times(3)).createFromCode(unknownProjectCodeArgumentCaptor.capture(), eq(savedUser));
        assertThat(unknownProjectCodeArgumentCaptor.getAllValues()).containsExactlyInAnyOrder(unknownProjectCode1, unknownProjectCode2, unknownProjectCode3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeProjectFromCurrentUserAccountScope_throwForbiddenException_whenProjectCodeIsBlank(String projectCode) throws ForbiddenException {
        // Given

        // When

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeProjectFromCurrentUserAccountScope(projectCode));
        verify(userEntityRoleOnProjectRepository, never()).deleteById(any(UserEntityRoleOnProject.UserEntityRoleOnProjectId.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void removeProjectFromCurrentUserAccountScope_throwForbiddenException_whenProjectIsNotFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeProjectFromCurrentUserAccountScope(projectCode));
        verify(userEntityRoleOnProjectRepository, never()).deleteById(any(UserEntityRoleOnProject.UserEntityRoleOnProjectId.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void removeProjectFromCurrentUserAccountScope_throwForbiddenException_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        var project = mock(Project.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeProjectFromCurrentUserAccountScope(projectCode));
        verify(userEntityRoleOnProjectRepository, never()).deleteById(any(UserEntityRoleOnProject.UserEntityRoleOnProjectId.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void removeProjectFromCurrentUserAccountScope_throwForbiddenException_whenAuthenticationPrincipalIsNull() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var project = mock(Project.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeProjectFromCurrentUserAccountScope(projectCode));
        verify(userEntityRoleOnProjectRepository, never()).deleteById(any(UserEntityRoleOnProject.UserEntityRoleOnProjectId.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeProjectFromCurrentUserAccountScope_throwForbiddenException_whenAuthenticationAuthorizedClientRegistrationIdIsBlank(String providerName) throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var project = mock(Project.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeProjectFromCurrentUserAccountScope(projectCode));
        verify(userEntityRoleOnProjectRepository, never()).deleteById(any(UserEntityRoleOnProject.UserEntityRoleOnProjectId.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void removeProjectFromCurrentUserAccountScope_throwForbiddenException_whenUserNotFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var project = mock(Project.class);

        var strategy = mock(UserAccountStrategy.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.removeProjectFromCurrentUserAccountScope(projectCode));
        verify(userEntityRoleOnProjectRepository, never()).deleteById(any(UserEntityRoleOnProject.UserEntityRoleOnProjectId.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void removeProjectFromCurrentUserAccountScope_deleteUserRoleAndRefreshAuthorities_whenUserAndProjectFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var project = mock(Project.class);
        var projectId = 1L;

        var user = mock(UserEntity.class);

        var strategy = mock(UserAccountStrategy.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));

        when(user.getLogin()).thenReturn(userLogin);
        when(user.getProviderName()).thenReturn(providerName);
        when(project.getId()).thenReturn(projectId);

        // Then
        userAccountService.removeProjectFromCurrentUserAccountScope(projectCode);
        var roleToDeleteArgumentCaptor = ArgumentCaptor.forClass(UserEntityRoleOnProject.UserEntityRoleOnProjectId.class);
        verify(userEntityRoleOnProjectRepository).deleteById(roleToDeleteArgumentCaptor.capture());
        assertThat(roleToDeleteArgumentCaptor.getValue())
                .extracting(
                        "projectId",
                        "userEntityId.login",
                        "userEntityId.providerName"
                )
                .contains(
                        projectId,
                        userLogin,
                        providerName
                );

        verify(authorityService).refreshCurrentUserAccountAuthorities();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateCurrentUserAccountProjectScope_throwForbiddenException_whenProjectCodeIsBlank(String projectCode) throws ForbiddenException {
        // Given
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        // When

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateCurrentUserAccountProjectScope(projectCode, roleToUpdate));
        verify(userEntityRepository, never()).save(any(UserEntity.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void updateCurrentUserAccountProjectScope_throwForbiddenException_whenProjectNotFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateCurrentUserAccountProjectScope(projectCode, roleToUpdate));
        verify(userEntityRepository, never()).save(any(UserEntity.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void updateCurrentUserAccountProjectScope_throwForbiddenException_whenUserNotFound() throws ForbiddenException {
        // Given
        var projectCode = "project-code";
        var roleToUpdate = UserAccountScopeRole.ADMIN;

        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateCurrentUserAccountProjectScope(projectCode, roleToUpdate));
        verify(userEntityRepository, never()).save(any(UserEntity.class));
        verify(authorityService, never()).refreshCurrentUserAccountAuthorities();
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void updateCurrentUserAccountProjectScope_updateProjectRole_whenProjectFoundInUserScope(UserAccountScopeRole roleToUpdate) throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var user = mock(UserEntity.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var scope1 = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var role1 = new UserEntityRoleOnProject(user, project1, scope1);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var scope2 = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var role2 = new UserEntityRoleOnProject(user, project2, scope2);
        var project3 = mock(Project.class);
        var scope3 = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var role3 = new UserEntityRoleOnProject(user, project3, scope3);
        var roles = List.of(role1, role2, role3);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));
        when(user.getRolesOnProjectWhenScopedUser()).thenReturn(roles);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode);

        // Then
        userAccountService.updateCurrentUserAccountProjectScope(projectCode, roleToUpdate);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        var expectedScope = UserEntityRoleOnProject.ScopedUserRoleOnProject.valueOf(roleToUpdate.name());
        assertThat(capturedUserToSave.getRolesOnProjectWhenScopedUser())
                .extracting("role", "project", "userEntity")
                .containsExactlyInAnyOrder(
                        tuple(scope1, project1, user),
                        tuple(scope2, project2, user),
                        tuple(expectedScope, project3, user)
                )
                .hasSize(3);

        verify(authorityService).refreshCurrentUserAccountAuthorities();
    }

    @ParameterizedTest
    @EnumSource(value = UserAccountScopeRole.class)
    void updateCurrentUserAccountProjectScope_addProjectRole_whenProjectNotFoundInUserScope(UserAccountScopeRole roleToUpdate) throws ForbiddenException {
        // Given
        var projectCode = "project-code";

        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var user = mock(UserEntity.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var scope1 = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var role1 = new UserEntityRoleOnProject(user, project1, scope1);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var scope2 = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var role2 = new UserEntityRoleOnProject(user, project2, scope2);
        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";
        var scope3 = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var role3 = new UserEntityRoleOnProject(user, project3, scope3);
        var roles = new ArrayList<UserEntityRoleOnProject>() {{add(role1); add(role2); add(role3);}};

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));
        when(user.getRolesOnProjectWhenScopedUser()).thenReturn(roles);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);

        // Then
        userAccountService.updateCurrentUserAccountProjectScope(projectCode, roleToUpdate);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        var expectedScope = UserEntityRoleOnProject.ScopedUserRoleOnProject.valueOf(roleToUpdate.name());
        assertThat(capturedUserToSave.getRolesOnProjectWhenScopedUser())
                .extracting("role", "project", "userEntity")
                .containsExactlyInAnyOrder(
                        tuple(scope1, project1, user),
                        tuple(scope2, project2, user),
                        tuple(scope3, project3, user),
                        tuple(expectedScope, project, user)
                )
                .hasSize(4);

        verify(authorityService).refreshCurrentUserAccountAuthorities();
    }

    @Test
    void clearDefaultProject_throwForbiddenException_whenUserNotFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.clearDefaultProject());
        verify(userEntityRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void clearDefaultProject_saveUserDefaultProjectAsNull_whenUserFound() throws ForbiddenException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var defaultProjectToReplace = mock(Project.class);
        var persistedUser = new UserEntity(userLogin, providerName);
        persistedUser.setDefaultProject(defaultProjectToReplace);

        var updatedUser = mock(UserEntity.class);
        var updatedAccount = mock(UserAccount.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(persistedUser));
        when(userEntityRepository.save(persistedUser)).thenReturn(updatedUser);
        when(updatedUser.getProviderName()).thenReturn(providerName);
        when(strategy.getUserAccount(principal, updatedUser)).thenReturn(updatedAccount);

        // Then
        var actualAccount = userAccountService.clearDefaultProject();
        assertThat(actualAccount).isSameAs(updatedAccount);

        var userToUpdateArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToUpdateArgumentCaptor.capture());
        var capturedUser = userToUpdateArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(persistedUser);
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
        verify(userEntityRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateDefaultProject_throwForbiddenException_whenProjectNotFound() {
        // Given
        var projectCode = "unknown-project-code";

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateDefaultProject(projectCode));
        verify(userEntityRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateDefaultProject_throwForbiddenException_whenUserNotFound() {
        // Given
        var projectCode = "unknown-project-code";
        var newDefaultProject = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(newDefaultProject));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(ForbiddenException.class, () -> userAccountService.updateDefaultProject(projectCode));
        verify(userEntityRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateDefaultProject_saveUserNewDefaultProject_whenProjectAndUserFound() throws ForbiddenException {
        // Given
        var projectCode = "unknown-project-code";
        var newDefaultProject = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);
        var userLogin = "user-login";
        var providerName = "provider-name";

        var strategy = mock(UserAccountStrategy.class);

        var defaultProjectToReplace = mock(Project.class);
        var persistedUser = new UserEntity(userLogin, providerName);
        persistedUser.setDefaultProject(defaultProjectToReplace);

        var updatedUser = mock(UserEntity.class);
        var updatedAccount = mock(UserAccount.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(newDefaultProject));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userStrategySelector.selectUserStrategyFromProviderName(providerName)).thenReturn(strategy);
        when(strategy.getLogin(principal)).thenReturn(userLogin);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(persistedUser));
        when(userEntityRepository.save(persistedUser)).thenReturn(updatedUser);
        when(updatedUser.getProviderName()).thenReturn(providerName);
        when(strategy.getUserAccount(principal, updatedUser)).thenReturn(updatedAccount);

        // Then
        var actualAccount = userAccountService.updateDefaultProject(projectCode);
        assertThat(actualAccount).isSameAs(updatedAccount);

        var userToUpdateArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToUpdateArgumentCaptor.capture());
        var capturedUser = userToUpdateArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(persistedUser);
        assertThat(capturedUser.getDefaultProject()).containsSame(newDefaultProject);
    }
}
