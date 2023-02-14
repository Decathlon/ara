package com.decathlon.ara.configuration.security;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class CustomSecurity {

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

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Whatever is the authentication method, we need to authorize /actguatorgw
        String redirectUrl = String.format("%s?spring_redirect=true", this.clientBaseUrl);
        http
                .csrf().disable() //NOSONAR
                .authorizeRequests() //NOSONAR
                .antMatchers("/oauth/**", "/actuator/**", "/swagger-ui/**","/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl(this.logoutProcessingUrl) // logout entrypoint
                .invalidateHttpSession(true)
                .clearAuthentication(true).logoutSuccessUrl(redirectUrl).deleteCookies("JSESSIONID").permitAll()
                .and()
                .oauth2Login()
                .loginProcessingUrl(this.loginProcessingUrl + "/*") // path to redirect to from auth server
                .loginPage(String.format("%s/%s", this.clientBaseUrl, "login")) // standard spring redirection for protected resources
                .defaultSuccessUrl(redirectUrl, true) // once logged in, redirect to
                .authorizationEndpoint().baseUri(this.loginStartingUrl); // entrypoint to initialize oauth processing

        if (isResourceServerConfiguration()) {
            http.oauth2ResourceServer().jwt();
        }
        return http.build();
    }

    private boolean isResourceServerConfiguration() {
        return Strings.isNotBlank(resourceIssuerUri) || Strings.isNotBlank(resourceJwkSetUri);
    }

}
