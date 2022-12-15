package com.decathlon.ara.security.service.login;

import com.decathlon.ara.security.service.user.UserAccountService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class OidcUserLoginService extends UserLoginService<OidcUserRequest, OidcUser, OidcUserService> {

    public OidcUserLoginService(UserAccountService userAccountService) {
        super(userAccountService);
    }

    @Override
    protected OidcUserService getUserService() {
        return userAccountService.getOidcUserService();
    }

    @Override
    protected OidcUser getUpdatedOAuth2User(Set<GrantedAuthority> authorities, OidcUser user) {
        return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo());
    }
}
