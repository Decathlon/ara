package com.decathlon.ara.security.dto.user;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public enum UserAccountProfile {

    SUPER_ADMIN,
    AUDITOR,
    SCOPED_USER;

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountProfile.class);

    /**
     * Get the profile matching a string, when found
     * @param profileAsString the profile as string
     * @return the matching user profile
     */
    public static Optional<UserAccountProfile> getProfileFromString(String profileAsString) {
        if (StringUtils.isBlank(profileAsString)) {
            return Optional.empty();
        }

        try {
            return Optional.of(UserAccountProfile.valueOf(profileAsString.toUpperCase()));
        } catch (IllegalArgumentException exception) {
            LOG.warn("The user profile \"{}\" doesn't exist!", profileAsString);
            return Optional.empty();
        }
    }
}
