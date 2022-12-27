package com.decathlon.ara.security.service.user;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRoleOnProjectRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.security.service.user.strategy.select.UserStrategySelector;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotUniqueException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountService.class);

    private final UserStrategySelector userStrategySelector;

    private final UserEntityRepository userEntityRepository;

    private final UserEntityRoleOnProjectRepository userEntityRoleOnProjectRepository;

    private final ProjectRepository projectRepository;

    private final OAuth2ProvidersConfiguration providersConfiguration;

    private final ProjectService projectService;

    private final AuthorityService authorityService;

    public UserAccountService(
            UserStrategySelector userStrategySelector,
            UserEntityRepository userEntityRepository,
            UserEntityRoleOnProjectRepository userEntityRoleOnProjectRepository,
            OAuth2ProvidersConfiguration providersConfiguration,
            ProjectService projectService,
            ProjectRepository projectRepository,
            AuthorityService authorityService
    ) {
        this.userStrategySelector = userStrategySelector;
        this.userEntityRepository = userEntityRepository;
        this.userEntityRoleOnProjectRepository = userEntityRoleOnProjectRepository;
        this.providersConfiguration = providersConfiguration;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.authorityService = authorityService;
    }

    public DefaultOAuth2UserService getDefaultOAuth2UserService() {
        return new DefaultOAuth2UserService();
    }

    public OidcUserService getOidcUserService() {
        return new OidcUserService();
    }

    /**
     * Fetch the currently logged-in {@link UserAccount}, if found
     * @return the current {@link UserAccount}
     */
    public Optional<UserAccount> getCurrentUserAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauth2Authentication) {
            return getCurrentUserAccount(oauth2Authentication);
        }

        return Optional.empty();
    }

    /**
     * Fetch the currently logged-in {@link UserAccount} from the {@link OAuth2AuthenticationToken}, if found
     * @param authentication the (OAuth2) authentication
     * @return the current {@link UserAccount}
     */
    public Optional<UserAccount> getCurrentUserAccount(OAuth2AuthenticationToken authentication) {
        var oauth2User = authentication.getPrincipal();
        var providerName = authentication.getAuthorizedClientRegistrationId();
        return getCurrentUserAccount(oauth2User, providerName);
    }

    /**
     * Fetch the currently logged-in {@link UserAccount} from the {@link OAuth2User} and providerName, if found
     * @param oauth2User the OAuth2 user
     * @param providerName the provider name
     * @return the current {@link UserAccount}
     */
    public Optional<UserAccount> getCurrentUserAccount(OAuth2User oauth2User, String providerName) {
        if (oauth2User == null || StringUtils.isBlank(providerName)) {
            return Optional.empty();
        }

        var strategy = userStrategySelector.selectUserStrategyFromProviderName(providerName);
        var userLogin = strategy.getLogin(oauth2User);
        return getCurrentUserEntity(userLogin, providerName).map(userEntity -> strategy.getUserAccount(oauth2User, userEntity));
    }

    private Optional<UserEntity> getCurrentUserEntity(String userLogin, String providerName) {
        return userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName));
    }

    public Optional<UserEntity> getCurrentUserEntity() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            return Optional.empty();
        }
        var oauth2User = oauth2Authentication.getPrincipal();
        var providerName = oauth2Authentication.getAuthorizedClientRegistrationId();
        if (oauth2User == null || StringUtils.isBlank(providerName)) {
            return Optional.empty();
        }

        var strategy = userStrategySelector.selectUserStrategyFromProviderName(providerName);
        var userLogin = strategy.getLogin(oauth2User);
        return getCurrentUserEntity(userLogin, providerName);
    }

    /**
     * Create a new {@link UserAccount} from a {@link OAuth2User} and a providerName
     * @param oauth2User the OAuth2 user
     * @param providerName the provider name
     * @return the created {@link UserAccount}
     */
    public UserAccount createUserAccount(OAuth2User oauth2User, String providerName) {
        var strategy = userStrategySelector.selectUserStrategyFromProviderName(providerName);
        var userLogin = strategy.getLogin(oauth2User);

        var userToSave = new UserEntity(userLogin, providerName);

        var firstName = strategy.getFirstName(oauth2User);
        firstName.ifPresent(userToSave::setFirstName);

        var lastName = strategy.getLastName(oauth2User);
        lastName.ifPresent(userToSave::setLastName);

        var email = strategy.getEmail(oauth2User);
        email.ifPresent(userToSave::setEmail);

        var profileConfiguration = providersConfiguration.getUserProfileConfiguration(providerName, userLogin);
        var userProfile = profileConfiguration.flatMap(UserAccountService::getUserProfileFromConfiguration);
        userProfile.ifPresent(userToSave::setProfile);

        var savedUser = userEntityRepository.save(userToSave);
        var finalSavedUser = savedUser;

        var userRoles = profileConfiguration.map(configuration -> getUserRolesFromConfiguration(configuration, providerName, savedUser));
        userRoles.ifPresent(userEntityRoleOnProjectRepository::saveAll);
        if (userRoles.isPresent()) {
            finalSavedUser = userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName)).orElse(savedUser);
        }
        return strategy.getUserAccount(oauth2User, finalSavedUser);
    }

    private static Optional<UserEntity.UserEntityProfile> getUserProfileFromConfiguration(UserProfileConfiguration profileConfiguration) {
        var profileAsString = profileConfiguration.getProfile();
        return UserAccountProfile.getProfileFromString(profileAsString)
                .map(Enum::name)
                .map(UserEntity.UserEntityProfile::valueOf);
    }

    private List<UserEntityRoleOnProject> getUserRolesFromConfiguration(UserProfileConfiguration profileConfiguration, String providerName, UserEntity savedUser) {
        return CollectionUtils.isNotEmpty(profileConfiguration.getScopes()) ?
                profileConfiguration.getScopes()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        scope -> getScopedUserRoleOnProjectFromRoleAsString(scope.getScope()),
                                        scope -> scope.getProjects()
                                                .stream()
                                                .map(projectCode -> getProjectFromCode(projectCode, providerName, savedUser))
                                                .filter(Objects::nonNull)
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
                                                .toList()
                                )
                        )
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().isPresent())
                        .flatMap(entry -> entry.getValue()
                                .stream()
                                .map(project -> new UserEntityRoleOnProject(savedUser, project, entry.getKey().get()))
                        )
                        .toList() :
                new ArrayList<>();
    }

    private static Optional<UserEntityRoleOnProject.ScopedUserRoleOnProject> getScopedUserRoleOnProjectFromRoleAsString(String roleAsString) {
        return UserAccountScopeRole.getScopeFromString(roleAsString)
                .map(Enum::name)
                .map(UserEntityRoleOnProject.ScopedUserRoleOnProject::valueOf);
    }

    private Optional<Project> getProjectFromCode(String projectCode, String providerName, UserEntity creationUser) {
        var project = projectRepository.findByCode(projectCode);
        var isAllowedToCreateProjects = providersConfiguration.createNewProjectsIfNotFoundAtUsersInit(providerName);
        var projectNotFound = project.isEmpty();
        if (projectNotFound && isAllowedToCreateProjects) {
            try {
                projectService.createFromCode(projectCode, creationUser);
            } catch (NotUniqueException e) {
                LOG.warn("Project {} already exists", projectCode, e);
            }
            return projectRepository.findByCode(projectCode);
        }
        return project;
    }

    /**
     * Remove a project from the user scope
     * @param projectCode the project code matching the project
     * @throws ForbiddenException thrown if this operation failed
     */
    public void removeProjectFromCurrentUserAccountScope(@NonNull String projectCode) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "remove project scope", projectCodeContext);
        if (StringUtils.isBlank(projectCode)) {
            throw exception;
        }

        var project = projectRepository.findByCode(projectCode).orElseThrow(() -> exception);
        var user = getCurrentUserEntity().orElseThrow(() -> exception);

        userEntityRoleOnProjectRepository.deleteById(new UserEntityRoleOnProject.UserEntityRoleOnProjectId(user, project));
        authorityService.refreshCurrentUserAccountAuthorities();
    }

    private Pair[] getProjectCodeExceptionContext(String projectCode) {
        return StringUtils.isNotBlank(projectCode) ? new Pair[]{Pair.of("code", projectCode)} : new Pair[]{};
    }

    /**
     * Update the current user scope.
     * If the project is already in the scope, then its role is updated
     * Otherwise, a new scope is added containing the project and the role
     * @param projectCode the project code
     * @param accountRole the role
     * @throws ForbiddenException thrown if this operation failed
     */
    public void updateCurrentUserAccountProjectScope(@NonNull String projectCode, @NonNull UserAccountScopeRole accountRole) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "update project scope", projectCodeContext);

        if (StringUtils.isBlank(projectCode)) {
            throw exception;
        }

        var project = projectRepository.findByCode(projectCode).orElseThrow(() -> exception);
        var userToUpdate = getCurrentUserEntity().orElseThrow(() -> exception);
        var roleToUpdate = getUserEntityScopedUserRoleOnProjectFromUserAccountScopeRole(accountRole);

        updateUserEntityRoles(projectCode, project, userToUpdate, roleToUpdate);

        userEntityRepository.save(userToUpdate);
        authorityService.refreshCurrentUserAccountAuthorities();
    }

    private UserEntityRoleOnProject.ScopedUserRoleOnProject getUserEntityScopedUserRoleOnProjectFromUserAccountScopeRole(UserAccountScopeRole userAccountScopeRole) {
        return UserEntityRoleOnProject.ScopedUserRoleOnProject.valueOf(userAccountScopeRole.name());
    }

    private static void updateUserEntityRoles(String projectCode, Project project, UserEntity userToUpdate, UserEntityRoleOnProject.ScopedUserRoleOnProject roleToUpdate) {
        var userRoles = userToUpdate.getRolesOnProjectWhenScopedUser();
        for (var userRole : userRoles) {
            var roleProject = userRole.getProject();
            var roleProjectCode = roleProject.getCode();
            if (projectCode.equals(roleProjectCode)) {
                userRole.setRole(roleToUpdate);
            }
        }
        var projectScopeNotFound = userRoles.stream()
                .map(UserEntityRoleOnProject::getProject)
                .map(Project::getCode)
                .noneMatch(projectCode::equals);
        if (projectScopeNotFound) {
            userRoles.add(new UserEntityRoleOnProject(userToUpdate, project, roleToUpdate));
        }
    }

    /**
     * Clear the current user default project
     * @return the updated user account
     * @throws ForbiddenException thrown if the operation failed
     */
    public UserAccount clearDefaultProject() throws ForbiddenException {
        var userToUpdate = getCurrentUserEntity().orElseThrow(() -> new ForbiddenException(Entities.PROJECT, "clear default project"));
        userToUpdate.setDefaultProject(null);
        var updatedUser = userEntityRepository.save(userToUpdate);
        return getUserAccountFromUserEntity(updatedUser);
    }

    private UserAccount getUserAccountFromUserEntity(@NonNull UserEntity userEntity) {
        var authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var oauth2User = authentication.getPrincipal();
        var providerName = userEntity.getProviderName();
        var strategy = userStrategySelector.selectUserStrategyFromProviderName(providerName);
        return strategy.getUserAccount(oauth2User, userEntity);
    }

    /**
     * Update the user default project
     * @param projectCode the project code. Note that the code must exist and the user must have access to this project.
     * @return the updated user account
     * @throws ForbiddenException thrown if the operation failed
     */
    public UserAccount updateDefaultProject(@NonNull String projectCode) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "update default project", projectCodeContext);

        if (StringUtils.isBlank(projectCode)) {
            throw exception;
        }

        var defaultProject = projectRepository.findByCode(projectCode).orElseThrow(() -> exception);
        var userToUpdate = getCurrentUserEntity().orElseThrow(() -> exception);
        userToUpdate.setDefaultProject(defaultProject);
        var updatedUser = userEntityRepository.save(userToUpdate);
        return getUserAccountFromUserEntity(updatedUser);
    }
}
