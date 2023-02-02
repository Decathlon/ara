package com.decathlon.ara.security.service.user;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.role.ProjectRole;
import com.decathlon.ara.domain.security.member.user.User;
import com.decathlon.ara.domain.security.member.user.UserProfile;
import com.decathlon.ara.domain.security.member.user.UserScope;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.UserRepository;
import com.decathlon.ara.repository.security.member.user.UserScopeRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserProfileConfiguration;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;

@Service
public class UserAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountService.class);

    private final UserRepository userRepository;

    private final UserScopeRepository userScopeRepository;

    private final ProjectRepository projectRepository;

    private final OAuth2ProvidersConfiguration providersConfiguration;

    private final UserMapper userMapper;

    private final ProjectMapper projectMapper;

    private final ProjectService projectService;

    private final UserSessionService userSessionService;

    public UserAccountService(
            UserRepository userRepository,
            UserScopeRepository userScopeRepository,
            ProjectRepository projectRepository,
            OAuth2ProvidersConfiguration providersConfiguration,
            UserMapper userMapper,
            ProjectMapper projectMapper,
            ProjectService projectService,
            UserSessionService userSessionService
    ) {
        this.userRepository = userRepository;
        this.userScopeRepository = userScopeRepository;
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
        return getCurrentUser().map(userMapper::getUserAccountFromUser);
    }

    /**
     * Fetch the currently logged-in {@link UserAccount} from the {@link OAuth2AuthenticationToken}, if found
     * @param authentication the (OAuth2) authentication
     * @return the current {@link UserAccount}
     */
    public Optional<UserAccount> getCurrentUserAccountFromAuthentication(@NonNull OAuth2AuthenticationToken authentication) {
        return userSessionService.getCurrentAuthenticatedOAuth2UserFromAuthentication(authentication)
                .flatMap(authenticatedUser -> getUserFromProviderNameAndUserLogin(authenticatedUser.getProviderName(), authenticatedUser.getLogin()))
                .map(userMapper::getUserAccountFromUser);
    }

    /**
     * Fetch the currently logged-in {@link UserAccount} from the {@link AuthenticatedOAuth2User}, if found
     * @param authenticatedUser the authenticated user
     * @return the current {@link UserAccount}
     */
    public Optional<UserAccount> getCurrentUserAccountFromAuthenticatedOAuth2User(@NonNull AuthenticatedOAuth2User authenticatedUser) {
        return getUserFromProviderNameAndUserLogin(authenticatedUser.getProviderName(), authenticatedUser.getLogin()).map(userMapper::getUserAccountFromUser);
    }

    private Optional<User> getUserFromProviderNameAndUserLogin(@NonNull String providerName, @NonNull String userLogin) {
        return userRepository.findById(new User.UserId(providerName, userLogin));
    }

    /**
     * Fetch the currently logged-in {@link User}
     * @return the current {@link User}
     */
    public Optional<User> getCurrentUser() {
        return userSessionService.getCurrentAuthenticatedOAuth2User()
                .flatMap(authenticatedOAuth2User -> getUserFromProviderNameAndUserLogin(authenticatedOAuth2User.getProviderName(), authenticatedOAuth2User.getLogin()));
    }

    /**
     * Create a new {@link UserAccount} from an {@link AuthenticatedOAuth2User}
     * @param authenticatedUser the authenticated user
     * @return the created {@link UserAccount}
     * @throws ForbiddenException thrown if the user couldn't be created
     */
    public UserAccount createUserAccountFromAuthenticatedOAuth2User(@NonNull AuthenticatedOAuth2User authenticatedUser) throws ForbiddenException {
        var exception = new ForbiddenException(Entities.USER, "create new user");

        var providerName = authenticatedUser.getProviderName();
        var userLogin = authenticatedUser.getLogin();
        var userAlreadyExists = userRepository.existsById(new User.UserId(providerName, userLogin));
        if (userAlreadyExists) {
            LOG.warn("Cannot create user '{}' because he already exists", userLogin);
            throw exception;
        }

        var userToSave = new User(providerName, userLogin);

        var firstName = authenticatedUser.getFirstName();
        firstName.ifPresent(userToSave::setFirstName);

        var lastName = authenticatedUser.getLastName();
        lastName.ifPresent(userToSave::setLastName);

        var email = authenticatedUser.getEmail();
        email.ifPresent(userToSave::setEmail);

        var pictureUrl = authenticatedUser.getPictureUrl();
        pictureUrl.ifPresent(userToSave::setPictureUrl);

        var profileConfiguration = providersConfiguration.getUserProfileConfiguration(providerName, userLogin);
        var userProfile = profileConfiguration.flatMap(UserAccountService::getUserProfileFromConfiguration);
        userProfile.ifPresent(userToSave::setProfile);

        var savedUser = userRepository.save(userToSave);
        var finalSavedUser = savedUser;

        var userRoles = profileConfiguration.map(configuration -> getUserRolesFromConfiguration(configuration, providerName, savedUser));
        userRoles.ifPresent(userScopeRepository::saveAll);
        if (userRoles.isPresent()) {
            finalSavedUser = userRepository.findById(new User.UserId(providerName, userLogin)).orElse(savedUser);
        }
        return userMapper.getUserAccountFromUser(finalSavedUser);
    }

    private static Optional<UserProfile> getUserProfileFromConfiguration(UserProfileConfiguration profileConfiguration) {
        var profileAsString = profileConfiguration.getProfile();
        return UserAccountProfile.getProfileFromString(profileAsString).map(UserAccountService::getUserProfileFromUserAccountProfile);
    }

    private List<UserScope> getUserRolesFromConfiguration(UserProfileConfiguration profileConfiguration, String providerName, User savedUser) {
        return CollectionUtils.isNotEmpty(profileConfiguration.getScopes()) ?
                profileConfiguration.getScopes()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        scope -> getProjectRoleFromString(scope.getScope()),
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
                                .map(project -> new UserScope(savedUser, project, entry.getKey().get()))
                        )
                        .toList() :
                new ArrayList<>();
    }

    private static Optional<ProjectRole> getProjectRoleFromString(String roleAsString) {
        return UserAccountScopeRole.getScopeFromString(roleAsString).map(UserAccountService::getProjectRoleFromUserAccountScopeRole);
    }

    private Optional<Project> getProjectFromCode(String projectCode, String providerName, User creationUser) {
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

        var currentUser = getCurrentUser().orElseThrow(() -> exception);
        var profile = currentUser.getProfile();
        if (profile == null) {
            throw exception;
        }

        Stream<Project> userProjectsStream = Stream.empty();
        var currentUserCanAccessAllProjects = UserProfile.SUPER_ADMIN.equals(profile) || UserProfile.AUDITOR.equals(profile);
        if (currentUserCanAccessAllProjects) {
            userProjectsStream = projectRepository.findAllByOrderByName().stream();
        }
        var currentUserIsScopedUser = UserProfile.SCOPED_USER.equals(profile);
        if (currentUserIsScopedUser) {
            var scopedProjectsStream = currentUser.getScopes().stream().map(UserScope::getProject);
            var demoProjectStream = projectRepository.findByCode(DEMO_PROJECT_CODE).map(Stream::of).orElse(Stream.empty());
            userProjectsStream = Stream.concat(scopedProjectsStream, demoProjectStream).sorted(Comparator.comparing(Project::getName));
        }
        return userProjectsStream.map(projectMapper::getProjectDTOFromProject).toList();
    }

    /**
     * Remove a scope from the current user scopes
     * @param projectCode the project code
     * @throws ForbiddenException thrown if this operation failed
     */
    public void removeCurrentUserScope(@NonNull String projectCode) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "remove current user project scope", projectCodeContext);

        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2User().orElseThrow(() -> exception);
        removeUserScope(authenticatedUser.getProviderName(), authenticatedUser.getLogin(), projectCode, exception);
        userSessionService.refreshCurrentUserAuthorities();
    }

    private void removeUserScope(String providerName, String userLogin, String projectCode, ForbiddenException exception) throws ForbiddenException {
        var targetUser = getUserFromProviderNameAndUserLogin(providerName, userLogin).orElseThrow(() -> exception);
        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);

        userScopeRepository.deleteById(new UserScope.UserScopeId(targetUser, targetProject));
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

    /**
     * Remove a user scope
     * @param userLogin the user login
     * @param projectCode the project code 
     * @throws ForbiddenException thrown if this operation failed
     */
    public void removeUserScope(@NonNull String userLogin, @NonNull String projectCode) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(Entities.PROJECT, "remove user project scope", projectCodeContext);

        if (StringUtils.isBlank(userLogin)) {
            throw exception;
        }

        var providerName = userSessionService.getCurrentAuthenticatedOAuth2User().map(AuthenticatedOAuth2User::getProviderName).orElseThrow(() -> exception);
        removeUserScope(providerName, userLogin, projectCode, exception);
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

        var authenticatedUser = userSessionService.getCurrentAuthenticatedOAuth2User().orElseThrow(() -> exception);
        updateUserProjectScope(authenticatedUser.getProviderName(), authenticatedUser.getLogin(), projectCode, accountRole, exception);
        
        userSessionService.refreshCurrentUserAuthorities();
    }

    private void updateUserProjectScope(String providerName, String userLogin, String projectCode, UserAccountScopeRole targetAccountRole, ForbiddenException exception) throws ForbiddenException {
        var targetUser = getUserFromProviderNameAndUserLogin(providerName, userLogin).orElseThrow(() -> exception);
        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);

        var targetRole = getProjectRoleFromUserAccountScopeRole(targetAccountRole);
        updateScopedUserProjectRole(targetUser, targetProject, targetRole);
        userRepository.save(targetUser);
    }

    private static ProjectRole getProjectRoleFromUserAccountScopeRole(@NonNull UserAccountScopeRole userAccountScopeRole) {
        return ProjectRole.valueOf(userAccountScopeRole.name());
    }

    private void updateScopedUserProjectRole(User targetUser, Project targetProject, ProjectRole targetRole) {
        var projectCode = targetProject.getCode();
        var userRoles = targetUser.getScopes();
        for (var userRole : userRoles) {
            var roleProject = userRole.getProject();
            var roleProjectCode = roleProject.getCode();
            if (projectCode.equals(roleProjectCode)) {
                userRole.setRole(targetRole);
            }
        }
        var projectScopeNotFound = userRoles.stream()
                .map(UserScope::getProject)
                .map(Project::getCode)
                .noneMatch(projectCode::equals);
        if (projectScopeNotFound) {
            userRoles.add(new UserScope(targetUser, targetProject, targetRole));
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

        var providerName = userSessionService.getCurrentAuthenticatedOAuth2User().map(AuthenticatedOAuth2User::getProviderName).orElseThrow(() -> exception);
        updateUserProjectScope(providerName, userLogin, projectCode, accountRole, exception);
    }

    /**
     * Update the user profile.
     * @param userLogin the user login
     * @param profile the new profile
     * @throws ForbiddenException thrown if this operation failed
     */
    public void updateUserProfile(@NonNull String userLogin, @NonNull UserAccountProfile profile) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(userLogin);
        var exception = new ForbiddenException(Entities.USER, "update user profile", projectCodeContext);

        if (StringUtils.isBlank(userLogin)) {
            throw exception;
        }

        var providerName = userSessionService.getCurrentAuthenticatedOAuth2User().map(AuthenticatedOAuth2User::getProviderName).orElseThrow(() -> exception);
        var targetUser = getUserFromProviderNameAndUserLogin(providerName, userLogin).orElseThrow(() -> exception);
        var targetProfile = getUserProfileFromUserAccountProfile(profile);
        targetUser.setProfile(targetProfile);
        userRepository.save(targetUser);
    }

    private static UserProfile getUserProfileFromUserAccountProfile(@NonNull UserAccountProfile userAccountProfile) {
        return UserProfile.valueOf(userAccountProfile.name());
    }

    /**
     * Clear the current user default project
     * @return the updated user account
     * @throws ForbiddenException thrown if the operation failed
     */
    public UserAccount clearDefaultProject() throws ForbiddenException {
        var userToUpdate = getCurrentUser().orElseThrow(() -> new ForbiddenException(Entities.PROJECT, "clear default project"));
        userToUpdate.setDefaultProject(null);
        var updatedUser = userRepository.save(userToUpdate);
        return userMapper.getUserAccountFromUser(updatedUser);
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
        var userToUpdate = getCurrentUser().orElseThrow(() -> exception);
        userToUpdate.setDefaultProject(defaultProject);
        var updatedUser = userRepository.save(userToUpdate);
        return userMapper.getUserAccountFromUser(updatedUser);
    }

    /**
     * Get all user accounts (only for the current OAuth2 provider)
     * @param authentication the current authentication
     * @return the user accounts
     */
    public List<UserAccount> getAllUserAccounts(@NonNull OAuth2AuthenticationToken authentication) {
        var providerName = authentication.getAuthorizedClientRegistrationId();

        var users = userRepository.findAllByProviderName(providerName);
        return users.stream().map(userMapper::getUserAccountFromUser).toList();
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

        var actualUserRole = roleFilter.map(UserAccountService::getProjectRoleFromUserAccountScopeRole).orElse(null);

        var users = userRepository.findAllScopedUsersByProviderName(providerName, projectCodeFilter.orElse(null), actualUserRole);

        var profile = userSessionService.getCurrentUserProfile();
        if (profile.isEmpty()) {
            return new ArrayList<>();
        }

        var userAccounts = users.stream().map(userMapper::getUserAccountFromUser).toList();

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
