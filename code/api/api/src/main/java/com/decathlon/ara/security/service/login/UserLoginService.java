package com.decathlon.ara.security.service.login;

import com.decathlon.ara.security.dto.authentication.user.AuthenticatedOAuth2User;
import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class UserLoginService<R extends OAuth2UserRequest, U extends OAuth2User, S extends OAuth2UserService<R, U>> {

    private static final Logger LOG = LoggerFactory.getLogger(UserLoginService.class);

    protected final UserAccountService userAccountService;

    protected final UserSessionService userSessionService;

    protected final AuthorityMapper authorityMapper;

    protected UserLoginService(
            UserAccountService userAccountService,
            UserSessionService userSessionService,
            AuthorityMapper authorityMapper
    ) {
        this.userAccountService = userAccountService;
        this.userSessionService = userSessionService;
        this.authorityMapper = authorityMapper;
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

        var authorities = userSessionService.getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName)
                .map(authenticatedUser -> getAuthoritiesFromExistingUser(authenticatedUser).orElseGet(() -> getAuthoritiesAfterCreatingUser(authenticatedUser)))
                .orElse(new HashSet<>());
        return getUpdatedOAuth2User(authorities, oauth2User);
    }

    private Optional<Set<GrantedAuthority>> getAuthoritiesFromExistingUser(AuthenticatedOAuth2User authenticatedOAuth2User) {
        return userAccountService.getCurrentUserAccountFromAuthenticatedOAuth2User(authenticatedOAuth2User)
                .map(authorityMapper::getGrantedAuthoritiesFromUserAccount);
    }

    private Set<GrantedAuthority> getAuthoritiesAfterCreatingUser(AuthenticatedOAuth2User authenticatedOAuth2User) {
        try {
            var createdAccount = userAccountService.createUserAccountFromAuthenticatedOAuth2User(authenticatedOAuth2User);
            return authorityMapper.getGrantedAuthoritiesFromUserAccount(createdAccount);
        } catch (ForbiddenException e) {
            LOG.warn("No user was created... You have restricted access, try to logout and login again.", e);
            return new HashSet<>();
        }
    }

    protected abstract U getUpdatedOAuth2User(Set<GrantedAuthority> authorities, U user);
}
