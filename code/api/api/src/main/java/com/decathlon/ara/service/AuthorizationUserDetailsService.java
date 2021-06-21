package com.decathlon.ara.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collections;

public class AuthorizationUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUserDetailsService.class);
    /**
     * Loads user from data store and creates UserDetails object based on principal and/or credential.
     *
     * Role name needs to have "ROLE_" prefix.
     *
     * @param token instance of PreAuthenticatedAuthenticationToken
     * @return UserDetails object which contains role information for the given user.
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        final String principal = (String)token.getPrincipal();

        // TODO this is only for illustration purpose. Should retrieve user from data store and determine user roles
        logger.info("request bound to user {}", principal);
        return new User(principal, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

}
