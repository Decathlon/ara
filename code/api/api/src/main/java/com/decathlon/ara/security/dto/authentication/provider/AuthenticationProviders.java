package com.decathlon.ara.security.dto.authentication.provider;

import java.util.List;

public class AuthenticationProviders {

    private List<AuthenticationProvider> providers;

    private String loginUrl;

    private String logoutUrl;

    public AuthenticationProviders(List<AuthenticationProvider> providers, String loginUrl, String logoutUrl) {
        this.providers = providers;
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
    }

    public List<AuthenticationProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<AuthenticationProvider> providers) {
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
