package com.decathlon.ara.security.service.user.strategy;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GoogleUserAccountStrategy implements UserAccountStrategy {

    @Override
    public String getLogin(OAuth2User oauth2User) {
        return ((OidcUser) oauth2User).getEmail();
    }

    @Override
    public Optional<String> getFirstName(OAuth2User oauth2User) {
        return Optional.ofNullable(((OidcUser) oauth2User).getGivenName());
    }

    @Override
    public Optional<String> getLastName(OAuth2User oauth2User) {
        return Optional.ofNullable(((OidcUser) oauth2User).getFamilyName());
    }

    @Override
    public Optional<String> getEmail(OAuth2User oauth2User) {
        return Optional.ofNullable(((OidcUser) oauth2User).getEmail());
    }

    @Override
    public Optional<String> getPictureUrl(OAuth2User oauth2User) {
        return Optional.ofNullable(((OidcUser) oauth2User).getPicture());
    }
}
