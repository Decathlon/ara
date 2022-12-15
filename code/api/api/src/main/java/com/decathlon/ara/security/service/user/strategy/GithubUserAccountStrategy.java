package com.decathlon.ara.security.service.user.strategy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class GithubUserAccountStrategy implements UserAccountStrategy {

    public static final String LOGIN_FIELD = "login";
    public static final String PICTURE_URL_FIELD = "avatar_url";

    @Override
    public String getLogin(OAuth2User oauth2User) {
        return oauth2User.getAttribute(LOGIN_FIELD);
    }

    @Override
    public Optional<String> getFirstName(OAuth2User oauth2User) {
        return getFullName(oauth2User)
                .filter(StringUtils::isNotBlank)
                .map(this::getSplitFullName)
                .filter(CollectionUtils::isNotEmpty)
                .map(splitName -> splitName.get(0));
    }

    @Override
    public Optional<String> getLastName(OAuth2User oauth2User) {
        return getFullName(oauth2User)
                .filter(StringUtils::isNotBlank)
                .map(this::getSplitFullName)
                .filter(splitName -> splitName.size() >= 2)
                .map(splitName -> splitName.get(splitName.size() - 1));
    }

    private Optional<String> getFullName(OAuth2User oauth2User) {
        return Optional.ofNullable(oauth2User.getAttribute(StandardClaimNames.NAME));
    }

    private List<String> getSplitFullName(String fullName) {
        return Arrays.stream(fullName.split("\s+")).filter(StringUtils::isNotBlank).toList();
    }

    @Override
    public Optional<String> getEmail(OAuth2User oauth2User) {
        return Optional.ofNullable(oauth2User.getAttribute(StandardClaimNames.EMAIL));
    }

    @Override
    public Optional<String> getPictureUrl(OAuth2User oauth2User) {
        return Optional.ofNullable(oauth2User.getAttribute(PICTURE_URL_FIELD));
    }
}
