package com.decathlon.ara.security.service.member.user.account.login;

import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.service.member.user.account.UserSessionService;
import com.decathlon.ara.security.service.member.user.account.UserAccountService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class OidcUserLoginService extends UserLoginService<OidcUserRequest, OidcUser, OidcUserService> {

    public OidcUserLoginService(
            UserAccountService userAccountService,
            UserSessionService userSessionService,
            AuthorityMapper authorityMapper
    ) {
        super(userAccountService, userSessionService, authorityMapper);
    }

    @Override
    protected OidcUserService getUserService() {
        return userAccountService.getOidcUserService();
    }

    @Override
    protected OidcUser getUpdatedOAuth2User(Collection<GrantedAuthority> authorities, OidcUser user) {
        return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo());
    }
}
