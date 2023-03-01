package com.decathlon.ara.security.service.member.user.account;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.domain.security.member.user.account.UserProfile;
import com.decathlon.ara.domain.security.member.user.account.UserProjectScope;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.account.UserProjectScopeRepository;
import com.decathlon.ara.repository.security.member.user.account.UserRepository;
import com.decathlon.ara.security.configuration.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.dto.authentication.user.AuthenticatedOAuth2User;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.mapper.UserMapper;
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

import static com.decathlon.ara.Entities.PROJECT;
import static com.decathlon.ara.Entities.USER;
import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;

@Service
public class UserAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountService.class);

    private final UserRepository userRepository;

    private final UserProjectScopeRepository userProjectScopeRepository;

    private final ProjectRepository projectRepository;

    private final OAuth2ProvidersConfiguration providersConfiguration;

    private final UserMapper userMapper;

    private final ProjectMapper projectMapper;

    private final ProjectService projectService;

    private final UserSessionService userSessionService;

    public UserAccountService(
            UserRepository userRepository,
            UserProjectScopeRepository userProjectScopeRepository,
            ProjectRepository projectRepository,
            OAuth2ProvidersConfiguration providersConfiguration,
            UserMapper userMapper,
            ProjectMapper projectMapper,
            ProjectService projectService,
            UserSessionService userSessionService
    ) {
        this.userRepository = userRepository;
        this.userProjectScopeRepository = userProjectScopeRepository;
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
        return getCurrentUser().map(userMapper::getFullScopeAccessUserAccountFromUser);
    }

    /**
     * Fetch the currently logged-in {@link UserAccount} from the {@link OAuth2AuthenticationToken}, if found
     * @param authentication the (OAuth2) authentication
     * @return the current {@link UserAccount}
     */
    public Optional<UserAccount> getCurrentUserAccountFromAuthentication(@NonNull OAuth2AuthenticationToken authentication) {
        return userSessionService.getCurrentAuthenticatedOAuth2UserFromAuthentication(authentication)
                .flatMap(authenticatedUser -> getUserFromProviderNameAndUserLogin(authenticatedUser.getProviderName(), authenticatedUser.getLogin()))
                .map(userMapper::getFullScopeAccessUserAccountFromUser);
    }

    /**
     * Fetch the currently logged-in {@link UserAccount} from the {@link AuthenticatedOAuth2User}, if found
     * @param authenticatedUser the authenticated user
     * @return the current {@link UserAccount}
     */
    public Optional<UserAccount> getCurrentUserAccountFromAuthenticatedOAuth2User(@NonNull AuthenticatedOAuth2User authenticatedUser) {
        return getUserFromProviderNameAndUserLogin(authenticatedUser.getProviderName(), authenticatedUser.getLogin()).map(userMapper::getFullScopeAccessUserAccountFromUser);
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
     * Get a {@link User} from its login (matching the current user provider name)
     * @param userLogin the user login
     * @return the {@link User}, if found
     */
    public Optional<User> getUserFromLogin(@NonNull String userLogin) {
        if (StringUtils.isBlank(userLogin)) {
            return Optional.empty();
        }

        return userSessionService.getCurrentAuthenticatedOAuth2User()
                .map(AuthenticatedOAuth2User::getProviderName)
                .flatMap(providerName -> getUserFromProviderNameAndUserLogin(providerName, userLogin));
    }

    /**
     * Create a new {@link UserAccount} from an {@link AuthenticatedOAuth2User}
     * @param authenticatedUser the authenticated user
     * @return the created {@link UserAccount}
     * @throws ForbiddenException thrown if the user couldn't be created
     */
    public UserAccount createUserAccountFromAuthenticatedOAuth2User(@NonNull AuthenticatedOAuth2User authenticatedUser) throws ForbiddenException {
        var exception = new ForbiddenException(USER, "create new user");

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

        var userScopes = profileConfiguration.map(configuration -> getUserProjectRolesFromConfiguration(configuration, providerName, savedUser));
        var savedScopes = userScopes.map(userProjectScopeRepository::saveAll).map(HashSet::new).orElse(new HashSet<>());
        savedUser.setScopes(savedScopes);
        return userMapper.getFullScopeAccessUserAccountFromUser(savedUser);
    }

    private static Optional<UserProfile> getUserProfileFromConfiguration(UserProfileConfiguration profileConfiguration) {
        var profileAsString = profileConfiguration.getProfile();
        return UserAccountProfile.getProfileFromString(profileAsString).map(UserAccountService::getUserProfileFromUserAccountProfile);
    }

    private List<UserProjectScope> getUserProjectRolesFromConfiguration(UserProfileConfiguration profileConfiguration, String providerName, User savedUser) {
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
                                .map(project -> new UserProjectScope(savedUser, project, entry.getKey().get()))
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
        var exception = new ForbiddenException(PROJECT, "fetch user projects");

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
            var scopedProjectsStream = currentUser.getScopes().stream().map(UserProjectScope::getProject);
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
        var exception = new ForbiddenException(PROJECT, "remove current user project scope", projectCodeContext);

        var currentUser = getCurrentUser().orElseThrow(() -> exception);
        removeUserScope(currentUser, projectCode, exception);
        userSessionService.refreshCurrentUserAuthorities();
    }

    private void removeUserScope(User targetUser, String projectCode, ForbiddenException exception) throws ForbiddenException {
        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);
        userProjectScopeRepository.deleteById(new UserProjectScope.UserProjectScopeId(targetUser, targetProject));
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
        var exception = new ForbiddenException(PROJECT, "remove user project scope", projectCodeContext);

        var targetUser = getUserFromLogin(userLogin).orElseThrow(() -> exception);
        removeUserScope(targetUser, projectCode, exception);
    }

    /**
     * Update the current user scope.
     * If the project is already in the scope, then its role is updated
     * Otherwise, a new scope is added containing the project and the role
     * @param projectCode the project code
     * @param accountRole the new role
     * @return the updated {@link UserAccount}
     * @throws ForbiddenException thrown if this operation failed
     */
    public UserAccount updateCurrentUserProjectScope(@NonNull String projectCode, @NonNull UserAccountScopeRole accountRole) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(PROJECT, "update current user project scope", projectCodeContext);

        var currentUser = getCurrentUser().orElseThrow(() -> exception);
        var updatedUser = updateUserProjectScope(currentUser, projectCode, accountRole, exception);
        
        userSessionService.refreshCurrentUserAuthorities();
        return userMapper.getFullScopeAccessUserAccountFromUser(updatedUser);
    }

    private User updateUserProjectScope(User targetUser, String projectCode, UserAccountScopeRole targetAccountRole, ForbiddenException exception) throws ForbiddenException {
        var targetProject = getProjectFromCode(projectCode).orElseThrow(() -> exception);
        var targetRole = getProjectRoleFromUserAccountScopeRole(targetAccountRole);
        updateScopedUserProjectRole(targetUser, targetProject, targetRole);
        return userRepository.save(targetUser);
    }

    private static ProjectRole getProjectRoleFromUserAccountScopeRole(@NonNull UserAccountScopeRole userAccountScopeRole) {
        return ProjectRole.valueOf(userAccountScopeRole.name());
    }

    private void updateScopedUserProjectRole(User targetUser, Project targetProject, ProjectRole targetRole) {
        var targetProjectCode = targetProject.getCode();
        var userScopes = targetUser.getScopes();
        for (var userScope : userScopes) {
            var projectInScope = userScope.getProject();
            var projectCodeInScope = projectInScope.getCode();
            if (targetProjectCode.equals(projectCodeInScope)) {
                userScope.setRole(targetRole);
            }
        }
        var targetProjectScopeNotFound = userScopes.stream()
                .map(UserProjectScope::getProject)
                .map(Project::getCode)
                .noneMatch(targetProjectCode::equals);
        if (targetProjectScopeNotFound) {
            userScopes.add(new UserProjectScope(targetUser, targetProject, targetRole));
        }
    }

    /**
     * Update a user scope.
     * If the project is already in the scope, then its role is updated
     * Otherwise, a new scope is added containing the project and the role
     * @param userLogin the user login
     * @param projectCode the project code
     * @param accountRole the new role
     * @return the updated {@link UserAccount}
     * @throws ForbiddenException thrown if this operation failed
     */
    public UserAccount updateUserProjectScope(@NonNull String userLogin, @NonNull String projectCode, @NonNull UserAccountScopeRole accountRole) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(PROJECT, "update user project scope", projectCodeContext);

        var currentUser = getCurrentUser().orElseThrow(() -> exception);
        var targetUser = getUserFromLogin(userLogin).orElseThrow(() -> exception);
        var updatedUser = updateUserProjectScope(targetUser, projectCode, accountRole, exception);
        return userMapper.getPartialScopeAccessUserAccountFromUser(updatedUser, currentUser);
    }

    /**
     * Update the user profile.
     * @param userLogin the user login
     * @param profile the new profile
     * @return the updated {@link UserAccount}
     * @throws ForbiddenException thrown if this operation failed
     */
    public UserAccount updateUserProfile(@NonNull String userLogin, @NonNull UserAccountProfile profile) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(userLogin);
        var exception = new ForbiddenException(USER, "update user profile", projectCodeContext);

        var targetUser = getUserFromLogin(userLogin).orElseThrow(() -> exception);
        var targetProfile = getUserProfileFromUserAccountProfile(profile);
        targetUser.setProfile(targetProfile);
        var updatedUser = userRepository.save(targetUser);
        return userMapper.getFullScopeAccessUserAccountFromUser(updatedUser);
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
        var userToUpdate = getCurrentUser().orElseThrow(() -> new ForbiddenException(PROJECT, "clear default project"));
        userToUpdate.setDefaultProject(null);
        var updatedUser = userRepository.save(userToUpdate);
        return userMapper.getFullScopeAccessUserAccountFromUser(updatedUser);
    }

    /**
     * Update the user default project
     * @param projectCode the project code. Note that the code must exist and the user must have access to this project.
     * @return the updated user account
     * @throws ForbiddenException thrown if the operation failed
     */
    public UserAccount updateDefaultProject(@NonNull String projectCode) throws ForbiddenException {
        var projectCodeContext = getProjectCodeExceptionContext(projectCode);
        var exception = new ForbiddenException(PROJECT, "update default project", projectCodeContext);

        if (StringUtils.isBlank(projectCode)) {
            throw exception;
        }

        var defaultProject = projectRepository.findByCode(projectCode).orElseThrow(() -> exception);
        var userToUpdate = getCurrentUser().orElseThrow(() -> exception);
        userToUpdate.setDefaultProject(defaultProject);
        var updatedUser = userRepository.save(userToUpdate);
        return userMapper.getFullScopeAccessUserAccountFromUser(updatedUser);
    }

    /**
     * Get all user accounts (only for the current OAuth2 provider)
     * @param authentication the current {@link OAuth2AuthenticationToken}
     * @return the user accounts
     */
    public List<UserAccount> getAllUserAccounts(@NonNull OAuth2AuthenticationToken authentication) {
        var providerName = authentication.getAuthorizedClientRegistrationId();

        var users = userRepository.findAllByProviderName(providerName);
        return users.stream().map(userMapper::getFullScopeAccessUserAccountFromUser).toList();
    }

    /**
     * Get all the scoped user accounts (only for the current OAuth2 provider).
     * Note that account scopes contain only projects shared by the current user.
     * @param roleFilter if present, filter on scoped users having at least this role on a project
     * @return the user accounts
     */
    public List<UserAccount> getAllScopedUserAccounts(Optional<UserAccountScopeRole> roleFilter) throws ForbiddenException {
        return getAllScopedUserAccounts(Optional.empty(), roleFilter);
    }

    private List<UserAccount> getAllScopedUserAccounts(Optional<String> projectCodeFilter, Optional<UserAccountScopeRole> roleFilter) throws ForbiddenException {
        var exception = new ForbiddenException(USER, "fetch scoped users");

        var currentUser = getCurrentUser().orElseThrow(() -> exception);
        var providerName = currentUser.getProviderName();

        var actualUserRole = roleFilter.map(UserAccountService::getProjectRoleFromUserAccountScopeRole).orElse(null);

        var users = userRepository.findAllScopedUsersByProviderName(providerName, projectCodeFilter.orElse(null), actualUserRole);
        return users.stream().map(targetUser -> userMapper.getPartialScopeAccessUserAccountFromUser(targetUser, currentUser)).toList();
    }

    /**
     * Get all the scoped user accounts having access to a given project (only for the current OAuth2 provider).
     * Note that account scopes contain only projects shared by the current user.
     * @param projectCode the project code
     * @param roleFilter if present, filter only on scoped users having this role on the project 'projectCode'
     * @return the user accounts
     */
    public List<UserAccount> getAllScopedUserAccountsOnProject(String projectCode, Optional<UserAccountScopeRole> roleFilter) throws ForbiddenException {
        return getAllScopedUserAccounts(Optional.of(projectCode), roleFilter);
    }
}
