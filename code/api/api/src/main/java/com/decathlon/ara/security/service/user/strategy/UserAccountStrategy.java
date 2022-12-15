package com.decathlon.ara.security.service.user.strategy;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.security.dto.user.UserAccount;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Optional;

public interface UserAccountStrategy {

    /**
     * Create a {@link UserAccount} from an {@link OAuth2User} and a {@link UserEntity}
     * @param oauth2User the oauth2 user
     * @param userEntity the user entity
     * @return the {@link UserAccount}
     */
    public default UserAccount getUserAccount(@NonNull OAuth2User oauth2User, @NonNull UserEntity userEntity) {
        var userAttributes = new HashMap<String, String>();

        var login = getLogin(oauth2User);
        userAttributes.put(StandardClaimNames.NAME, login);

        var firstName = getFirstName(oauth2User);
        firstName.ifPresent(value -> userAttributes.put(StandardClaimNames.GIVEN_NAME, value));

        var lastName = getLastName(oauth2User);
        lastName.ifPresent(value -> userAttributes.put(StandardClaimNames.FAMILY_NAME, value));

        var email = getEmail(oauth2User);
        email.ifPresent(value -> userAttributes.put(StandardClaimNames.EMAIL, value));

        var pictureUrl = getPictureUrl(oauth2User);
        pictureUrl.ifPresent(value -> userAttributes.put(StandardClaimNames.PICTURE, value));

        return new UserAccount(userAttributes, userEntity);
    }

    public abstract String getLogin(OAuth2User oauth2User);

    public abstract Optional<String> getFirstName(OAuth2User oauth2User);

    public abstract Optional<String> getLastName(OAuth2User oauth2User);

    public abstract Optional<String> getEmail(OAuth2User oauth2User);

    public abstract Optional<String> getPictureUrl(OAuth2User oauth2User);
}
