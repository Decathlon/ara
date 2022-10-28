package com.decathlon.ara.oauth2devserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class DefaultSecurityConfiguration {

    @Value("${provider.url}")
    private String providerUrl;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/oauth2/**", "/user/**", "/actuator/**").permitAll().anyRequest().authenticated()
                .and().formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var uds = new InMemoryUserDetailsManager();

        var scopedUser1 = User.withDefaultPasswordEncoder().username("user1").password("user").roles("USER").build();
        var scopedUser2 = User.withDefaultPasswordEncoder().username("user2").password("user").roles("USER").build();
        var auditor = User.withDefaultPasswordEncoder().username("audit").password("audit").roles("AUDIT").build();
        var superAdmin = User.withDefaultPasswordEncoder().username("admin").password("admin").roles("ADMIN").build();

        uds.createUser(scopedUser1);
        uds.createUser(scopedUser2);
        uds.createUser(auditor);
        uds.createUser(superAdmin);

        return uds;
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
                Authentication principal = context.getPrincipal();
                context.getClaims().claim(
                        StandardClaimNames.PICTURE,
                        "%s/user/%s.png".formatted(providerUrl, principal.getName())
                );
                context.getClaims().claim(
                        StandardClaimNames.EMAIL,
                        "%s@oauth2-dev-server.localhost".formatted(principal.getName())
                );
            }
        };
    }

}
