package com.decathlon.ara.authentication;

import static org.mockito.Mockito.when;

import com.decathlon.ara.web.rest.authentication.UserResource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    private OidcUser oidcUser;

    @Mock
    private OAuth2User oauth2User;

    private UserResource userResource = new UserResource();

    @Test
    void whenOIDCUser_userDetails_should_not_be_null() {
        when(oidcUser.getSubject()).thenReturn("oidc_id");
        var userDetails = userResource.getUserDetails(oidcUser, null);
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals("oidc_id", userDetails.getId());
    }

    @Test
    void whenOAauth2User_userDetails_should_not_be_null() {
        when(oauth2User.getAttribute("id")).thenReturn("oauth2_id");
        var userDetails = userResource.getUserDetails(null, oauth2User);
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals("oauth2_id", userDetails.getId());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void whenOAauth2UserAndOIDCUser_userDetails_should_be_generated_from_oidc() {
        when(oauth2User.getAttribute("name")).thenReturn("oauth2_name");
        when(oidcUser.getFullName()).thenReturn("oidc_name");
        var userDetails = userResource.getUserDetails(oidcUser, oauth2User);
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals("oidc_name", userDetails.getName());
    }

    @Test
    void whenNoUser_userDetails_should_not_be_null() {
        Assertions.assertNotNull(userResource.getUserDetails(null, null));
    }
    
}
