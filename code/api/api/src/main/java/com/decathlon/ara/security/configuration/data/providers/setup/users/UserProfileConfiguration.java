package com.decathlon.ara.security.configuration.data.providers.setup.users;

import java.util.List;

public class UserProfileConfiguration {

    private String login;

    private String profile;

    private List<UserScopeConfiguration> scopes;

    public String getLogin() {
        return login;
    }

    public String getProfile() {
        return profile;
    }

    public List<UserScopeConfiguration> getScopes() {
        return scopes;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setScopes(List<UserScopeConfiguration> scopes) {
        this.scopes = scopes;
    }
}
