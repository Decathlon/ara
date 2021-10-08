package com.decathlon.ara.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.annotation.PostConstruct;

@Configuration
public class CustomAuthenticationConf {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationConf.class);

    @Autowired
    Environment env;

    private Boolean oauthAuthorizationCodeActive;
    private Boolean oauthClientCredentialsActive;

    @PostConstruct
    // I want properties to be correctly in the correct type so I dont use @Value
    public void initialize(){

        String needEnableAuthorizationCodeProp = env.getProperty("feature-flipping.authent.oauth.features.authorization-code");
        String needEnableOauthClientCredentialsProp = env.getProperty("feature-flipping.authent.oauth.features.client-credentials");
        logger.debug("Props: enable authorization code: {}, enalbe client credentials: {}",
                needEnableAuthorizationCodeProp, needEnableOauthClientCredentialsProp);
        this.oauthAuthorizationCodeActive= "true".equals(needEnableAuthorizationCodeProp);
        this.oauthClientCredentialsActive= "true".equals(needEnableOauthClientCredentialsProp);
    }

    // authorize users by token ( jwt )
    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http ){
        // Whatever is the authentication method, we need to authorize /actguatorgw
        http
                .authorizeExchange()
                .pathMatchers("/actuatorgw/**")
                .permitAll()
                .and();

        // Then optimize with the target auth method
        return this.enableOauth(http).build();

    }

    // Enable Oauth ( default )
    public ServerHttpSecurity  enableOauth(
            ServerHttpSecurity http ) {

        logger.info("Global oauth2 feature enabled");
        logger.info("-> authorization code flow active ? {}", this.oauthAuthorizationCodeActive);
        logger.info("-> client credentials flow active ? {}", this.oauthClientCredentialsActive);
        if (!this.oauthClientCredentialsActive && !this.oauthAuthorizationCodeActive){
            logger.error("Enable oauth without enabling any oauth features doesn't make sense. You wont be able to authenticate");
            throw new RuntimeException("oauth enabled but neither 'client credential' nor 'authorize code' flows active");
        }

        http
                .csrf().disable()//NOSONAR
                .authorizeExchange()
                .anyExchange()
                .authenticated().and();

        if (this.oauthAuthorizationCodeActive){
            http
                    .oauth2Login()
                    .and();
        }

        if (this.oauthClientCredentialsActive){
            http
                    .oauth2ResourceServer()
                    .jwt();
        }

        return http;

    }

}

