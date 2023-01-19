package com.decathlon.ara.security.dto.authentication.user;

import java.util.Optional;

/**
 * The currently logged-in user.
 * Contains the user details found in the {@link org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken}
 */
public class AuthenticatedOAuth2User {

    private String providerName;

    private String login;

    private String firstName;

    private String lastName;

    private String email;

    private String pictureUrl;

    public AuthenticatedOAuth2User(String providerName, String login) {
        this.providerName = providerName;
        this.login = login;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getLogin() {
        return login;
    }

    public Optional<String> getFirstName() {
        return Optional.ofNullable(firstName);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Optional<String> getLastName() {
        return Optional.ofNullable(lastName);
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Optional<String> getPictureUrl() {
        return Optional.ofNullable(pictureUrl);
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
