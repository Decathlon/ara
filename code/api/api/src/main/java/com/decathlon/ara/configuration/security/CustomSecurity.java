package com.decathlon.ara.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class CustomSecurity {

    @Value("${base-application-path}")
    private String baseApplicationPath;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http ) throws Exception {
        // Whatever is the authentication method, we need to authorize /actguatorgw
        String redirectUrl= String.format("%s/?spring_redirect=true", this.baseApplicationPath);
        http
                .csrf().disable() //NOSONAR
                .authorizeRequests() //NOSONAR
                    .antMatchers("/oauth/**", "/actuator/**").permitAll()
                    .anyRequest().authenticated()
                .and()
                .logout().invalidateHttpSession(true)
                .clearAuthentication(true).logoutSuccessUrl(redirectUrl).deleteCookies("JSESSIONID").permitAll()
                .and()
                .oauth2Login().loginPage("/").defaultSuccessUrl(redirectUrl, true);

        return http.build();
    }


}
