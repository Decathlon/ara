package com.decathlon.ara.security.dto.provider;

import java.util.List;

public class AuthenticationProvidersDTO {

    private List<AuthenticationProviderDTO> providers;

    private String loginUrl;

    private String logoutUrl;

    public AuthenticationProvidersDTO(List<AuthenticationProviderDTO> providers, String loginUrl, String logoutUrl) {
        this.providers = providers;
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
    }

    public List<AuthenticationProviderDTO> getProviders() {
        return providers;
    }

    public void setProviders(List<AuthenticationProviderDTO> providers) {
        this.providers = providers;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
}
