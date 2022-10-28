package com.decathlon.ara.security.configuration.data.providers.setup.users;

import java.util.List;

public class UserScopeConfiguration {

    private String scope;

    private List<String> projects;

    public String getScope() {
        return scope;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }
}
