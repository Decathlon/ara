package com.decathlon.ara.web.rest.authentication;


import com.decathlon.ara.service.dto.authentication.AuthenticationUserDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

@RestController
@RequestMapping(UserResource.PATH)
@RequiredArgsConstructor
public class UserResource {

    static final String PATH = API_PATH + "/user";

    @GetMapping("/details")
    public AuthenticationUserDetailsDTO getUserDetails(
            @AuthenticationPrincipal OidcUser user
    ){

        return new AuthenticationUserDetailsDTO(
                user.getSubject(),
                user.getFullName(),
                user.getName(),
                user.getEmail(),
                user.getPicture());
    }

}
