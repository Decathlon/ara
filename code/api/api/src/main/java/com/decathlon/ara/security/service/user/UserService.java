package com.decathlon.ara.security.service.user;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.dto.user.LoggedInUserDTO;
import com.decathlon.ara.security.dto.user.LoggedInUserScopeDTO;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private static final String USER_AUTHENTICATION_ERROR = "authenticated_user_error";

    private final OAuth2ProvidersConfiguration providersConfiguration;

    private final ProjectRepository projectRepository;

    private final UserEntityRepository userEntityRepository;

    private final AuthorityService authorityService;

    public UserService(
            OAuth2ProvidersConfiguration providersConfiguration, ProjectRepository projectRepository,
            UserEntityRepository userEntityRepository,
            AuthorityService authorityService
    ) {
        this.providersConfiguration = providersConfiguration;
        this.projectRepository = projectRepository;
        this.userEntityRepository = userEntityRepository;
        this.authorityService = authorityService;
    }

    /**
     * Manage the user at login: if needed, save the user and return its granted authorities.
     * @param oAuth2User the OAuth2 user
     * @param providerName the provider name
     * @return the granted authorities the user has
     */
    public Set<GrantedAuthority> manageUserAtLogin(OAuth2User oAuth2User, String providerName) {
        String userLogin = getLoginFromOAuth2User(oAuth2User);
        var registeredUser = userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName));
        var user = registeredUser.orElseGet(() -> createNewUser(oAuth2User, providerName));
        return user.getMatchingAuthorities();
    }

    private UserEntity createNewUser(OAuth2User oauth2User, String providerName) {
        String userLogin = getLoginFromOAuth2User(oauth2User);
        var userFoundInConfiguration = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, userLogin, projectRepository);
        var newUser = userFoundInConfiguration.orElse(new UserEntity(userLogin, providerName));

        var customAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);

        Optional<String> email = getUserFieldValue(oauth2User, customAttributes, StandardClaimNames.EMAIL);
        email.ifPresent(newUser::setEmail);

        Optional<String> firstName = getUserFieldValue(oauth2User, customAttributes, StandardClaimNames.GIVEN_NAME);
        firstName.ifPresent(newUser::setFirstName);

        Optional<String> lastName = getUserFieldValue(oauth2User, customAttributes, StandardClaimNames.FAMILY_NAME);
        lastName.ifPresent(newUser::setLastName);

        userEntityRepository.save(newUser);
        return newUser;
    }

    private static String getLoginFromOAuth2User(OAuth2User oauth2User) {
        return oauth2User.getName();
    }

    private static Optional<String> getUserFieldValue(OAuth2User oauth2User, Map<String, String> customAttributes, String userField) {
        if (oauth2User instanceof OidcUser oidcUser) {
            Optional<String> userFieldValueFromOidcUser = getUserFieldValueFromOidcUser(oidcUser, userField);
            if (userFieldValueFromOidcUser.isPresent()) {
                return userFieldValueFromOidcUser;
            }
        }
        return getUserFieldValueFromAttribute(oauth2User, customAttributes, userField);
    }

    private static Optional<String> getUserFieldValueFromOidcUser(OidcUser oidcUser, String userField) {
        try {
            var getterName = "get" + CaseUtils.toCamelCase(userField, true, '_');
            var getterMethod = oidcUser.getClass().getMethod(getterName);
            var getterReturnedValue = (String) getterMethod.invoke(oidcUser);
            if (StringUtils.isNotBlank(getterReturnedValue)) {
                return Optional.of(getterReturnedValue);
            }
            return Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.warn("The (oidc) user field '{}' was not found...", userField);
            return Optional.empty();
        }
    }

    private static Optional<String> getUserFieldValueFromAttribute(OAuth2User oauth2User, Map<String, String> customAttributes, String userField) {
        var userFieldValue = (String) oauth2User.getAttribute(userField);
        if (StringUtils.isNotBlank(userFieldValue)) {
            return Optional.of(userFieldValue);
        }
        var customUserField = customAttributes.get(userField);
        return Optional.ofNullable(oauth2User.getAttribute(customUserField));
    }

    /**
     * Get the logged-in matching {@link UserEntity}, if found
     * @return a {@link UserEntity}
     */
    public Optional<UserEntity> getCurrentUserEntity() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return Optional.empty();
        }

        var userLogin = authentication.getName();
        if (StringUtils.isBlank(userLogin)) {
            return Optional.empty();
        }

        var providerName = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        if (StringUtils.isBlank(providerName)) {
            return Optional.empty();
        }

        return userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName));
    }

    /**
     * Get the logged-in user, if found
     * @param authentication the authentication
     * @return the logged-in user
     */
    public Optional<LoggedInUserDTO> getLoggedInUserDTO(OAuth2AuthenticationToken authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        var principal = authentication.getPrincipal();
        if (principal == null) {
            return Optional.empty();
        }

        var providerName = authentication.getAuthorizedClientRegistrationId();
        var customAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);

        var userLogin = getLoginFromOAuth2User(principal);
        var user = new LoggedInUserDTO(userLogin);

        Optional<String> email = getUserFieldValue(principal, customAttributes, StandardClaimNames.EMAIL);
        email.ifPresent(user::setEmail);

        Optional<String> firstName = getUserFieldValue(principal, customAttributes, StandardClaimNames.GIVEN_NAME);
        firstName.ifPresent(user::setFirstName);

        Optional<String> lastName = getUserFieldValue(principal, customAttributes, StandardClaimNames.FAMILY_NAME);
        lastName.ifPresent(user::setLastName);

        Optional<String> pictureUrl = getUserFieldValue(principal, customAttributes, StandardClaimNames.PICTURE);
        pictureUrl.ifPresent(user::setPictureUrl);

        var profile = authorityService.getProfile().orElse(null);
        if (profile != null) {
            user.setProfile(profile.name());
        }

        List<LoggedInUserScopeDTO> scopes = new ArrayList<>();
        if (UserEntity.UserEntityProfile.SCOPED_USER.equals(profile)) {
            scopes = authorityService.getLoggedInUserScopes();
            if (CollectionUtils.isEmpty(scopes)) {
                scopes = new ArrayList<>();
            }
        }
        user.setScopes(scopes);
        return Optional.of(user);
    }

    /**
     * Update the logged-in user project scopes.
     * If the project is already in scope, then the role is updated.
     * Otherwise, it is added with the given role
     * @param projectCode the project code
     * @param scope the new role on the project
     * @throws BadRequestException thrown if the update failed (e.g. user not connected, project doesn't exist, etc.)
     */
    public void updateLoggedInUserProjectScopes(String projectCode, UserEntityRoleOnProject.ScopedUserRoleOnProject scope) throws BadRequestException {
        if (scope == null) {
            throw new BadRequestException("Cannot update the project scope because the scope given was null", Entities.SECURITY, "project_scope_null");
        }

        if (StringUtils.isBlank(projectCode)) {
            throw new BadRequestException("Cannot update the project scope because the project code given was blank", Entities.SECURITY, "project_code_blank");
        }

        var project = projectRepository.findByCode(projectCode).orElseThrow(() -> new NotFoundException("Cannot update the project scope because the project given was not found", Entities.SECURITY));

        var user = getCurrentUserEntity().orElseThrow(() -> new BadRequestException("Cannot update the project scope because no logged in user was found", Entities.SECURITY, USER_AUTHENTICATION_ERROR));
        var userRoles = user.getRolesOnProjectWhenScopedUser();
        for (var userRole : userRoles) {
            var roleProject = userRole.getProject();
            var roleProjectCode = roleProject.getCode();
            if (projectCode.equals(roleProjectCode)) {
                userRole.setRole(scope);
            }
        }
        var projectScopeNotFound = userRoles.stream()
                .map(UserEntityRoleOnProject::getProject)
                .map(Project::getCode)
                .noneMatch(projectCode::equals);
        if (projectScopeNotFound) {
            userRoles.add(new UserEntityRoleOnProject(user, project, scope));
        }

        var updatedUser = userEntityRepository.save(user);
        updateAuthorities(updatedUser.getMatchingAuthorities());
    }

    private void updateAuthorities(Collection<GrantedAuthority> authorities) {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(
                        ((OAuth2AuthenticationToken) authentication).getPrincipal(),
                        authorities,
                        ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()
                )
        );
    }

    /**
     * Refresh authorities
     * @throws BadRequestException if the update failed
     */
    public void updateAuthoritiesFromLoggedInUser() throws BadRequestException {
        var user = getCurrentUserEntity().orElseThrow(() -> new BadRequestException("Cannot update user authorities because no logged in user was found", Entities.SECURITY, USER_AUTHENTICATION_ERROR));
        updateAuthorities(user.getMatchingAuthorities());
    }

    /**
     * Remove a project from user scopes
     * @param projectCode the project code
     * @throws BadRequestException thrown if the removal failed (e.g. project not found in user scopes, etc.)
     */
    public void removeLoggedInUserProjectFromScope(String projectCode) throws BadRequestException {
        if (StringUtils.isBlank(projectCode)) {
            throw new BadRequestException("Cannot remove the project scope because the project code given was blank", Entities.SECURITY, "project_code_blank");
        }

        var user = getCurrentUserEntity().orElseThrow(() -> new BadRequestException("Cannot delete the project scope because no logged in user was found", Entities.SECURITY, USER_AUTHENTICATION_ERROR));

        var userRoles = user.getRolesOnProjectWhenScopedUser();
        if (CollectionUtils.isEmpty(userRoles)) {
            throw new BadRequestException("Cannot remove the project scope because the project was not found in the user scopes", Entities.SECURITY, "project_not_found_in_user_scopes");
        }
        var userRolesWithoutProjectScope = userRoles.stream()
                .filter(role -> !projectCode.equals(role.getProject().getCode()))
                .toList();
        var projectWasNotInUserScopes = userRoles.size() == userRolesWithoutProjectScope.size();
        if (projectWasNotInUserScopes) {
            throw new BadRequestException("Cannot remove the project scope because the project was not found in the user scopes", Entities.SECURITY, "project_not_found_in_user_scopes");
        }
        user.setRolesOnProjectWhenScopedUser(userRolesWithoutProjectScope);
        var updatedUser = userEntityRepository.save(user);
        updateAuthorities(updatedUser.getMatchingAuthorities());
    }

}
