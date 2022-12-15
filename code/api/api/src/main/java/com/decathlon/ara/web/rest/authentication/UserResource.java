package com.decathlon.ara.web.rest.authentication;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.service.user.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

@RestController
@RequestMapping(UserResource.PATH)
public class UserResource {

    private final UserAccountService userAccountService;

    static final String PATH = API_PATH + "/user";
    public static final String PATHS = PATH + "/**";

    public UserResource(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/details")
    public ResponseEntity<UserAccount> getUserDetails(OAuth2AuthenticationToken authentication) {
        var user = userAccountService.getCurrentUserAccount(authentication);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
    }

}
