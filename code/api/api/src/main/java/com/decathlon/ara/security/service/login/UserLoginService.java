package com.decathlon.ara.security.service.login;

import com.decathlon.ara.security.service.user.UserAccountService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Set;

public abstract class UserLoginService<R extends OAuth2UserRequest, U extends OAuth2User, S extends OAuth2UserService<R, U>> {

    protected final UserAccountService userAccountService;

    protected UserLoginService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    protected abstract S getUserService();

    /**
     * Manage the user login request, i.e.:
     * - if not found, a new user account is created
     * - otherwise, the permissions are fetched from the previously saved user account
     * @param request the login request
     * @return the updated user (containing authorities, matching its profile and roles)
     */
    public U manageUserLoginRequest(R request) {
        S userService = getUserService();
        U oauth2User = userService.loadUser(request);
        var providerName = request.getClientRegistration().getRegistrationId();
        var savedUserAccount = userAccountService
                .getCurrentUserAccount(oauth2User, providerName)
                .orElseGet(() -> userAccountService.createUserAccount(oauth2User, providerName));
        var authorities = savedUserAccount.getMatchingAuthorities();
        return getUpdatedOAuth2User(authorities, oauth2User);
    }

    protected abstract U getUpdatedOAuth2User(Set<GrantedAuthority> authorities, U user);
}
