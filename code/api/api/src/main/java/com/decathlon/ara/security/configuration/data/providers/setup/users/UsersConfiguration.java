package com.decathlon.ara.security.configuration.data.providers.setup.users;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

public class UsersConfiguration {

    @Value("${create-new-project-on-init:false}")
    private boolean createNewProjectOnInit;

    private List<UserProfileConfiguration> profiles;

    private Map<String, String> customAttributes;

    public boolean getCreateNewProjectOnInit() {
        return createNewProjectOnInit;
    }

    public List<UserProfileConfiguration> getProfiles() {
        return profiles;
    }

    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    public void setCreateNewProjectOnInit(boolean createNewProjectOnInit) {
        this.createNewProjectOnInit = createNewProjectOnInit;
    }

    public void setProfiles(List<UserProfileConfiguration> profiles) {
        this.profiles = profiles;
    }

    public void setCustomAttributes(Map<String, String> customAttributes) {
        this.customAttributes = customAttributes;
    }
}
