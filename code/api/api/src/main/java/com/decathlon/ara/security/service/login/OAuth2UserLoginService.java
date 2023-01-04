package com.decathlon.ara.security.service.login;

import com.decathlon.ara.security.mapper.AuthorityMapper;
import com.decathlon.ara.security.service.user.UserAccountService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class OAuth2UserLoginService extends UserLoginService<OAuth2UserRequest, OAuth2User, OAuth2UserService<OAuth2UserRequest, OAuth2User>> {

    public OAuth2UserLoginService(UserAccountService userAccountService, AuthorityMapper authorityMapper) {
        super(userAccountService, authorityMapper);
    }

    @Override
    protected OAuth2UserService<OAuth2UserRequest, OAuth2User> getUserService() {
        return userAccountService.getDefaultOAuth2UserService();
    }

    @Override
    protected OAuth2User getUpdatedOAuth2User(Set<GrantedAuthority> authorities, OAuth2User user) {
        return new DefaultOAuth2User(authorities, user.getAttributes(), StandardClaimNames.NAME);
    }
}
