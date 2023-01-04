package com.decathlon.ara.web.rest.authentication;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;
import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_CODE_REQUEST_PARAMETER;

@RestController
@RequestMapping(UserResource.PATH)
public class UserResource {

    private final UserAccountService userAccountService;

    static final String PATH = API_PATH + "/user";

    private static final String ACCOUNTS = "/accounts";
    private static final String CURRENT_ACCOUNT = ACCOUNTS + "/current";
    public static final String CURRENT_ACCOUNT_PATH = PATH + CURRENT_ACCOUNT;
    private static final String ALL_ACCOUNTS = ACCOUNTS + "/all";
    public static final String ALL_ACCOUNTS_PATH = PATH + ALL_ACCOUNTS;
    private static final String SCOPED_ACCOUNTS = ACCOUNTS + "/scoped";
    public static final String SCOPED_ACCOUNTS_PATH = PATH + SCOPED_ACCOUNTS;
    private static final String SCOPED_ACCOUNTS_BY_PROJECT = SCOPED_ACCOUNTS + "/project/" + PROJECT_CODE_REQUEST_PARAMETER;
    public static final String SCOPED_ACCOUNTS_BY_PROJECT_PATH = PATH + SCOPED_ACCOUNTS_BY_PROJECT;

    private static final String DEFAULT_PROJECT = CURRENT_ACCOUNT + "/default-project";
    private static final String UPDATE_DEFAULT_PROJECT_BY_CODE = DEFAULT_PROJECT + "/" + PROJECT_CODE_REQUEST_PARAMETER;
    public static final String CLEAR_DEFAULT_PROJECT_PATH = PATH + DEFAULT_PROJECT;
    public static final String UPDATE_DEFAULT_PROJECT_BY_CODE_PATH = PATH + UPDATE_DEFAULT_PROJECT_BY_CODE;

    public UserResource(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping(CURRENT_ACCOUNT)
    public ResponseEntity<UserAccount> getCurrentUserAccount(OAuth2AuthenticationToken authentication) {
        var user = userAccountService.getCurrentUserAccount(authentication);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping(ALL_ACCOUNTS)
    public ResponseEntity<List<UserAccount>> getAllUserAccounts(OAuth2AuthenticationToken authentication) {
        var accounts = userAccountService.getAllUserAccounts(authentication);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping(SCOPED_ACCOUNTS)
    public ResponseEntity<List<UserAccount>> getScopedAccounts(
            OAuth2AuthenticationToken authentication,
            @RequestParam(value = "role", required = false) String roleAsString
    ) {
        var roleOptional = UserAccountScopeRole.getScopeFromString(roleAsString);
        var accounts = userAccountService.getAllScopedUserAccounts(authentication, roleOptional);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping(SCOPED_ACCOUNTS_BY_PROJECT)
    public ResponseEntity<List<UserAccount>> getProjectScopedAccounts(
            OAuth2AuthenticationToken authentication,
            @PathVariable String projectCode,
            @RequestParam(value = "role", required = false) String roleAsString
    ) {
        var roleOptional = UserAccountScopeRole.getScopeFromString(roleAsString);
        var accounts = userAccountService.getAllScopedUserAccountsOnProject(authentication, projectCode, roleOptional);
        return ResponseEntity.ok(accounts);
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

    @PutMapping(UPDATE_DEFAULT_PROJECT_BY_CODE)
    public ResponseEntity<UserAccount> updateDefaultProject(@PathVariable String projectCode) {
        try {
            var updatedAccount = userAccountService.updateDefaultProject(projectCode);
            return ResponseEntity.ok().body(updatedAccount);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }
}
