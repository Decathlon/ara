package com.decathlon.ara.security.mapper;

import com.decathlon.ara.security.dto.authentication.user.AuthenticatedOAuth2User;
import com.decathlon.ara.security.service.member.user.account.login.strategy.select.UserStrategySelector;
import com.decathlon.ara.service.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import static com.decathlon.ara.Entities.USER;

@Component
public class AuthenticationMapper {

    private final UserStrategySelector strategySelector;

    public AuthenticationMapper(UserStrategySelector strategySelector) {
        this.strategySelector = strategySelector;
    }

    /**
     * Extract {@link AuthenticatedOAuth2User} from {@link OAuth2AuthenticationToken}
     * @param authentication the authentication
     * @return the matching {@link AuthenticatedOAuth2User}
     * @throws ForbiddenException thrown if the principal ({@link org.springframework.security.oauth2.core.user.OAuth2User}) is null or the provider name is blank
     */
    public AuthenticatedOAuth2User getAuthenticatedOAuth2UserFromAuthentication(@NonNull OAuth2AuthenticationToken authentication) throws ForbiddenException {
        var exception =  new ForbiddenException(USER, "current user access");

        var oauth2User = authentication.getPrincipal();
        var providerName = authentication.getAuthorizedClientRegistrationId();

        if (oauth2User == null || StringUtils.isBlank(providerName)) {
            throw exception;
        }

        return getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(oauth2User, providerName);
    }

    /**
     * Extract {@link AuthenticatedOAuth2User} from {@link OAuth2User} and provider name
     * @param oauth2User the oauth2 user
     * @param providerName the provider name
     * @return the matching {@link AuthenticatedOAuth2User}
     * @throws ForbiddenException thrown if the principal ({@link org.springframework.security.oauth2.core.user.OAuth2User}) is null or the provider name is blank
     */
    public AuthenticatedOAuth2User getAuthenticatedOAuth2UserFromOAuth2UserAndProviderName(@NonNull OAuth2User oauth2User, @NonNull String providerName) throws ForbiddenException {
        var exception =  new ForbiddenException(USER, "current user access");

        if (StringUtils.isBlank(providerName)) {
            throw exception;
        }

        var strategy = strategySelector.selectUserStrategyFromProviderName(providerName);
        var userLogin = strategy.getLogin(oauth2User);
        var authenticatedOAuth2User = new AuthenticatedOAuth2User(providerName, userLogin);

        var firstName = strategy.getFirstName(oauth2User);
        firstName.ifPresent(authenticatedOAuth2User::setFirstName);

        var lastName = strategy.getLastName(oauth2User);
        lastName.ifPresent(authenticatedOAuth2User::setLastName);

        var email = strategy.getEmail(oauth2User);
        email.ifPresent(authenticatedOAuth2User::setEmail);

        var pictureUrl = strategy.getPictureUrl(oauth2User);
        pictureUrl.ifPresent(authenticatedOAuth2User::setPictureUrl);

        return authenticatedOAuth2User;
    }
}
