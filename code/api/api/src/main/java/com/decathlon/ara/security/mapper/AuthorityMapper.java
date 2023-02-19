package com.decathlon.ara.security.mapper;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;

@Component
public class AuthorityMapper {

    public static final String AUTHORITY_USER_PROJECT_SCOPE_PREFIX = "USER_PROJECT_SCOPE:";
    public static final String AUTHORITY_USER_PROFILE_PREFIX = "USER_PROFILE:";

    public static final String AUTHORITY_MANAGED_GROUP_PREFIX = "MANAGED_GROUP:";

    public static final String AUTHORITY_USER_PROFILE_SUPER_ADMIN = AUTHORITY_USER_PROFILE_PREFIX + UserAccountProfile.SUPER_ADMIN.name();
    public static final String AUTHORITY_USER_PROFILE_AUDITOR = AUTHORITY_USER_PROFILE_PREFIX + UserAccountProfile.AUDITOR.name();

    /**
     * Extract a {@link Collection} of {@link GrantedAuthority} from a {@link UserAccount}
     * @param userAccount the user account
     * @return the matching granted authorities
     */
    public Collection<GrantedAuthority> getGrantedAuthoritiesFromUserAccount(@NonNull UserAccount userAccount) {
        var profileAuthority = getProfileAuthorityFromUserAccountProfile(userAccount.getProfile());
        var scopeAuthorities = getScopeAuthoritiesFromUserAccountScopes(userAccount.getScopes());
        var managedGroupAuthorities = getManagedGroupAuthoritiesFromManagedGroupIds(userAccount.getManagedGroupIds());
        return Stream.of(Set.of(profileAuthority), scopeAuthorities, managedGroupAuthorities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private GrantedAuthority getProfileAuthorityFromUserAccountProfile(@NonNull UserAccountProfile profile) {
        return () -> String.format("%s%s", AUTHORITY_USER_PROFILE_PREFIX, profile.name());
    }

    private Collection<GrantedAuthority> getScopeAuthoritiesFromUserAccountScopes(Collection<UserAccountScope> scopes) {
        if (CollectionUtils.isEmpty(scopes)) {
            return new HashSet<>();
        }
        return scopes.stream().map(AuthorityMapper::getScopeAuthorityFromUserAccountScope).collect(Collectors.toSet());
    }

    private static GrantedAuthority getScopeAuthorityFromUserAccountScope(@NonNull UserAccountScope scope) {
        return () -> String.format("%s%s:%s", AUTHORITY_USER_PROJECT_SCOPE_PREFIX, scope.getProject(), scope.getRole());
    }

    private Collection<GrantedAuthority> getManagedGroupAuthoritiesFromManagedGroupIds(@NonNull Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return new HashSet<>();
        }
        return groupIds.stream().map(AuthorityMapper::getManagedGroupAuthorityFromManagedGroupId).collect(Collectors.toSet());
    }

    private static GrantedAuthority getManagedGroupAuthorityFromManagedGroupId(@NonNull Long groupId) {
        return () -> String.format("%s%d", AUTHORITY_MANAGED_GROUP_PREFIX, groupId);
    }

    /**
     * Get {@link UserAccountProfile} from a {@link Collection} of {@link GrantedAuthority}, if found
     * @param authorities the granted authorities
     * @return the user profile (as an optional)
     */
    public Optional<UserAccountProfile> getUserAccountProfileFromAuthorities(@NonNull Collection<GrantedAuthority> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return Optional.empty();
        }
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(AUTHORITY_USER_PROFILE_PREFIX))
                .map(authority -> authority.split(":"))
                .filter(array -> array.length == 2)
                .findFirst()
                .map(array -> array[1])
                .flatMap(UserAccountProfile::getProfileFromString);
    }

    /**
     * Get a {@link Collection} of {@link UserAccountScope} from a {@link Collection} of {@link GrantedAuthority}
     * Note that any user will have the demo project in its scope, as anyone is an admin in this project
     * @param authorities the authorities
     * @return user scopes
     */
    public Collection<UserAccountScope> getUserAccountScopesFromAuthorities(@NonNull Collection<GrantedAuthority> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return new ArrayList<>();
        }

        var demoProjectScopeStream = Stream.of(new UserAccountScope(DEMO_PROJECT_CODE, UserAccountScopeRole.ADMIN));
        var userAccountScopesStream = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(AUTHORITY_USER_PROJECT_SCOPE_PREFIX))
                .map(authority -> authority.split(":"))
                .filter(array -> array.length == 3)
                .map(array -> UserAccountScope.userAccountScopeFactory(array[1], array[2]))
                .filter(Optional::isPresent)
                .map(Optional::get);
        return Stream.concat(userAccountScopesStream, demoProjectScopeStream).toList();
    }

    /**
     * Get managed user group ids from a {@link Collection} of {@link GrantedAuthority}
     * @param authorities the authorities
     * @return the managed group ids
     */
    public Collection<Long> getManagedUserAccountGroupIdsFromAuthorities(@NonNull Collection<GrantedAuthority> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return new ArrayList<>();
        }

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(AUTHORITY_MANAGED_GROUP_PREFIX))
                .map(authority -> authority.split(":"))
                .filter(array -> array.length == 2)
                .map(array -> array[1])
                .map(Long::parseLong)
                .sorted()
                .distinct()
                .toList();
    }
}
