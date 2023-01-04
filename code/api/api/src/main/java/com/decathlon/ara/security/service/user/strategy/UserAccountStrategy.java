package com.decathlon.ara.security.service.user.strategy;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

public interface UserAccountStrategy {

    String getLogin(OAuth2User oauth2User);

    Optional<String> getFirstName(OAuth2User oauth2User);

    Optional<String> getLastName(OAuth2User oauth2User);

    Optional<String> getEmail(OAuth2User oauth2User);

    Optional<String> getPictureUrl(OAuth2User oauth2User);
}
