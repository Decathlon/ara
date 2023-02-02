package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.security.member.user.User;
import com.decathlon.ara.domain.security.member.user.UserProfile;
import com.decathlon.ara.domain.security.member.user.UserScope;
import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.service.UserSessionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AuthorityMapper {

    /**
     * Extract granted authorities from a {@link User}
     * @param user the user
     * @return the matching granted authorities
     */
    public Set<GrantedAuthority> getGrantedAuthoritiesFromUser(@NonNull User user) {
        var profileAuthority = getProfileAuthorityFromUserProfile(user.getProfile());
        var scopeAuthorities = getScopeAuthoritiesFromUserScopes(user.getScopes());
        return Stream.of(Set.of(profileAuthority), scopeAuthorities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private GrantedAuthority getProfileAuthorityFromUserProfile(@NonNull UserProfile profile) {
        return getProfileAuthorityFromUserProfileAsString(profile.name());
    }

    private GrantedAuthority getProfileAuthorityFromUserProfileAsString(String profileAsString) {
        return () -> String.format("%s%s", UserSessionService.AUTHORITY_USER_PROFILE_PREFIX, profileAsString);
    }

    private Set<GrantedAuthority> getScopeAuthoritiesFromUserScopes(List<UserScope> scopes) {
        return CollectionUtils.isNotEmpty(scopes) ?
                scopes.stream()
                        .filter(scope -> scope.getProject() != null)
                        .filter(scope -> StringUtils.isNotBlank(scope.getProject().getCode()))
                        .filter(scope -> scope.getRole() != null)
                        .map(this::getScopeAuthorityFromUserScope)
                        .collect(Collectors.toSet()) :
                new HashSet<>();
    }

    private GrantedAuthority getScopeAuthorityFromUserScope(@NonNull UserScope scope) {
        var projectCode = scope.getProject().getCode();
        var roleAsString = scope.getRole().name();
        return getScopeAuthorityFromProjectCodeAndRoleAsStringPair(Pair.of(projectCode, roleAsString));
    }

    private GrantedAuthority getScopeAuthorityFromProjectCodeAndRoleAsStringPair(Pair<String, String> projectCodeAndRoleAsStringPair) {
        var projectCode = projectCodeAndRoleAsStringPair.getFirst();
        var roleAsString = projectCodeAndRoleAsStringPair.getSecond();
        var projectCodeAndRole = String.format("%s:%s", projectCode, roleAsString);
        var authorityAsString = String.format("%s%s", UserSessionService.AUTHORITY_USER_PROJECT_SCOPE_PREFIX, projectCodeAndRole);
        return () -> authorityAsString;
    }

    /**
     * Extract granted authorities from a user account
     * @param userAccount the user account
     * @return the matching granted authorities
     */
    public Set<GrantedAuthority> getGrantedAuthoritiesFromUserAccount(@NonNull UserAccount userAccount) {
        var profileAuthority = getProfileAuthorityFromUserAccountProfile(userAccount.getProfile());
        var scopeAuthorities = getScopeAuthoritiesFromUserAccountScopes(userAccount.getScopes());
        return Stream.of(Set.of(profileAuthority), scopeAuthorities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private GrantedAuthority getProfileAuthorityFromUserAccountProfile(@NonNull UserAccountProfile profile) {
        return getProfileAuthorityFromUserProfileAsString(profile.name());
    }

    private Set<GrantedAuthority> getScopeAuthoritiesFromUserAccountScopes(List<UserAccountScope> scopes) {
        return CollectionUtils.isNotEmpty(scopes) ?
                scopes.stream()
                        .map(this::getScopeAuthorityFromUserAccountScope)
                        .collect(Collectors.toSet()) :
                new HashSet<>();
    }

    private GrantedAuthority getScopeAuthorityFromUserAccountScope(@NonNull UserAccountScope scope) {
        var projectCode = scope.getProject();
        var roleAsString = scope.getRole().name();
        return getScopeAuthorityFromProjectCodeAndRoleAsStringPair(Pair.of(projectCode, roleAsString));
    }
}
