package com.decathlon.ara.security.configuration.data.providers;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.security.configuration.data.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.users.UsersConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Configuration
@ConfigurationProperties("oauth2providers")
public class OAuth2ProvidersConfiguration {

    private List<ProviderSetupConfiguration> setup;

    public List<ProviderSetupConfiguration> getSetup() {
        return setup;
    }

    public void setSetup(List<ProviderSetupConfiguration> setup) {
        this.setup = setup;
    }

    /**
     * Search for a configuration matching an OAuth2 provider name and a user login.
     * If found, a matching user entity is returned
     * @param providerName the provider name
     * @param login the user login
     * @param projectRepository the project repository
     * @return the matching user entity, when found
     */
    public Optional<UserEntity> getMatchingUserEntityFromProviderNameAndLogin(String providerName, String login, ProjectRepository projectRepository) {
        if (StringUtils.isBlank(providerName)) {
            return Optional.empty();
        }

        if (StringUtils.isBlank(login)) {
            return Optional.empty();
        }

        return CollectionUtils.isEmpty(setup) ?
                Optional.empty() :
                setup.stream()
                        .filter(configuration -> Objects.nonNull(configuration.getProvider()))
                        .filter(configuration -> providerName.equalsIgnoreCase(configuration.getProvider().getRegistration()))
                        .map(configuration -> configuration.getMatchingUserEntityFromLogin(login, projectRepository))
                        .filter(Optional::isPresent)
                        .findFirst()
                        .orElse(Optional.empty());
    }

    /**
     * Get all non-standard attributes for a given provider
     * @param providerName the provider name
     * @return the map matching the standard attributes to non-standard ones
     */
    public Map<String, String> getUsersCustomAttributesFromProviderName(String providerName) {
        if (StringUtils.isBlank(providerName)) {
            return new HashMap<>();
        }

        if (CollectionUtils.isEmpty(setup)) {
            return new HashMap<>();
        }

        return setup.stream()
                .filter(configuration -> Objects.nonNull(configuration.getProvider()))
                .filter(configuration -> providerName.equalsIgnoreCase(configuration.getProvider().getRegistration()))
                .map(ProviderSetupConfiguration::getUsers)
                .filter(Objects::nonNull)
                .map(UsersConfiguration::getCustomAttributes)
                .filter(Objects::nonNull) 
                .findFirst()
                .orElse(new HashMap<>());
    }
}
