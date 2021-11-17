package com.decathlon.ara.web.rest.authentication;


import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

import java.util.Optional;

import com.decathlon.ara.service.dto.authentication.AuthenticationUserDetailsDTO;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(UserResource.PATH)
@RequiredArgsConstructor
public class UserResource {

    static final String PATH = API_PATH + "/user";

    @GetMapping("/details")
    public AuthenticationUserDetailsDTO getUserDetails(
            @AuthenticationPrincipal OidcUser user,
            @AuthenticationPrincipal OAuth2User userOauth2
    ){
        if (user != null) {
            return new AuthenticationUserDetailsDTO(
                user.getSubject(),
                user.getFullName(),
                user.getName(),
                user.getEmail(),
                user.getPicture());
        }

        if (userOauth2 != null) {
            return new AuthenticationUserDetailsDTO(
                safeStringAttribute(userOauth2, "id"),
                safeStringAttribute(userOauth2, "name"),
                safeStringAttribute(userOauth2, "login"),
                safeStringAttribute(userOauth2, "email"),
                safeStringAttribute(userOauth2, "avatar_url"));
        }
        return new AuthenticationUserDetailsDTO();
    }

    private String safeStringAttribute(OAuth2User userOauth2, String attribute) {
        return Optional.ofNullable(userOauth2.getAttribute(attribute)).orElse("").toString();
    }

}
