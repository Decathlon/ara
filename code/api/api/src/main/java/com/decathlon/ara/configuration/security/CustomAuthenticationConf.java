package com.decathlon.ara.configuration.security;

import com.decathlon.ara.service.AuthorizationUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class CustomAuthenticationConf {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationConf.class);

    // Authentication Manager Stuff ( ~ retrieve user data from name )

    @Bean("customAuthentUserDetailsService")
    public AuthenticationUserDetailsService userDetailsServiceWrapper() {
        return new AuthorizationUserDetailsService();
    }

    @Bean(name = "preAuthProvider")
    PreAuthenticatedAuthenticationProvider preAuthAuthProvider(
            @Qualifier("customAuthentUserDetailsService") AuthenticationUserDetailsService authUserDetailsService) {

        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(authUserDetailsService);
        return provider;
    }

    @Bean(name= "customAuthManager")
    public AuthenticationManager authenticationManager(
            @Qualifier("preAuthProvider") PreAuthenticatedAuthenticationProvider authProvider
    ) {
        return new ProviderManager(List.of(authProvider));
    }


    // Authentication filter ( ~ retrieve name from request )

    @Bean("customHeaderAuthFilter")
    public RequestHeaderAuthenticationFilter getHeaderAuthFilter(
            @Qualifier("customAuthManager") AuthenticationManager authManager
    ){
        RequestHeaderAuthenticationFilter filter= new RequestHeaderAuthenticationFilter();
        filter.setRequiresAuthenticationRequestMatcher(new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest httpServletRequest) {
                return new AntPathRequestMatcher("/api/**").matches(httpServletRequest) ||
                        new AntPathRequestMatcher("/demo-files/**").matches(httpServletRequest);
            }
        });
        filter.setPrincipalRequestHeader("GW-DOWNSTREAM-USERNAME");
        filter.setExceptionIfHeaderMissing(true);
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
        /*
        filter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                logger.error("Authentication failed");
            }
        });*/
        filter.setAuthenticationManager(authManager);
        return filter;
    }


    @Bean
    public DefaultSecurityFilterChain enableAuthentication(
            HttpSecurity http,
            @Qualifier("customHeaderAuthFilter") RequestHeaderAuthenticationFilter authFilter) throws Exception {

        logger.info("authentication enabled");
        http
                .csrf().disable()//NOSONAR
                .authorizeRequests()
                .antMatchers("/api/**", "/demo-files/**")
                //.anyRequest()
                .authenticated();

        /*
        http
                .csrf().disable().cors().disable()
                .authorizeRequests().anyRequest()
                .permitAll();
        */
        http.addFilterBefore(authFilter, RequestHeaderAuthenticationFilter.class);
        return http.build();

    }



}
