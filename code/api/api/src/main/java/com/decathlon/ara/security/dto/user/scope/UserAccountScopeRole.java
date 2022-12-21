package com.decathlon.ara.security.dto.user.scope;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public enum UserAccountScopeRole {
    ADMIN,
    MAINTAINER,
    MEMBER;

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountScopeRole.class);

    /**
     * Get a scope from string, when found
     * @param scopeAsString the scope represented as string
     * @return the scope from string
     */
    public static Optional<UserAccountScopeRole> getScopeFromString(String scopeAsString) {
        if (StringUtils.isBlank(scopeAsString)) {
            return Optional.empty();
        }

        try {
            return Optional.of(UserAccountScopeRole.valueOf(scopeAsString.toUpperCase()));
        } catch (IllegalArgumentException exception) {
            LOG.warn("The user scope \"{}\" doesn't exist!", scopeAsString);
            return Optional.empty();
        }
    }

}
