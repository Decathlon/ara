package com.decathlon.ara.security.configuration;

import com.decathlon.ara.scenario.cucumber.resource.CucumberResource;
import com.decathlon.ara.scenario.cypress.resource.CypressResource;
import com.decathlon.ara.scenario.generic.resource.GenericResource;
import com.decathlon.ara.scenario.postman.resource.PostmanResource;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.security.service.login.OAuth2UserLoginService;
import com.decathlon.ara.security.service.login.OidcUserLoginService;
import com.decathlon.ara.web.rest.*;
import com.decathlon.ara.web.rest.authentication.AuthenticationResource;
import com.decathlon.ara.web.rest.authentication.UserResource;
import com.decathlon.ara.web.rest.util.RestConstants;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    private static final String PROJECT_DATA_FETCH_PERMISSION = "@projectData.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).FETCH)";
    private static final String PROJECT_DATA_ALTER_PERMISSION = "@projectData.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).ALTER)";
    private static final String PROJECT_SETTINGS_FETCH_PERMISSION = "@projectSettings.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).FETCH)";
    private static final String PROJECT_SETTINGS_ALTER_PERMISSION = "@projectSettings.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).ALTER)";
    private static final String PROJECT_INSTANCE_FETCH_PERMISSION = "@projectInstance.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).FETCH)";
    private static final String PROJECT_INSTANCE_ALTER_PERMISSION = "@projectInstance.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).ALTER)";

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

    private final OidcUserLoginService oidcUserLoginService;

    private final OAuth2UserLoginService oauth2UserLoginService;

    private static final String SUPER_ADMIN_PROFILE_AUTHORITY = AuthorityService.AUTHORITY_USER_PROFILE_PREFIX + UserAccountProfile.SUPER_ADMIN.name();

    private static final String[] PROJECT_DEMO_PATHS = {DemoResource.PATHS, ProjectResource.DEMO_PATHS};

    public SecurityConfiguration(
            OidcUserLoginService oidcUserLoginService,
            OAuth2UserLoginService oauth2UserLoginService
    ) {
        this.oidcUserLoginService = oidcUserLoginService;
        this.oauth2UserLoginService = oauth2UserLoginService;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Whatever is the authentication method, we need to authorize /actguatorgw
        String redirectUrl = String.format("%s?spring_redirect=true", this.clientBaseUrl);
        http
                .csrf().disable() //NOSONAR
                .authorizeRequests() //NOSONAR
                .antMatchers(AuthenticationResource.PATHS, RestConstants.ACTUATOR_PATHS).permitAll()
                // user
                .antMatchers(HttpMethod.GET, UserResource.PATHS).authenticated()

                // projects > demo
                .antMatchers(PROJECT_DEMO_PATHS).authenticated()

                // projects [data] > execution
                .antMatchers(HttpMethod.GET, ExecutionResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ExecutionResource.FILTER_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ExecutionResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, ExecutionResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > executed scenario
                .antMatchers(HttpMethod.GET, ExecutedScenarioResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ExecutedScenarioResource.HISTORY_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                // projects [data] > error
                .antMatchers(HttpMethod.GET, ErrorResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ErrorResource.MATCHING_PATH).access(PROJECT_DATA_FETCH_PERMISSION)

                // projects [data] > scenario
                .antMatchers(HttpMethod.GET, ScenarioResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                // projects [data] > scenario > cucumber
                .antMatchers(HttpMethod.POST, CucumberResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > scenario > postman
                .antMatchers(HttpMethod.POST, PostmanResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > scenario > cypress
                .antMatchers(HttpMethod.POST, CypressResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > scenario > generic
                .antMatchers(HttpMethod.POST, GenericResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)

                // projects [data] > functionality
                .antMatchers(HttpMethod.GET, FunctionalityResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.OPTIONS, FunctionalityResource.EXPORT_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, FunctionalityResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, FunctionalityResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, FunctionalityResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)

                // projects [data] > problem
                .antMatchers(HttpMethod.GET, ProblemResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ProblemResource.FILTER_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ProblemResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, ProblemResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, ProblemResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > problem patterns
                .antMatchers(HttpMethod.GET, ProblemPatternResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.PUT, ProblemPatternResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, ProblemPatternResource.PATHS).access(PROJECT_DATA_ALTER_PERMISSION)

                // projects [data] > template
                .antMatchers(HttpMethod.GET, TemplateResource.PATHS).access(PROJECT_DATA_FETCH_PERMISSION)

                // projects [settings]
                .antMatchers(HttpMethod.GET, SettingResource.PATHS).access(PROJECT_SETTINGS_FETCH_PERMISSION)
                .antMatchers(HttpMethod.PUT, SettingResource.PATHS).access(PROJECT_SETTINGS_ALTER_PERMISSION)

                // projects [instance] > purge
                .antMatchers(HttpMethod.DELETE, PurgeResource.FORCE_PATH).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > communication
                .antMatchers(HttpMethod.GET, CommunicationResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.PUT, CommunicationResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > country
                .antMatchers(HttpMethod.GET, CountryResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, CountryResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, CountryResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, CountryResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > cycle definition
                .antMatchers(HttpMethod.GET, CycleDefinitionResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, CycleDefinitionResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, CycleDefinitionResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, CycleDefinitionResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > root cause
                .antMatchers(HttpMethod.GET, RootCauseResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, RootCauseResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, RootCauseResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, RootCauseResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > severity
                .antMatchers(HttpMethod.GET, SeverityResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, SeverityResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, SeverityResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, SeverityResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > team
                .antMatchers(HttpMethod.GET, TeamResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, TeamResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, TeamResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, TeamResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > source
                .antMatchers(HttpMethod.GET, SourceResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, SourceResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, SourceResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, SourceResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > type
                .antMatchers(HttpMethod.GET, TypeResource.PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, TypeResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, TypeResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, TypeResource.PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)

                // projects [instance]
                .antMatchers(HttpMethod.GET, ProjectResource.PATH).authenticated()
                .antMatchers(HttpMethod.POST, ProjectResource.PATH).authenticated()
                .antMatchers(HttpMethod.PUT, ProjectResource.CODE_PATH).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, ProjectResource.CODE_PATH).access(PROJECT_INSTANCE_ALTER_PERMISSION)

                // feature flipping
                .antMatchers(HttpMethod.GET, FeatureResource.PATHS).authenticated()
                .antMatchers(HttpMethod.PATCH, FeatureResource.PATHS).hasAuthority(SUPER_ADMIN_PROFILE_AUTHORITY)
                .antMatchers(HttpMethod.DELETE, FeatureResource.PATHS).hasAuthority(SUPER_ADMIN_PROFILE_AUTHORITY)

                .anyRequest().denyAll()
                .and()
                .logout()
                .logoutUrl(this.logoutProcessingUrl) // logout entrypoint
                .invalidateHttpSession(true)
                .clearAuthentication(true).logoutSuccessUrl(redirectUrl).deleteCookies("JSESSIONID").permitAll()
                .and()
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()).userService(oauth2UserService()))
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

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return oauth2UserLoginService::manageUserLoginRequest;
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return oidcUserLoginService::manageUserLoginRequest;
    }

    private boolean isResourceServerConfiguration() {
        return Strings.isNotBlank(resourceIssuerUri) || Strings.isNotBlank(resourceJwkSetUri);
    }

}
