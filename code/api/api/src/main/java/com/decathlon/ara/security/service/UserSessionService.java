package com.decathlon.ara.security.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.loader.DemoLoaderConstants;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.service.user.strategy.select.UserStrategySelector;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class UserSessionService {

    public static final String AUTHORITY_USER_PROJECT_SCOPE_PREFIX = "USER_PROJECT_SCOPE:";

    public static final String AUTHORITY_USER_PROFILE_PREFIX = "USER_PROFILE:";

    public static final String SUPER_ADMIN_PROFILE_AUTHORITY = AUTHORITY_USER_PROFILE_PREFIX + UserAccountProfile.SUPER_ADMIN.name();
    public static final String AUDITOR_PROFILE_AUTHORITY = AUTHORITY_USER_PROFILE_PREFIX + UserAccountProfile.AUDITOR.name();

    private final UserStrategySelector strategySelector;

    private final UserEntityRepository userEntityRepository;

    private final AuthorityMapper authorityMapper;

    public UserSessionService(
            UserStrategySelector strategySelector,
            UserEntityRepository userEntityRepository,
            AuthorityMapper authorityMapper
    ) {
        this.strategySelector = strategySelector;
        this.userEntityRepository = userEntityRepository;
        this.authorityMapper = authorityMapper;
    }

    /**
     * Get the current user role on a given project, if found.
     * Note that if the user is a super admin or an auditor, the role is still returned (if found) but not taken into account.
     * @param projectCode the project code
     * @return the role, if found
     */
    public Optional<UserAccountScopeRole> getCurrentUserRoleOnProject(String projectCode) {
        if (StringUtils.isBlank(projectCode)) {
            return Optional.empty();
        }

        return getCurrentUserScopes().stream()
                .filter(scope -> projectCode.equals(scope.getProject()))
                .map(UserAccountScope::getRole)
                .findFirst();
    }

    private boolean userDoesNotHaveAccessToAnyAuthority() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        var authorities = authentication.getAuthorities();
        return CollectionUtils.isEmpty(authorities);
    }

    private Stream<String[]> getSplitAuthoritiesStreamFromPrefix(String prefix) {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(prefix))
                .map(authority -> authority.split(":"));
    }

    /**
     * Get the current user profile, if found (and correct)
     * @return the user profile
     */
    public Optional<UserAccountProfile> getCurrentUserProfile() {
        if (userDoesNotHaveAccessToAnyAuthority()) {
            return Optional.empty();
        }

        var splitAuthoritiesStream = getSplitAuthoritiesStreamFromPrefix(AUTHORITY_USER_PROFILE_PREFIX);
        return splitAuthoritiesStream
                .filter(array -> array.length == 2)
                .findFirst()
                .map(array -> array[1])
                .map(UserAccountProfile::getProfileFromString)
                .orElse(Optional.empty());
    }

    /**
     * Get the current user scoped project codes, if any
     * @return the scoped project codes
     */
    public List<String> getCurrentUserScopedProjectCodes() {
        return getCurrentUserScopes().stream().map(UserAccountScope::getProject).sorted().toList();
    }

    /**
     * Get the logged-in user scopes
     * @return the user scopes
     */
    public List<UserAccountScope> getCurrentUserScopes() {
        if (userDoesNotHaveAccessToAnyAuthority()) {
            return new ArrayList<>();
        }

        var userAccountScopesStream = getSplitAuthoritiesStreamFromPrefix(AUTHORITY_USER_PROJECT_SCOPE_PREFIX)
                .filter(array -> array.length == 3)
                .map(array -> UserAccountScope.userAccountScopeFactory(array[1], array[2]))
                .filter(Optional::isPresent)
                .map(Optional::get);
        var demoProjectScopeStream = Stream.of(new UserAccountScope(DemoLoaderConstants.DEMO_PROJECT_CODE, UserAccountScopeRole.ADMIN));
        return Stream.concat(userAccountScopesStream, demoProjectScopeStream).toList();
    }

    /**
     * Refresh the logged-in user authorities (updated in the {@link org.springframework.security.core.context.SecurityContext})
     * @throws ForbiddenException thrown if the refresh has failed
     */
    public void refreshCurrentUserAuthorities() throws ForbiddenException {
        var exception = new ForbiddenException(Entities.PROJECT, "refresh authorities");

        var securityContext = SecurityContextHolder.getContext();
        var authentication = securityContext.getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            throw exception;
        }

        var oauth2User = oauth2Authentication.getPrincipal();
        var providerName = oauth2Authentication.getAuthorizedClientRegistrationId();

        var strategy = strategySelector.selectUserStrategyFromProviderName(providerName);
        var userLogin = strategy.getLogin(oauth2User);

        var persistedUser = userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName)).orElseThrow(() -> exception);

        var authorities = authorityMapper.getGrantedAuthoritiesFromUserEntity(persistedUser);
        securityContext.setAuthentication(new OAuth2AuthenticationToken(oauth2User, authorities, providerName));
    }

}
