package com.decathlon.ara.security.service.user;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.dto.user.LoggedInUserScopeDTO;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private OAuth2ProvidersConfiguration providersConfiguration;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUserEntity_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        var user = userService.getCurrentUserEntity();
        assertThat(user).isEmpty();
    }

    @Test
    void getCurrentUserEntity_returnEmptyOptional_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var user = userService.getCurrentUserEntity();
        assertThat(user).isEmpty();
    }

    @Test
    void getCurrentUserEntity_returnEmptyOptional_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        var user = userService.getCurrentUserEntity();
        assertThat(user).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getCurrentUserEntity_returnEmptyOptional_whenUserLoginIsBlank(String userLogin) {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);

        // Then
        var user = userService.getCurrentUserEntity();
        assertThat(user).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void getCurrentUserEntity_returnEmptyOptional_whenProviderNameIsBlank(String providerName) {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);

        // Then
        var user = userService.getCurrentUserEntity();
        assertThat(user).isEmpty();
    }

    @Test
    void getCurrentUserEntity_returnEmptyOptional_whenUserNotFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        var user = userService.getCurrentUserEntity();
        assertThat(user).isEmpty();
    }

    @Test
    void getCurrentUserEntity_returnUser_whenUserFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";
        var user = mock(UserEntity.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));

        // Then
        var actualUser = userService.getCurrentUserEntity();
        assertThat(actualUser).contains(user);
    }

    @Test
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenScopeIsNull() {
        // Given
        var projectCode = "project-code";

        // When

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenProjectCodeIsBlank(String projectCode) {
        // Given
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);

        // When

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @Test
    void updateLoggedInUserProjectScopes_throwNotFoundException_whenProjectNotFound() {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @Test
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenAuthenticationIsNull() {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @Test
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenUserIsNotAuthenticated() {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @Test
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenProviderNameIsBlank(String providerName) {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @Test
    void updateLoggedInUserProjectScopes_throwBadRequestException_whenUserNotFound() {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateLoggedInUserProjectScopes(projectCode, scope));
    }

    @Test
    void updateLoggedInUserProjectScopes_updateProjectRole_whenProjectFoundInUserScope() throws BadRequestException {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";
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

        var updatedUser = mock(UserEntity.class);
        var updatedAuthority1 = mock(GrantedAuthority.class);
        var updatedAuthority2 = mock(GrantedAuthority.class);
        var updatedAuthority3 = mock(GrantedAuthority.class);
        var updatedAuthorities = Set.of(
                updatedAuthority1,
                updatedAuthority2,
                updatedAuthority3
        );

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));
        when(user.getRolesOnProjectWhenScopedUser()).thenReturn(roles);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode);
        when(userEntityRepository.save(user)).thenReturn(updatedUser);
        when(updatedUser.getMatchingAuthorities()).thenReturn(updatedAuthorities);

        // Then
        userService.updateLoggedInUserProjectScopes(projectCode, scope);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        assertThat(capturedUserToSave.getRolesOnProjectWhenScopedUser())
                .hasSize(3)
                .extracting("role", "project", "userEntity")
                .containsExactlyInAnyOrder(
                        tuple(scope1, project1, user),
                        tuple(scope2, project2, user),
                        tuple(scope, project3, user)
                );

        var authenticationToUpdateArgumentCaptor = ArgumentCaptor.forClass(OAuth2AuthenticationToken.class);
        verify(securityContext, times(1)).setAuthentication(authenticationToUpdateArgumentCaptor.capture());
        var authenticationToUpdate = authenticationToUpdateArgumentCaptor.getValue();
        assertThat(authenticationToUpdate.getPrincipal()).isSameAs(principal);
        assertThat(authenticationToUpdate.getAuthorities())
                .containsExactlyInAnyOrder(
                        updatedAuthority1,
                        updatedAuthority2,
                        updatedAuthority3
                );
        assertThat(authenticationToUpdate.getAuthorizedClientRegistrationId()).isEqualTo(providerName);
    }

    @Test
    void updateLoggedInUserProjectScopes_addProjectRole_whenProjectNotFoundInUserScope() throws BadRequestException {
        // Given
        var projectCode = "project-code";
        var scope = mock(UserEntityRoleOnProject.ScopedUserRoleOnProject.class);
        var project = mock(Project.class);

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";
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

        var updatedUser = mock(UserEntity.class);
        var updatedAuthority1 = mock(GrantedAuthority.class);
        var updatedAuthority2 = mock(GrantedAuthority.class);
        var updatedAuthority3 = mock(GrantedAuthority.class);
        var updatedAuthority4 = mock(GrantedAuthority.class);
        var updatedAuthorities = Set.of(
                updatedAuthority1,
                updatedAuthority2,
                updatedAuthority3,
                updatedAuthority4
        );

        // When
        when(projectRepository.findByCode(projectCode)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));
        when(user.getRolesOnProjectWhenScopedUser()).thenReturn(roles);
        when(project1.getCode()).thenReturn(projectCode1);
        when(project2.getCode()).thenReturn(projectCode2);
        when(project3.getCode()).thenReturn(projectCode3);
        when(userEntityRepository.save(user)).thenReturn(updatedUser);
        when(updatedUser.getMatchingAuthorities()).thenReturn(updatedAuthorities);

        // Then
        userService.updateLoggedInUserProjectScopes(projectCode, scope);

        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository, times(1)).save(userToSaveArgumentCaptor.capture());
        var capturedUserToSave = userToSaveArgumentCaptor.getValue();
        assertThat(capturedUserToSave.getRolesOnProjectWhenScopedUser())
                .hasSize(4)
                .extracting("role", "project", "userEntity")
                .containsExactlyInAnyOrder(
                        tuple(scope1, project1, user),
                        tuple(scope2, project2, user),
                        tuple(scope3, project3, user),
                        tuple(scope, project, user)
                );

        var authenticationToUpdateArgumentCaptor = ArgumentCaptor.forClass(OAuth2AuthenticationToken.class);
        verify(securityContext, times(1)).setAuthentication(authenticationToUpdateArgumentCaptor.capture());
        var authenticationToUpdate = authenticationToUpdateArgumentCaptor.getValue();
        assertThat(authenticationToUpdate.getPrincipal()).isSameAs(principal);
        assertThat(authenticationToUpdate.getAuthorities())
                .containsExactlyInAnyOrder(
                        updatedAuthority1,
                        updatedAuthority2,
                        updatedAuthority3,
                        updatedAuthority4
                );
        assertThat(authenticationToUpdate.getAuthorizedClientRegistrationId()).isEqualTo(providerName);
    }

    @Test
    void getLoggedInUserDTO_returnEmptyOptional_whenAuthenticationIsNull() {
        // Given

        // When

        // Then
        var user = userService.getLoggedInUserDTO(null);
        assertThat(user).isNotPresent();
    }

    @Test
    void getLoggedInUserDTO_returnEmptyOptional_whenUserIsNotAuthenticated() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isNotPresent();
    }

    @Test
    void getLoggedInUserDTO_returnEmptyOptional_whenPrincipalIsNull() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(null);

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isNotPresent();
    }

    @Test
    void getLoggedInUserDTO_returnUserWithoutProfile_whenNoProfileFound() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        // When
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authorityService.getProfile()).thenReturn(Optional.empty());

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isPresent();
        assertThat(user.get().getProfile()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void getLoggedInUserDTO_returnUser_whenPrincipalIsNotInstanceOfOidcUser(UserEntity.UserEntityProfile userProfile) {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var userLogin = "user-login";
        var userFirstName = "user-first-name";
        var userLastName = "user-last-name";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        // When
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(userLogin);
        when(principal.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(userFirstName);
        when(principal.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(userLastName);
        when(principal.getAttribute(StandardClaimNames.EMAIL)).thenReturn(userEmail);
        when(principal.getAttribute(StandardClaimNames.PICTURE)).thenReturn(userPictureUrl);

        when(authorityService.getProfile()).thenReturn(Optional.of(userProfile));

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl",
                        "profile"
                )
                .contains(
                        userLogin,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl,
                        userProfile.name()
                );
    }

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class)
    void getLoggedInUserDTO_returnUser_whenPrincipalIsInstanceOfOidcUser(UserEntity.UserEntityProfile userProfile) {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OidcUser.class);

        var userLogin = "user-login";
        var userFirstName = "user-first-name";
        var userLastName = "user-last-name";
        var userEmail = "user-email";
        var userPictureUrl = "user-picture-url";

        // When
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(userLogin);
        when(principal.getGivenName()).thenReturn(userFirstName);
        when(principal.getFamilyName()).thenReturn(userLastName);
        when(principal.getEmail()).thenReturn(userEmail);
        when(principal.getPicture()).thenReturn(userPictureUrl);

        when(authorityService.getProfile()).thenReturn(Optional.of(userProfile));

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isPresent();
        assertThat(user.get())
                .extracting(
                        "login",
                        "firstName",
                        "lastName",
                        "email",
                        "pictureUrl",
                        "profile"
                )
                .contains(
                        userLogin,
                        userFirstName,
                        userLastName,
                        userEmail,
                        userPictureUrl,
                        userProfile.name()
                );
    }

    @ParameterizedTest
    @EnumSource(value = UserEntity.UserEntityProfile.class, names = {"SUPER_ADMIN", "AUDITOR"})
    void getLoggedInUserDTO_returnUserWithoutScopes_whenUserIsSuperAdminOrAuditor(UserEntity.UserEntityProfile userProfile) {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        // When
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authorityService.getProfile()).thenReturn(Optional.of(userProfile));

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isPresent();
        assertThat(user.get().getScopes()).isEmpty();
        verify(authorityService, never()).getLoggedInUserScopes();
    }

    @Test
    void getLoggedInUserDTO_returnUserWithoutScopes_whenUserIsScopedUserAndHasNoScopes() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var userProfile = UserEntity.UserEntityProfile.SCOPED_USER;

        // When
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authorityService.getProfile()).thenReturn(Optional.of(userProfile));
        when(authorityService.getLoggedInUserScopes()).thenReturn(null);

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isPresent();
        assertThat(user.get().getScopes()).isEmpty();
    }

    @Test
    void getLoggedInUserDTO_returnUserWithScopes_whenUserIsScopedUserAndHasScopes() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var userProfile = UserEntity.UserEntityProfile.SCOPED_USER;
        var projectCode1 = "project-code1";
        var role1 = "role1";
        var scope1 = new LoggedInUserScopeDTO(projectCode1, role1);
        var projectCode2 = "project-code2";
        var role2 = "role2";
        var scope2 = new LoggedInUserScopeDTO(projectCode2, role2);
        var projectCode3 = "project-code3";
        var role3 = "role3";
        var scope3 = new LoggedInUserScopeDTO(projectCode3, role3);
        var scopes = List.of(scope1, scope2, scope3);

        // When
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authorityService.getProfile()).thenReturn(Optional.of(userProfile));
        when(authorityService.getLoggedInUserScopes()).thenReturn(scopes);

        // Then
        var user = userService.getLoggedInUserDTO(authentication);
        assertThat(user).isPresent();
        assertThat(user.get().getScopes()).containsExactlyInAnyOrder(scope1, scope2, scope3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenProjectCodeIsBlank(String projectCode) {
        // Given

        // When

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @Test
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenAuthenticationIsNull() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @Test
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenUserIsNotAuthenticated() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @Test
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenProviderNameIsBlank(String providerName) {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @Test
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenUserNotFound() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @Test
    void removeLoggedInUserProjectFromScope_throwBadRequestException_whenProjectNotFoundInUserScope() {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";
        var user = mock(UserEntity.class);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var role1 = mock(UserEntityRoleOnProject.class);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var role2 = mock(UserEntityRoleOnProject.class);
        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";
        var role3 = mock(UserEntityRoleOnProject.class);
        var roles = List.of(role1, role2, role3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));
        when(user.getRolesOnProjectWhenScopedUser()).thenReturn(roles);
        when(role1.getProject()).thenReturn(project1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(role2.getProject()).thenReturn(project2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(role3.getProject()).thenReturn(project3);
        when(project3.getCode()).thenReturn(projectCode3);

        // Then
        assertThrows(BadRequestException.class, () -> userService.removeLoggedInUserProjectFromScope(projectCode));
    }

    @Test
    void removeLoggedInUserProjectFromScope_removeProjectScopeAndUpdateAuthorities_whenProjectFoundInUserScope() throws BadRequestException {
        // Given
        var projectCode = "project-code";

        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";
        var user = new UserEntity(userLogin, providerName);

        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";
        var role1 = mock(UserEntityRoleOnProject.class);
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";
        var role2 = mock(UserEntityRoleOnProject.class);
        var project3 = mock(Project.class);
        var role3 = mock(UserEntityRoleOnProject.class);
        var roles = List.of(role1, role2, role3);
        user.setRolesOnProjectWhenScopedUser(roles);

        var updatedUser = mock(UserEntity.class);
        var updatedAuthority1 = mock(GrantedAuthority.class);
        var updatedAuthority2 = mock(GrantedAuthority.class);
        var updatedAuthorities = Set.of(updatedAuthority1, updatedAuthority2);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));
        when(role1.getProject()).thenReturn(project1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(role2.getProject()).thenReturn(project2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(role3.getProject()).thenReturn(project3);
        when(project3.getCode()).thenReturn(projectCode);
        when(userEntityRepository.save(user)).thenReturn(updatedUser);
        when(updatedUser.getMatchingAuthorities()).thenReturn(updatedAuthorities);

        // Then
        userService.removeLoggedInUserProjectFromScope(projectCode);
        var userToSaveArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userEntityRepository).save(userToSaveArgumentCaptor.capture());
        var userToSave = userToSaveArgumentCaptor.getValue();
        assertThat(userToSave.getRolesOnProjectWhenScopedUser()).containsExactlyInAnyOrder(role1, role2);

        var authenticationToUpdateArgumentCaptor = ArgumentCaptor.forClass(OAuth2AuthenticationToken.class);
        verify(securityContext, times(1)).setAuthentication(authenticationToUpdateArgumentCaptor.capture());
        var authenticationToUpdate = authenticationToUpdateArgumentCaptor.getValue();
        assertThat(authenticationToUpdate.getPrincipal()).isSameAs(principal);
        assertThat(authenticationToUpdate.getAuthorities()).containsExactlyInAnyOrder(updatedAuthority1, updatedAuthority2);
        assertThat(authenticationToUpdate.getAuthorizedClientRegistrationId()).isEqualTo(providerName);
    }

    @Test
    void updateAuthoritiesFromLoggedInUser_throwBadRequestException_whenAuthenticationIsNull() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // When
        when(securityContext.getAuthentication()).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateAuthoritiesFromLoggedInUser());
    }

    @Test
    void updateAuthoritiesFromLoggedInUser_throwBadRequestException_whenUserIsNotAuthenticated() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateAuthoritiesFromLoggedInUser());
    }

    @Test
    void updateAuthoritiesFromLoggedInUser_throwBadRequestException_whenAuthenticationIsNotAnInstanceOfOAuth2AuthenticationToken() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(UsernamePasswordAuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateAuthoritiesFromLoggedInUser());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateAuthoritiesFromLoggedInUser_throwBadRequestException_whenUserLoginIsBlank(String userLogin) {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateAuthoritiesFromLoggedInUser());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateAuthoritiesFromLoggedInUser_throwBadRequestException_whenProviderNameIsBlank(String providerName) {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateAuthoritiesFromLoggedInUser());
    }

    @Test
    void updateAuthoritiesFromLoggedInUser_throwBadRequestException_whenUserNotFound() {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);

        var userLogin = "user-login";
        var providerName = "provider-name";

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.empty());

        // Then
        assertThrows(BadRequestException.class, () -> userService.updateAuthoritiesFromLoggedInUser());
    }

    @Test
    void updateAuthoritiesFromLoggedInUser_updateUserAuthorities_whenUserFound() throws BadRequestException {
        // Given
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(OAuth2AuthenticationToken.class);
        var principal = mock(OAuth2User.class);

        var userLogin = "user-login";
        var providerName = "provider-name";
        var user = mock(UserEntity.class);

        var updatedAuthority1 = mock(GrantedAuthority.class);
        var updatedAuthority2 = mock(GrantedAuthority.class);
        var updatedAuthority3 = mock(GrantedAuthority.class);
        var updatedAuthorities = Set.of(updatedAuthority1, updatedAuthority2, updatedAuthority3);

        // When
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userLogin);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(providerName);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName))).thenReturn(Optional.of(user));
        when(user.getMatchingAuthorities()).thenReturn(updatedAuthorities);

        // Then
        userService.updateAuthoritiesFromLoggedInUser();

        var authenticationToUpdateArgumentCaptor = ArgumentCaptor.forClass(OAuth2AuthenticationToken.class);
        verify(securityContext, times(1)).setAuthentication(authenticationToUpdateArgumentCaptor.capture());
        var authenticationToUpdate = authenticationToUpdateArgumentCaptor.getValue();
        assertThat(authenticationToUpdate.getPrincipal()).isSameAs(principal);
        assertThat(authenticationToUpdate.getAuthorities()).containsExactlyInAnyOrder(updatedAuthority1, updatedAuthority2, updatedAuthority3);
        assertThat(authenticationToUpdate.getAuthorizedClientRegistrationId()).isEqualTo(providerName);
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
        when(oidcUser.getName()).thenReturn(login);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.of(userFoundInDatabase));
        when(userFoundInDatabase.getMatchingAuthorities()).thenReturn(authorities);

        // Then
        var returnedAuthorities = userService.manageUserAtLogin(oidcUser, provider);
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
        when(oidcUser.getName()).thenReturn(login);
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getGivenName()).thenReturn(firstName);
        when(oidcUser.getFamilyName()).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(provider, login, projectRepository)).thenReturn(Optional.of(userCreatedFromConfigurationFile));
        when(userCreatedFromConfigurationFile.getMatchingAuthorities()).thenReturn(authorities);

        // Then
        var returnedAuthorities = userService.manageUserAtLogin(oidcUser, provider);
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
        when(oidcUser.getName()).thenReturn(login);
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getGivenName()).thenReturn(firstName);
        when(oidcUser.getFamilyName()).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(provider, login, projectRepository)).thenReturn(Optional.empty());

        // Then
        var actualAuthorities = userService.manageUserAtLogin(oidcUser, provider);
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
        when(oidcUser.getName()).thenReturn(login);
        when(oidcUser.getEmail()).thenReturn(null);
        when(oidcUser.getGivenName()).thenReturn(null);
        when(oidcUser.getFamilyName()).thenReturn(null);
        when(oidcUser.getAttribute(StandardClaimNames.EMAIL)).thenReturn(email);
        when(oidcUser.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(firstName);
        when(oidcUser.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(provider, login, projectRepository)).thenReturn(Optional.empty());

        // Then
        var actualAuthorities = userService.manageUserAtLogin(oidcUser, provider);
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

        var emailField = StandardClaimNames.EMAIL;
        var familyNameField = StandardClaimNames.FAMILY_NAME;
        var givenNameField = StandardClaimNames.GIVEN_NAME;

        var customEmailField = "custom_email";
        var customFamilyNameField = "custom_family_name";
        var customGivenNameField = "custom_given_name";

        // When
        when(oidcUser.getName()).thenReturn(login);
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
        var actualAuthorities = userService.manageUserAtLogin(oidcUser, providerName);
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

    @Test
    void manageUserAtLogin_returnNewUserMatchingAuthoritiesAndFetchDetailsFromAttributes_whenUserNeitherFoundInDatabaseNorInConfigurationFileAndIsOauth2User() {
        // Given
        var oauth2User = mock(OAuth2User.class);

        var login = "user_login";
        var provider = "provider_name";
        var email = "user@email.com";
        var firstName = "user_firstName";
        var lastName = "user_lastName";

        // When
        when(oauth2User.getName()).thenReturn(login);
        when(oauth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn(email);
        when(oauth2User.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(firstName);
        when(oauth2User.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(lastName);
        when(userEntityRepository.findById(new UserEntity.UserEntityId(login, provider))).thenReturn(Optional.empty());
        when(providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(provider, login, projectRepository)).thenReturn(Optional.empty());

        // Then
        var actualAuthorities = userService.manageUserAtLogin(oauth2User, provider);
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
    void manageUserAtLogin_returnNewUserMatchingAuthoritiesAndFetchAdditionalDetailsFromCustomAttributes_whenUserNeitherFoundInDatabaseNorInConfigurationFileAndOAuth2UserHaveNoAttributes() {
        // Given
        var oauth2User = mock(OAuth2User.class);

        var login = "user_login";
        var providerName = "provider_name";
        var email = "user@email.com";
        var firstName = "user_firstName";
        var lastName = "user_lastName";

        var emailField = StandardClaimNames.EMAIL;
        var familyNameField = StandardClaimNames.FAMILY_NAME;
        var givenNameField = StandardClaimNames.GIVEN_NAME;

        var customEmailField = "custom_email";
        var customFamilyNameField = "custom_family_name";
        var customGivenNameField = "custom_given_name";

        // When
        when(oauth2User.getName()).thenReturn(login);
        when(oauth2User.getAttribute(emailField)).thenReturn(null);
        when(oauth2User.getAttribute(familyNameField)).thenReturn(null);
        when(oauth2User.getAttribute(givenNameField)).thenReturn(null);
        when(oauth2User.getAttribute(customEmailField)).thenReturn(email);
        when(oauth2User.getAttribute(customFamilyNameField)).thenReturn(firstName);
        when(oauth2User.getAttribute(customGivenNameField)).thenReturn(lastName);
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
        var actualAuthorities = userService.manageUserAtLogin(oauth2User, providerName);
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
