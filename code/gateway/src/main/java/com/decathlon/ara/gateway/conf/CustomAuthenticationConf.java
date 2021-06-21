package com.decathlon.ara.gateway.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.annotation.PostConstruct;

@Configuration
public class CustomAuthenticationConf {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationConf.class);

    @Autowired
    Environment env;

    private Boolean oauthAuthorizationCodeActive;
    private Boolean oauthClientCredentialsActive;
    private Boolean needEnableOauth;

    @PostConstruct
    // I want properties to be correctly in the correct type so I dont use @Value
    public void initialize(){

        String needEnableOauthProp = env.getProperty("feature-flipping.authent.oauth.enabled");
        String needEnableAuthorizationCodeProp = env.getProperty("feature-flipping.authent.oauth.features.authorization-code");
        String needEnableOauthClientCredentialsProp = env.getProperty("feature-flipping.authent.oauth.features.client-credentials");
        logger.debug("Props: enable oaut: {}, enable auth code: {}, enalbe client creds: {}",
                needEnableOauthProp, needEnableAuthorizationCodeProp, needEnableOauthClientCredentialsProp);
        this.needEnableOauth = "true".equals(needEnableOauthProp);
        this.oauthAuthorizationCodeActive= "true".equals(needEnableAuthorizationCodeProp);
        this.oauthClientCredentialsActive= "true".equals(needEnableOauthClientCredentialsProp);
    }

    // Enable Oauth ( default )
    public ServerHttpSecurity  enableOauth(
            ServerHttpSecurity http ) {

        logger.info("Global oauth2 feature enabled");
        logger.info("-> authorization code flow active ? {}", this.oauthAuthorizationCodeActive);
        logger.info("-> client credentials flow active ? {}", this.oauthClientCredentialsActive);
        if (!this.oauthClientCredentialsActive && !this.oauthAuthorizationCodeActive){
            logger.error("Enable oauth without enabling any oauth features doesn't make sense. You wont be able to authenticate");
        }

        http
                .csrf().disable()
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

    // Disable Oauth ( if asked )
    public ServerHttpSecurity disableOauth(
            ServerHttpSecurity http ) {

        logger.info("oauth2 disabled");
        logger.info("Basic auth enabled");
        http
                .csrf().disable()
                .authorizeExchange().anyExchange()
                .authenticated()
                .and()
                .httpBasic();
                //.permitAll();

        return http;
    }


    /*
    Create a user service for basic auth when oauth is disabled
     */
    @Bean
    @ConditionalOnProperty(name="feature-flipping.authent.oauth.enabled", havingValue = "false" )
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}user")
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("ADMIN")
                .build();
        return new MapReactiveUserDetailsService(user, admin);
    }


    // authorize users by token ( jwt )
    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http ){
        //TODO Add the filter
        // https://github.com/bezkoder/spring-boot-spring-security-jwt-authentication

        // Whatever is the authentication method, we need to authorize /actguatorgw
        http
                .authorizeExchange()
                .pathMatchers("/actuatorgw/**")
                .permitAll()
                .and();

        // Then optimize with the target auth method
        return ( this.needEnableOauth ? this.enableOauth(http): this.disableOauth(http) ).build();

    }


}

