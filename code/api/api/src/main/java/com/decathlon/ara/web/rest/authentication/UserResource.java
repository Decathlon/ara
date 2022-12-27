package com.decathlon.ara.web.rest.authentication;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;
import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_CODE_REQUEST_PARAMETER;

@RestController
@RequestMapping(UserResource.PATH)
public class UserResource {

    private final UserAccountService userAccountService;

    static final String PATH = API_PATH + "/user";
    public static final String PATHS = PATH + "/**";

    private static final String DEFAULT_PROJECT = "/default-project";
    private static final String DEFAULT_PROJECT_CODE = DEFAULT_PROJECT + "/" + PROJECT_CODE_REQUEST_PARAMETER;
    public static final String DEFAULT_PROJECT_PATH = PATH + DEFAULT_PROJECT;
    public static final String DEFAULT_PROJECT_CODE_PATH = PATH + DEFAULT_PROJECT_CODE;

    public UserResource(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/details")
    public ResponseEntity<UserAccount> getUserDetails(OAuth2AuthenticationToken authentication) {
        var user = userAccountService.getCurrentUserAccount(authentication);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(DEFAULT_PROJECT)
    public ResponseEntity<UserAccount> clearDefaultProject() {
        try {
            var updatedAccount = userAccountService.clearDefaultProject();
            return ResponseEntity.ok().body(updatedAccount);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @PutMapping(DEFAULT_PROJECT_CODE)
    public ResponseEntity<UserAccount> updateDefaultProject(@PathVariable String projectCode) {
        try {
            var updatedAccount = userAccountService.updateDefaultProject(projectCode);
            return ResponseEntity.ok().body(updatedAccount);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }
}
