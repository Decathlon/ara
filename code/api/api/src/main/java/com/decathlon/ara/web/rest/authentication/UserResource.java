package com.decathlon.ara.web.rest.authentication;

import com.decathlon.ara.security.dto.user.AuthenticationUserDetailsDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

@RestController
@RequestMapping(UserResource.PATH)
public class UserResource {

    static final String PATH = API_PATH + "/user";

    @GetMapping("/details")
    public AuthenticationUserDetailsDTO getUserDetails(
                                                       @AuthenticationPrincipal OidcUser oidcUser,
                                                       @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oidcUser != null) {
            return new AuthenticationUserDetailsDTO(
                    oidcUser.getSubject(),
                    oidcUser.getFullName(),
                    oidcUser.getName(),
                    oidcUser.getEmail(),
                    oidcUser.getPicture());
        }

        if (oauth2User != null) {
            return new AuthenticationUserDetailsDTO(
                    safeStringAttribute(oauth2User, "id"),
                    safeStringAttribute(oauth2User, "name"),
                    safeStringAttribute(oauth2User, "login"),
                    safeStringAttribute(oauth2User, "email"),
                    safeStringAttribute(oauth2User, "avatar_url"));
        }
        return new AuthenticationUserDetailsDTO();
    }

    private String safeStringAttribute(OAuth2User oauth2User, String attribute) {
        return Optional.ofNullable(oauth2User.getAttribute(attribute)).orElse("").toString();
    }

}
