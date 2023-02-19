package com.decathlon.ara.security.service;

import com.decathlon.ara.domain.security.member.user.account.User;
import com.decathlon.ara.repository.security.member.user.account.UserRepository;
import com.decathlon.ara.security.dto.authentication.user.AuthenticatedOAuth2User;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.mapper.AuthenticationMapper;
import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.mapper.UserMapper;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.decathlon.ara.Entities.USER;

@Service
public class UserSessionService {

    private static final Logger LOG = LoggerFactory.getLogger(UserSessionService.class);

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final AuthorityMapper authorityMapper;

    private final AuthenticationMapper authenticationMapper;

    public UserSessionService(
            UserRepository userRepository,
            UserMapper userMapper,
            AuthorityMapper authorityMapper,
            AuthenticationMapper authenticationMapper
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authorityMapper = authorityMapper;
        this.authenticationMapper = authenticationMapper;
    }

    /**
     * Get the current user role on a given project, if found.
     * Note that if the user is a super admin or an auditor, the role is still returned (if found) but not taken into account.
     * @param projectCode the project code
     * @return the role, if found
     */
    public Optional<UserAccountScopeRole> getCurrentUserAccountScopeRoleFromProjectCode(String projectCode) {
        if (StringUtils.isBlank(projectCode)) {
            return Optional.empty();
        }

        return getCurrentUserScopes().stream()
                .filter(scope -> projectCode.equals(scope.getProject()))
                .map(UserAccountScope::getRole)
                .findFirst();
    }

    private Collection<GrantedAuthority> getCurrentAuthorities() {
        return getCurrentOAuth2Authentication().map(AbstractAuthenticationToken::getAuthorities).orElse(new HashSet<>());
    }

    private Optional<OAuth2AuthenticationToken> getCurrentOAuth2Authentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && (authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            return Optional.of(oauth2Authentication);
        }

        return Optional.empty();
    }

    /**
     * Get the current {@link UserAccountProfile}
     * @return the user profile
     * @throws ForbiddenException thrown if the current user profile couldn't be fetched
     */
    public UserAccountProfile getCurrentUserProfile() throws ForbiddenException {
        var exception = new ForbiddenException(USER, "fetch current user profile");

        var authorities = getCurrentAuthorities();
        return authorityMapper.getUserAccountProfileFromAuthorities(authorities).orElseThrow(() -> exception);
    }

    /**
     * Get the logged-in user scopes
     * @return the user scopes
     */
    public List<UserAccountScope> getCurrentUserScopes() {
        var authorities = getCurrentAuthorities();
        return authorityMapper.getUserAccountScopesFromAuthorities(authorities).stream().toList();
    }

    /**
     * Show if the current user can manage a user group
     * @param groupId the group id
     * @return true iff the current user can manage this group
     */
    public boolean canManageGroup(@NonNull long groupId) {
        try {
            var profile = getCurrentUserProfile();
            if (!UserAccountProfile.SCOPED_USER.equals(profile)) {
                return UserAccountProfile.SUPER_ADMIN.equals(profile);
            }

            var authorities = getCurrentAuthorities();
            return authorityMapper.getManagedUserAccountGroupIdsFromAuthorities(authorities).contains(groupId);
        } catch (ForbiddenException e) {
            return false;
        }
    }

    /**
     * Refresh the logged-in user authorities (updated in the {@link org.springframework.security.core.context.SecurityContext})
     * @throws ForbiddenException thrown if the refresh has failed
     */
    public void refreshCurrentUserAuthorities() throws ForbiddenException {
        var exception = new ForbiddenException(USER, "refresh authorities");

        var oauth2Authentication = getCurrentOAuth2Authentication().orElseThrow(() -> exception);

        var authenticatedUser = getCurrentAuthenticatedOAuth2UserFromAuthentication(oauth2Authentication).orElseThrow(() -> exception);

        var providerName = authenticatedUser.getProviderName();
        var userLogin = authenticatedUser.getLogin();

        var currentUser = userRepository.findById(new User.UserId(providerName, userLogin)).orElseThrow(() -> exception);
        var currentUserAccount = userMapper.getFullScopeAccessUserAccountFromUser(currentUser);

        var oauth2User = oauth2Authentication.getPrincipal();
        var authorities = authorityMapper.getGrantedAuthoritiesFromUserAccount(currentUserAccount);
        SecurityContextHolder.getContext().setAuthentication(new OAuth2AuthenticationToken(oauth2User, authorities, providerName));
    }

    /**
     * Get current authenticated (OAuth2) user
     * @return the current authenticated user
     */
    public Optional<AuthenticatedOAuth2User> getCurrentAuthenticatedOAuth2User() {
        return getCurrentOAuth2Authentication().flatMap(this::getCurrentAuthenticatedOAuth2UserFromAuthentication);
    }

    /**
     * Get current authenticated user from a {@link OAuth2AuthenticationToken}
     * @param authentication the authentication
     * @return the current authenticated user
     */
    public Optional<AuthenticatedOAuth2User> getCurrentAuthenticatedOAuth2UserFromAuthentication(@NonNull OAuth2AuthenticationToken authentication) {
        try {
            var authenticatedUser = authenticationMapper.getAuthenticatedOAuth2UserFromAuthentication(authentication);
            return Optional.of(authenticatedUser);
        } catch (ForbiddenException e) {
            LOG.warn("Current user couldn't be fetched...");
        }
        return Optional.empty();
    }

    /**
     * Get current authenticated user from {@link OAuth2User} and provider name
     * @param oauth2User the oauth2 user
     * @param providerName the provider name
     * @return the current authenticated user
     */
    public Optional<AuthenticatedOAuth2User> getCurrentAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(@NonNull OAuth2User oauth2User, @NonNull String providerName) {
        try {
            var authenticatedUser = authenticationMapper.getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName);
            return Optional.of(authenticatedUser);
        } catch (ForbiddenException e) {
            LOG.warn("Current user couldn't be fetched...");
        }
        return Optional.empty();
    }

}
