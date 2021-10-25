package com.decathlon.ara.oauth2devserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class DefaultSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/oauth2/**", "/actuator/**").permitAll()
                .anyRequest().authenticated().and()
                .formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var uds = new InMemoryUserDetailsManager();

        var user = User.withDefaultPasswordEncoder()
                                    .username("user")
                                    .password("user")
                                    .roles("USER").build();

        var admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin")
                .roles("ADMIN").build();

        uds.createUser(user);
        uds.createUser(admin);

        return uds;
    }

}
