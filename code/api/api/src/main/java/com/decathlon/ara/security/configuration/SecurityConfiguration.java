package com.decathlon.ara.security.configuration;

import com.decathlon.ara.security.service.AuthenticationService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

@Configuration
public class SecurityConfiguration {

    @Value("${ara.clientBaseUrl}")
    private String clientBaseUrl;

    @Value("${ara.loginStartingUrl}")
    private String loginStartingUrl;

    @Value("${ara.loginProcessingUrl}")
    private String loginProcessingUrl;

    @Value("${ara.logoutProcessingUrl}")
    private String logoutProcessingUrl;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String resourceIssuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String resourceJwkSetUri;

    private final AuthenticationService authenticationService;

    public SecurityConfiguration(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Whatever is the authentication method, we need to authorize /actguatorgw
        String redirectUrl = String.format("%s?spring_redirect=true", this.clientBaseUrl);
        http
                .csrf().disable() //NOSONAR
                .authorizeRequests() //NOSONAR
                .antMatchers("/oauth/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl(this.logoutProcessingUrl) // logout entrypoint
                .invalidateHttpSession(true)
                .clearAuthentication(true).logoutSuccessUrl(redirectUrl).deleteCookies("JSESSIONID").permitAll()
                .and()
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(this.oidcUserService()))
                        .loginProcessingUrl(this.loginProcessingUrl + "/*") // path to redirect to from auth server
                        .loginPage(String.format("%s/%s", this.clientBaseUrl, "login")) // standard spring redirection for protected resources
                        .defaultSuccessUrl(redirectUrl, true) // once logged in, redirect to
                        .authorizationEndpoint().baseUri(this.loginStartingUrl) // entrypoint to initialize oauth processing
                );

        if (isResourceServerConfiguration()) {
            http.oauth2ResourceServer().jwt();
        }
        return http.build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return userRequest -> {
            var providerName = userRequest.getClientRegistration().getRegistrationId();
            OidcUser oidcUser = delegate.loadUser(userRequest);
            Set<GrantedAuthority> mappedAuthorities = authenticationService.manageUserAtLogin(oidcUser, providerName);
            oidcUser = new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
            return oidcUser;
        };
    }

    private boolean isResourceServerConfiguration() {
        return Strings.isNotBlank(resourceIssuerUri) || Strings.isNotBlank(resourceJwkSetUri);
    }

}
