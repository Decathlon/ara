/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.security.dto.user;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.AuthorityService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserAccount {

    private String providerName;

    private String login;

    private String firstName;

    private String lastName;

    private String email;

    private String pictureUrl;

    private UserAccountProfile profile;

    private List<UserAccountScope> scopes;

    public UserAccount(@NonNull Map<String, String> userAttributes, @NonNull UserEntity userEntity) {
        this.providerName = userEntity.getProviderName();
        this.login = userEntity.getLogin();
        this.profile = getProfileFromUserEntity(userEntity);
        this.scopes = getScopesFromUserEntity(userEntity);

        this.firstName = userAttributes.get(StandardClaimNames.GIVEN_NAME);
        this.lastName = userAttributes.get(StandardClaimNames.FAMILY_NAME);
        this.email = userAttributes.get(StandardClaimNames.EMAIL);
        this.pictureUrl = userAttributes.get(StandardClaimNames.PICTURE);
    }

    public String getProviderName() {
        return providerName;
    }

    public String getLogin() {
        return login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public UserAccountProfile getProfile() {
        return profile;
    }

    public List<UserAccountScope> getScopes() {
        return scopes;
    }

    private UserAccountProfile getProfileFromUserEntity(@NonNull UserEntity userEntity) {
        var userEntityProfile = userEntity.getProfile();
        return UserAccountProfile.valueOf(userEntityProfile.name());
    }

    private List<UserAccountScope> getScopesFromUserEntity(@NonNull UserEntity userEntity) {
        var userRoles = userEntity.getRolesOnProjectWhenScopedUser();
        return CollectionUtils.isNotEmpty(userRoles) ?
                userRoles.stream()
                        .filter(role -> role.getProject() != null)
                        .filter(role -> role.getRole() != null)
                        .map(this::getUserAccountScopeFromUserEntityRole)
                        .toList():
                new ArrayList<>();
    }

    private UserAccountScope getUserAccountScopeFromUserEntityRole(@NonNull UserEntityRoleOnProject userEntityRole) {
        var projectCode = userEntityRole.getProject().getCode();
        var userAccountRole = getUserAccountScopeRoleFromScopedUserRoleOnProject(userEntityRole.getRole());
        return new UserAccountScope(projectCode, userAccountRole);
    }

    private UserAccountScopeRole getUserAccountScopeRoleFromScopedUserRoleOnProject(@NonNull UserEntityRoleOnProject.ScopedUserRoleOnProject scopedUserRoleOnProject) {
        return UserAccountScopeRole.valueOf(scopedUserRoleOnProject.name());
    }

    /**
     * Get all the user's matching authorities
     * @return the granted authorities
     */
    @JsonIgnore
    public Set<GrantedAuthority> getMatchingAuthorities() {
        var profileAuthority = getProfileAuthority();
        var scopeAuthorities = getScopeAuthorities();
        return Stream.of(Set.of(profileAuthority), scopeAuthorities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Get the authority matching this user profile
     * @return the granted authority
     */
    private GrantedAuthority getProfileAuthority() {
        return () -> String.format("%s%s", AuthorityService.AUTHORITY_USER_PROFILE_PREFIX, profile);
    }

    private Set<GrantedAuthority> getScopeAuthorities() {
        return CollectionUtils.isNotEmpty(scopes) ?
                scopes.stream()
                        .map(UserAccountScope::getMatchingAuthority)
                        .collect(Collectors.toSet()) :
                new HashSet<>();
    }
}
