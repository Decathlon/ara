package com.decathlon.ara.configuration.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class TestAuthentication implements Authentication, OAuth2User {

    private static final long serialVersionUID = 1L;
    
    private String userName;
    private List<SimpleGrantedAuthority> authorities;
    
    public TestAuthentication() {
        this(Collections.emptyList());
    }

    public TestAuthentication(List<SimpleGrantedAuthority> authorities) {
        this("userName", authorities);
    }
    
    public TestAuthentication(String userName, List<SimpleGrantedAuthority> authorities) {
        this.userName = userName;
        this.authorities = authorities;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public <A> A getAttribute(String name) {
        return null;
    }

}