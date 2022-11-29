package com.decathlon.ara.web.rest.authentication;

import com.decathlon.ara.security.dto.user.LoggedInUserDTO;
import com.decathlon.ara.security.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

@RestController
@RequestMapping(UserResource.PATH)
public class UserResource {

    private final UserService userService;

    static final String PATH = API_PATH + "/user";
    public static final String PATHS = PATH + "/**";

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/details")
    public ResponseEntity<LoggedInUserDTO> getUserDetails(OAuth2AuthenticationToken authentication) {
        var user = userService.getLoggedInUserDTO(authentication);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
    }

}
