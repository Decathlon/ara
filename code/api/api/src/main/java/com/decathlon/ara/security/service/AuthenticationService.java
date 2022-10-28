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

package com.decathlon.ara.security.service;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.security.member.user.entity.UserEntityRepository;
import com.decathlon.ara.security.configuration.data.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.data.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.dto.provider.AuthenticationProviderDTO;
import com.decathlon.ara.security.dto.provider.AuthenticationProvidersDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
@Transactional
public class AuthenticationService {

    private UserEntityRepository userEntityRepository;

    private ProjectRepository projectRepository;

    private final OAuth2ProvidersConfiguration providersConfiguration;

    @Value("${ara.loginStartingUrl}")
    private String loginStartingUrl;

    @Value("${ara.logoutProcessingUrl}")
    private String logoutProcessingUrl;

    public AuthenticationService(UserEntityRepository userEntityRepository, ProjectRepository projectRepository, OAuth2ProvidersConfiguration providersConfiguration) {
        this.userEntityRepository = userEntityRepository;
        this.projectRepository = projectRepository;
        this.providersConfiguration = providersConfiguration;
    }

    /**
     * Get the authentication configuration
     * @return the authentication configuration
     */
    public AuthenticationProvidersDTO getAuthenticationConfiguration() {
        var providers = CollectionUtils.isEmpty(this.providersConfiguration.getSetup()) ?
                new ArrayList<AuthenticationProviderDTO>() :
                this.providersConfiguration.getSetup()
                        .stream()
                        .map(ProviderSetupConfiguration::getProvider)
                        .filter(Objects::nonNull)
                        .map(provider -> new AuthenticationProviderDTO(provider.getDisplayValue(), provider.getType(), provider.getRegistration()))
                        .toList();
        return new AuthenticationProvidersDTO(
                providers,
                this.loginStartingUrl,
                this.logoutProcessingUrl);
    }

    /**
     * Manage the user at login: if needed, save the user and return its granted authorities.
     * @param oidcUser the OAuth2 user
     * @param providerName the provider name
     * @return the granted authorities the user has
     */
    public Set<GrantedAuthority> manageUserAtLogin(OidcUser oidcUser, String providerName) {
        var userLogin = oidcUser.getSubject();
        var registeredUser = userEntityRepository.findById(new UserEntity.UserEntityId(userLogin, providerName));
        var user = registeredUser.isPresent() ? registeredUser.get() : createNewUser(oidcUser, providerName);
        return user.getMatchingAuthorities();
    }

    private UserEntity createNewUser(OidcUser oidcUser, String providerName) {
        var userFoundInConfiguration = providersConfiguration.getMatchingUserEntityFromProviderNameAndLogin(providerName, oidcUser.getSubject(), projectRepository);
        var newUser = userFoundInConfiguration.orElse(new UserEntity(oidcUser.getSubject(), providerName));

        var customAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);

        var email = getUserDetails(oidcUser, customAttributes, "email");
        email.ifPresent(newUser::setEmail);

        var firstName = getUserDetails(oidcUser, customAttributes, "givenName");
        firstName.ifPresent(newUser::setFirstName);

        var lastName = getUserDetails(oidcUser, customAttributes, "familyName");
        lastName.ifPresent(newUser::setLastName);

        userEntityRepository.save(newUser);
        return newUser;
    }

    private Optional<String> getUserDetails(OidcUser oidcUser, Map<String, String> customAttributes, String userDetailsField) {
        try {
            var getterName = String.format("get%s", userDetailsField.substring(0,1).toUpperCase() + userDetailsField.substring(1));
            var getterMethod = oidcUser.getClass().getMethod(getterName);
            var getterReturnedValue = (String) getterMethod.invoke(oidcUser);
            if (StringUtils.isNotBlank(getterReturnedValue)) {
                return Optional.of(getterReturnedValue);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }

        var attribute = StringUtils.isNotBlank(oidcUser.getAttribute(userDetailsField)) ? userDetailsField : customAttributes.get(userDetailsField);
        return Optional.ofNullable(oidcUser.getAttribute(attribute));
    }

}
