package com.decathlon.ara.security.configuration.providers;

import com.decathlon.ara.security.configuration.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.configuration.providers.setup.users.UserProfileConfiguration;
import com.decathlon.ara.security.configuration.providers.setup.users.UsersConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Stream;

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

    /**
     * Get {@link UserProfileConfiguration} if any matches the providerName and userLogin
     * @param providerName the provider name
     * @param userLogin the user login
     * @return the {@link UserProfileConfiguration}, if found
     */
    public Optional<UserProfileConfiguration> getUserProfileConfiguration(String providerName, String userLogin) {
        if (StringUtils.isBlank(userLogin)) {
            return Optional.empty();
        }

        return getUsersConfigurationStreamFromProviderName(providerName)
                .map(UsersConfiguration::getProfiles)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(profile -> userLogin.equals(profile.getLogin()))
                .findFirst();
    }

    private Stream<UsersConfiguration> getUsersConfigurationStreamFromProviderName(String providerName) {
        if (StringUtils.isBlank(providerName) || CollectionUtils.isEmpty(setup)) {
            return Stream.empty();
        }

        return setup.stream()
                .filter(configuration -> Objects.nonNull(configuration.getProvider()))
                .filter(configuration -> providerName.equalsIgnoreCase(configuration.getProvider().getRegistration()))
                .map(ProviderSetupConfiguration::getUsers)
                .filter(Objects::nonNull);
    }

    /**
     * Tell whether unknown projects can be created when creating new users
     * @param providerName the provider name for which this option applies
     * @return true iff unknown projects are created at user init
     */
    public boolean createNewProjectsIfNotFoundAtUsersInit(String providerName) {
        return getUsersConfigurationStreamFromProviderName(providerName)
                .map(UsersConfiguration::getCreateNewProjectOnInit)
                .findFirst()
                .orElse(false);
    }
}
