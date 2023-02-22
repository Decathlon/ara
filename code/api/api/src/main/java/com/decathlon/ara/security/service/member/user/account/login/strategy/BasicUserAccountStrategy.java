package com.decathlon.ara.security.service.member.user.account.login.strategy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BasicUserAccountStrategy implements UserAccountStrategy {

    private final Map<String, String> customProviderAttributes;

    private static final Logger LOG = LoggerFactory.getLogger(BasicUserAccountStrategy.class);

    public BasicUserAccountStrategy(@NonNull Map<String, String> customProviderAttributes) {
        this.customProviderAttributes = CollectionUtils.isEmpty(customProviderAttributes) ?
                new HashMap<>() :
                customProviderAttributes;
    }

    @Override
    public String getLogin(OAuth2User oauth2User) {
        return oauth2User.getName();
    }

    @Override
    public Optional<String> getFirstName(OAuth2User oauth2User) {
        return getUserFieldValue(oauth2User, StandardClaimNames.GIVEN_NAME);
    }

    @Override
    public Optional<String> getLastName(OAuth2User oauth2User) {
        return getUserFieldValue(oauth2User, StandardClaimNames.FAMILY_NAME);
    }

    @Override
    public Optional<String> getEmail(OAuth2User oauth2User) {
        return getUserFieldValue(oauth2User, StandardClaimNames.EMAIL);
    }

    @Override
    public Optional<String> getPictureUrl(OAuth2User oauth2User) {
        return getUserFieldValue(oauth2User, StandardClaimNames.PICTURE);
    }

    private Optional<String> getUserFieldValue(OAuth2User oauth2User, String userField) {
        if (oauth2User instanceof OidcUser oidcUser) {
            Optional<String> userFieldValueFromOidcUser = getUserFieldValueFromOidcUser(oidcUser, userField);
            if (userFieldValueFromOidcUser.isPresent()) {
                return userFieldValueFromOidcUser;
            }
        }
        return getUserFieldValueFromAttribute(oauth2User, userField);
    }

    private static Optional<String> getUserFieldValueFromOidcUser(OidcUser oidcUser, String userField) {
        try {
            var getterName = "get" + CaseUtils.toCamelCase(userField, true, '_');
            var getterMethod = oidcUser.getClass().getMethod(getterName);
            var getterReturnedValue = (String) getterMethod.invoke(oidcUser);
            if (StringUtils.isNotBlank(getterReturnedValue)) {
                return Optional.of(getterReturnedValue);
            }
            return Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.warn("The (oidc) user field '{}' was not found...", userField);
            return Optional.empty();
        }
    }

    private Optional<String> getUserFieldValueFromAttribute(OAuth2User oauth2User, String userField) {
        var userFieldValue = (String) oauth2User.getAttribute(userField);
        if (StringUtils.isNotBlank(userFieldValue)) {
            return Optional.of(userFieldValue);
        }
        var customUserField = customProviderAttributes.get(userField);
        return Optional.ofNullable(oauth2User.getAttribute(customUserField));
    }

    public Map<String, String> getCustomProviderAttributes() {
        return customProviderAttributes;
    }
}
