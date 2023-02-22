package com.decathlon.ara.security.configuration.providers.setup;

import com.decathlon.ara.security.configuration.providers.setup.provider.ProviderConfiguration;
import com.decathlon.ara.security.configuration.providers.setup.users.UsersConfiguration;

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

}
