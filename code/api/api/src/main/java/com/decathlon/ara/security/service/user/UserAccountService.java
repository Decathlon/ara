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
import com.decathlon.ara.security.mapper.UserMapper;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.security.service.user.strategy.select.UserStrategySelector;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.ProjectMapper;
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
import java.util.stream.Stream;

import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;

@Service
public class UserAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountService.class);

    private final UserStrategySelector userStrategySelector;

    private final UserEntityRepository userEntityRepository;

    private final UserEntityRoleOnProjectRepository userEntityRoleOnProjectRepository;

    private final ProjectRepository projectRepository;

    private final OAuth2ProvidersConfiguration providersConfiguration;

    private final UserMapper userMapper;

    private final ProjectMapper projectMapper;

    private final ProjectService projectService;

    private final UserSessionService userSessionService;

    public UserAccountService(
            UserStrategySelector userStrategySelector,
            UserEntityRepository userEntityRepository,
            UserEntityRoleOnProjectRepository userEntityRoleOnProjectRepository,
            ProjectRepository projectRepository,
            OAuth2ProvidersConfiguration providersConfiguration,
            UserMapper userMapper,
            ProjectMapper projectMapper,
            ProjectService projectService,
            UserSessionService userSessionService
    ) {
        this.userStrategySelector = userStrategySelector;
        this.userEntityRepository = userEntityRepository;
        this.userEntityRoleOnProjectRepository = userEntityRoleOnProjectRepository;
        this.projectRepository = projectRepository;
        this.providersConfiguration = providersConfiguration;
        this.userMapper = userMapper;
        this.projectMapper = projectMapper;
        this.projectService = projectService;
        this.userSessionService = userSessionService;
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

        var userLogin = getUserLoginFromOAuth2UserAndProviderName(oauth2User, providerName);
        return getUserEntityFromLoginAndProviderName(userLogin, providerName).map(userMapper::getUserAccountFromPersistedUser);
    }

    private String getUserLoginFromOAuth2UserAndProviderName(OAuth2User oauth2User, String providerName) {
        var strategy = userStrategySelector.selectUserStrategyFromProviderName(providerName);
        return strategy.getLogin(oauth2User);
    }

    private Optional<UserEntity> getUserEntityFromLoginAndProviderName(@NonNull String userLogin, @NonNull String providerName) {
        return userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName));
    }

    /**
     * Fetch the currently logged-in {@link UserEntity}
     * @return the current {@link UserEntity}
     */
    public Optional<UserEntity> getCurrentUserEntity() {
        try {
            var principalAndProviderName = getCurrentOAuth2UserAndProviderName();
            var oauth2User = principalAndProviderName.getFirst();
            var providerName = principalAndProviderName.getSecond();
            var userLogin = getUserLoginFromOAuth2UserAndProviderName(oauth2User, providerName);
            return getUserEntityFromLoginAndProviderName(userLogin, providerName);
        } catch (ForbiddenException e) {
            LOG.warn("Current user not found...");
        }
        return Optional.empty();
    }

    private Pair<OAuth2User, String> getCurrentOAuth2UserAndProviderName() throws ForbiddenException {
        var exception =  new ForbiddenException(Entities.SECURITY, "current authentication details access");
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            throw exception;
        }
        var oauth2User = oauth2Authentication.getPrincipal();
        var providerName = oauth2Authentication.getAuthorizedClientRegistrationId();
        if (oauth2User == null || StringUtils.isBlank(providerName)) {
            throw exception;
        }
        return Pair.of(oauth2User, providerName);
    }

    private Optional<UserEntity> getUserEntityFromLogin(String userLogin) {
        try {
            var principalAndProviderName = getCurrentOAuth2UserAndProviderName();
            var providerName = principalAndProviderName.getSecond();
            return getUserEntityFromLoginAndProviderName(userLogin, providerName);
        } catch (ForbiddenException e) {
            LOG.warn("User not found...");
        }
        return Optional.empty();
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

        var pictureUrl = strategy.getPictureUrl(oauth2User);
        pictureUrl.ifPresent(userToSave::setPictureUrl);

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
        return userMapper.getUserAccountFromPersistedUser(finalSavedUser);
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
     * Fetch the projects ({@link ProjectDTO}) the current user can access:
     * - super admins and auditors can access all projects
     * - scoped users can only access the projects in their scopes, plus the demo project (if found)
     * - no projects are returned otherwise
     * @return the current user projects
     * @throws ForbiddenException thrown if this operation failed
     */
    public List<ProjectDTO> getCurrentUserProjects() throws ForbiddenException {
        var exception = new ForbiddenException(Entities.PROJECT, "fetch user projects");

        var currentUser = getCurrentUserEntity().orElseThrow(() -> exception);
        var profile = currentUser.getProfile();
        if (profile == null) {
            throw exception;
        }

        Stream<Project> userProjectsStream = Stream.empty();
        var currentUserCanAccessAllProjects = UserEntity.UserEntityProfile.SUPER_ADMIN.equals(profile) || UserEntity.UserEntityProfile.AUDITOR.equals(profile);
        if (currentUserCanAccessAllProjects) {
            userProjectsStream = projectRepository.findAllByOrderByName().stream();
        }
        var currentUserIsScopedUser = UserEntity.UserEntityProfile.SCOPED_USER.equals(profile);
        if (currentUserIsScopedUser) {
            var scopedProjectsStream = currentUser.getRolesOnProjectWhenScopedUser().stream().map(UserEntityRoleOnProject::getProject);
            var demoProjectStream = projectRepository.findByCode(DEMO_PROJECT_CODE).map(Stream::of).orElse(Stream.empty());
            userProjectsStream = Stream.concat(scopedProjectsStream, demoProjectStream);
        }
        return userProjectsStream.map(projectMapper::getProjectDTOFromProjectEntity).toList();
    }

    /**
     * Remove a project from the current user scope
     * @param projectCode the project code matching the project
     * @throws ForbiddenException thrown if this operation failed
     */
    public void removeProjectFromCurrentUserScope(@NonNull String projectCode) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "remove current user project scope", projectCodeContext);

        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);
        var targetUser = getCurrentUserEntity().orElseThrow(() -> exception);

        removeProjectFromUserScope(targetUser, targetProject);
        userSessionService.refreshCurrentUserAuthorities();
    }

    private Pair[] getProjectCodeExceptionContext(String projectCode) {
        return StringUtils.isNotBlank(projectCode) ? new Pair[]{Pair.of("code", projectCode)} : new Pair[]{};
    }

    private Optional<Project> getProjectFromCode(String projectCode) {
        if (StringUtils.isBlank(projectCode)) {
            return Optional.empty();
        }

        return projectRepository.findByCode(projectCode);
    }

    private void removeProjectFromUserScope(UserEntity user, Project project) {
        userEntityRoleOnProjectRepository.deleteById(new UserEntityRoleOnProject.UserEntityRoleOnProjectId(user, project));
    }

    /**
     * Remove a project from a user scope
     * @param userLogin the user login
     * @param projectCode the project code matching the project
     * @throws ForbiddenException thrown if this operation failed
     */
    public void removeProjectFromUserScope(@NonNull String userLogin, @NonNull String projectCode) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "remove user project scope", projectCodeContext);

        if (StringUtils.isBlank(userLogin)) {
            throw exception;
        }

        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);
        var targetUser = getUserEntityFromLogin(userLogin).orElseThrow(() -> exception);

        removeProjectFromUserScope(targetUser, targetProject);
    }

    /**
     * Update the current user scope.
     * If the project is already in the scope, then its role is updated
     * Otherwise, a new scope is added containing the project and the role
     * @param projectCode the project code
     * @param accountRole the new role
     * @throws ForbiddenException thrown if this operation failed
     */
    public void updateCurrentUserProjectScope(@NonNull String projectCode, @NonNull UserAccountScopeRole accountRole) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "update current user project scope", projectCodeContext);

        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);
        var targetUser = getCurrentUserEntity().orElseThrow(() -> exception);
        updateUserProjectScope(targetUser, targetProject, accountRole);
        
        userSessionService.refreshCurrentUserAuthorities();
    }

    private void updateUserProjectScope(UserEntity targetUser, Project targetProject, UserAccountScopeRole targetAccountRole) {
        var targetRole = getUserEntityScopedUserRoleOnProjectFromUserAccountScopeRole(targetAccountRole);
        updateScopedUserRoleOnProject(targetUser, targetProject, targetRole);
        userEntityRepository.save(targetUser);
    }

    private UserEntityRoleOnProject.ScopedUserRoleOnProject getUserEntityScopedUserRoleOnProjectFromUserAccountScopeRole(@NonNull UserAccountScopeRole userAccountScopeRole) {
        return UserEntityRoleOnProject.ScopedUserRoleOnProject.valueOf(userAccountScopeRole.name());
    }

    private void updateScopedUserRoleOnProject(UserEntity targetUser, Project targetProject, UserEntityRoleOnProject.ScopedUserRoleOnProject targetRole) {
        var projectCode = targetProject.getCode();
        var userRoles = targetUser.getRolesOnProjectWhenScopedUser();
        for (var userRole : userRoles) {
            var roleProject = userRole.getProject();
            var roleProjectCode = roleProject.getCode();
            if (projectCode.equals(roleProjectCode)) {
                userRole.setRole(targetRole);
            }
        }
        var projectScopeNotFound = userRoles.stream()
                .map(UserEntityRoleOnProject::getProject)
                .map(Project::getCode)
                .noneMatch(projectCode::equals);
        if (projectScopeNotFound) {
            userRoles.add(new UserEntityRoleOnProject(targetUser, targetProject, targetRole));
        }
    }

    /**
     * Update a user scope.
     * If the project is already in the scope, then its role is updated
     * Otherwise, a new scope is added containing the project and the role
     * @param userLogin the user login
     * @param projectCode the project code
     * @param accountRole the new role
     * @throws ForbiddenException thrown if this operation failed
     */
    public void updateUserProjectScope(@NonNull String userLogin, @NonNull String projectCode, @NonNull UserAccountScopeRole accountRole) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "update user project scope", projectCodeContext);

        if (StringUtils.isBlank(userLogin)) {
            throw exception;
        }

        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);
        var targetUser = getUserEntityFromLogin(userLogin).orElseThrow(() -> exception);
        updateUserProjectScope(targetUser, targetProject, accountRole);
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
        return userMapper.getUserAccountFromPersistedUser(updatedUser);
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
        return userMapper.getUserAccountFromPersistedUser(updatedUser);
    }

    /**
     * Get all user accounts (only for the current OAuth2 provider)
     * @param authentication the current authentication
     * @return the user accounts
     */
    public List<UserAccount> getAllUserAccounts(@NonNull OAuth2AuthenticationToken authentication) {
        var providerName = authentication.getAuthorizedClientRegistrationId();

        var persistedUsers = userEntityRepository.findAllByProviderName(providerName);
        return persistedUsers.stream().map(userMapper::getUserAccountFromPersistedUser).toList();
    }

    /**
     * Get all the scoped user accounts (only for the current OAuth2 provider).
     * Note that account scopes contain only projects shared by the current user.
     * @param authentication the current authentication
     * @param roleFilter if present, filter on scoped users having at least this role on a project
     * @return the user accounts
     */
    public List<UserAccount> getAllScopedUserAccounts(@NonNull OAuth2AuthenticationToken authentication, Optional<UserAccountScopeRole> roleFilter) {
        return getAllScopedUserAccounts(authentication, Optional.empty(), roleFilter);
    }

    private List<UserAccount> getAllScopedUserAccounts(@NonNull OAuth2AuthenticationToken authentication, Optional<String> projectCodeFilter, Optional<UserAccountScopeRole> roleFilter) {
        var providerName = authentication.getAuthorizedClientRegistrationId();

        var actualUserEntityRole = roleFilter.map(this::getUserEntityScopedUserRoleOnProjectFromUserAccountScopeRole).orElse(null);

        var persistedScopedUsers = userEntityRepository.findAllScopedUsersByProviderName(providerName, projectCodeFilter.orElse(null), actualUserEntityRole);

        var profile = userSessionService.getCurrentUserProfile();
        if (profile.isEmpty()) {
            return new ArrayList<>();
        }

        var userAccounts = persistedScopedUsers.stream().map(userMapper::getUserAccountFromPersistedUser).toList();

        var currentUserIsScopedUser = UserAccountProfile.SCOPED_USER.equals(profile.get());
        if (currentUserIsScopedUser) {
            filterScopedUserAccountsScopes(userAccounts);
        }

        return userAccounts;
    }

    private void filterScopedUserAccountsScopes(List<UserAccount> userAccounts) {
        var scopedProjectCodes = userSessionService.getCurrentUserScopedProjectCodes();
        userAccounts.forEach(account -> {
            var filteredScopes = account.getScopes()
                    .stream()
                    .filter(scope -> scopedProjectCodes.contains(scope.getProject()))
                    .toList();
            account.setScopes(filteredScopes);
        });
    }

    /**
     * Get all the scoped user accounts having access to a given project (only for the current OAuth2 provider).
     * Note that account scopes contain only projects shared by the current user.
     * @param authentication the current authentication
     * @param projectCode the project code
     * @param roleFilter if present, filter only on scoped users having this role on the project 'projectCode'
     * @return the user accounts
     */
    public List<UserAccount> getAllScopedUserAccountsOnProject(@NonNull OAuth2AuthenticationToken authentication, String projectCode, Optional<UserAccountScopeRole> roleFilter) {
        return getAllScopedUserAccounts(authentication, Optional.of(projectCode), roleFilter);
    }
}
