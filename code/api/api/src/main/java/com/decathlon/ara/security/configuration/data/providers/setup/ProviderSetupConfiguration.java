package com.decathlon.ara.security.configuration.data.providers.setup;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.security.configuration.data.providers.setup.provider.ProviderConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UsersConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ProviderSetupConfiguration {

    private ProviderConfiguration provider;

    private UsersConfiguration users;

    public ProviderConfiguration getProvider() {
        return provider;
    }

    public UsersConfiguration getUsers() {
        return users;
    }

    public void setProvider(ProviderConfiguration provider) {
        this.provider = provider;
    }

    public void setUsers(UsersConfiguration users) {
        this.users = users;
    }

    /**
     * Search for a configuration matching an OAuth2 user login.
     * If found, a matching user entity is returned
     * @param login the user login
     * @param projectRepository the project repository
     * @return the matching user entity, when found
     */
    public Optional<UserEntity> getMatchingUserEntityFromLogin(String login, ProjectRepository projectRepository) {
        if (StringUtils.isBlank(login)) {
            return Optional.empty();
        }

        var noUserProfileFound = (users == null) || CollectionUtils.isEmpty(users.getProfiles());
        if (noUserProfileFound) {
            return Optional.empty();
        }

        var noProviderFound = provider == null || StringUtils.isBlank(provider.getRegistration());
        if (noProviderFound) {
            return Optional.empty();
        }

        return users.getProfiles()
                .stream()
                .filter(profile -> login.equals(profile.getLogin()))
                .map(createUserEntityFromUserProfileConfiguration(login, projectRepository))
                .findFirst();
    }

    private Function<UserProfileConfiguration, UserEntity> createUserEntityFromUserProfileConfiguration(String login, ProjectRepository projectRepository) {
        return profile -> {
            var userProfile = UserEntity.UserEntityProfile.getProfileFromString(profile.getProfile()).orElse(UserEntity.UserEntityProfile.SCOPED_USER);
            var user = new UserEntity(login, provider.getRegistration(), userProfile);

            var thereIsAtLeastOneRole = !CollectionUtils.isEmpty(profile.getScopes());
            var userProfileIsScopedUser = UserEntity.UserEntityProfile.SCOPED_USER.equals(userProfile);
            var searchForRoles = userProfileIsScopedUser && thereIsAtLeastOneRole;
            var roles = searchForRoles ?
                    profile.getScopes()
                            .stream()
                            .filter(scope -> !CollectionUtils.isEmpty(scope.getProjects()))
                            .flatMap(scope -> scope.getProjects()
                                    .stream()
                                    .distinct()
                                    .map(projectCode -> Pair.of(
                                            projectCode,
                                            UserEntityRoleOnProject.ScopedUserRoleOnProject.getScopeFromString(scope.getScope())
                                            .orElse(UserEntityRoleOnProject.ScopedUserRoleOnProject.MEMBER)
                                            )
                                    )
                            )
                            .collect(Collectors.groupingBy(Pair::getFirst, Collectors.mapping(Pair::getSecond, Collectors.toList())))
                            .entrySet()
                            .stream()
                            .map(createRoleOnProjectFromProjectCodeAndRolesEntry(user, projectRepository))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList() :
                    new ArrayList<UserEntityRoleOnProject>();
            user.setRolesOnProjectWhenScopedUser(roles);
            return user;
        };
    }

    private Function<Map.Entry<String, List<UserEntityRoleOnProject.ScopedUserRoleOnProject>>, Optional<UserEntityRoleOnProject>> createRoleOnProjectFromProjectCodeAndRolesEntry(UserEntity user, ProjectRepository projectRepository) {
        return entry -> {
            var projectCode = entry.getKey();
            var scopes = entry.getValue();

            var project = getProjectFromCode(projectCode, projectRepository);
            if (project.isEmpty()) {
                return Optional.empty();
            }

            var highestRole = scopes.stream()
                    .distinct()
                    .min(Comparator.comparing(Enum::ordinal));
            return highestRole.map(role -> new UserEntityRoleOnProject(user, project.get(), role));
        };
    }

    private Optional<Project> getProjectFromCode(String projectCode, ProjectRepository projectRepository) {
        var fetchedProject = projectRepository.findByCode(projectCode);
        if (fetchedProject.isPresent()) {
            return fetchedProject;
        }

        var thisProviderRequiresToCreateNotFoundProjectOnInit = users.getCreateNewProjectOnInit();
        if (thisProviderRequiresToCreateNotFoundProjectOnInit) {
            var newlySavedProject = createNewProjectFromCode(projectCode, projectRepository);
            return Optional.of(newlySavedProject);
        }

        return Optional.empty();
    }

    private Project createNewProjectFromCode(String projectCode, ProjectRepository projectRepository) {
        String projectName = getProjectNameFromCode(projectCode);
        return projectRepository.save(new Project(projectCode, projectName));
    }

    private static String getProjectNameFromCode(String projectCode) {
        UnaryOperator<String> capitalizeFirstLetter = string -> Character.toUpperCase(string.charAt(0)) + string.substring(1);
        var projectNameFromCode = Arrays.stream(projectCode.split("[^a-zA-Z0-9]+"))
                .map(capitalizeFirstLetter)
                .collect(Collectors.joining(" "));
        return String.format("%s (generated)", projectNameFromCode);
    }
}
