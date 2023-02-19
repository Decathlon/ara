package com.decathlon.ara.security.configuration;

import com.decathlon.ara.security.service.login.OAuth2UserLoginService;
import com.decathlon.ara.security.service.login.OidcUserLoginService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.decathlon.ara.scenario.cucumber.resource.CucumberResource.CUCUMBER_SCENARIO_ALL_API_PATHS;
import static com.decathlon.ara.scenario.cypress.resource.CypressResource.CYPRESS_SCENARIO_ALL_API_PATHS;
import static com.decathlon.ara.scenario.generic.resource.GenericResource.GENERIC_SCENARIO_ALL_API_PATHS;
import static com.decathlon.ara.scenario.postman.resource.PostmanResource.POSTMAN_SCENARIO_ALL_API_PATHS;
import static com.decathlon.ara.security.mapper.AuthorityMapper.AUTHORITY_USER_PROFILE_AUDITOR;
import static com.decathlon.ara.security.mapper.AuthorityMapper.AUTHORITY_USER_PROFILE_SUPER_ADMIN;
import static com.decathlon.ara.web.rest.CommunicationResource.COMMUNICATION_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.CountryResource.COUNTRY_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.CycleDefinitionResource.CYCLE_DEFINITION_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.DemoResource.DEMO_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.ErrorResource.ERROR_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.ErrorResource.ERROR_MATCHING_API_PATH;
import static com.decathlon.ara.web.rest.ExecutedScenarioResource.EXECUTED_SCENARIO_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.ExecutedScenarioResource.EXECUTED_SCENARIO_HISTORY_API_PATH;
import static com.decathlon.ara.web.rest.ExecutionResource.EXECUTION_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.ExecutionResource.EXECUTION_FILTER_API_PATH;
import static com.decathlon.ara.web.rest.FeatureResource.FEATURE_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.FunctionalityResource.FUNCTIONALITY_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.FunctionalityResource.FUNCTIONALITY_EXPORT_API_PATH;
import static com.decathlon.ara.web.rest.ProblemPatternResource.PROBLEM_PATTERN_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.ProblemResource.PROBLEM_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.ProblemResource.PROBLEM_FILTER_API_PATH;
import static com.decathlon.ara.web.rest.ProjectResource.*;
import static com.decathlon.ara.web.rest.PurgeResource.PURGE_FORCE_API_PATH;
import static com.decathlon.ara.web.rest.RootCauseResource.ROOT_CAUSE_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.ScenarioResource.SCENARIO_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.SettingResource.SETTING_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.SeverityResource.SEVERITY_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.SourceResource.SOURCE_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.TeamResource.TEAM_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.TemplateResource.TEMPLATE_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.TypeResource.TYPE_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.authentication.AuthenticationResource.OAUTH_ALL_API_PATHS;
import static com.decathlon.ara.web.rest.member.user.UserAccountResource.*;
import static com.decathlon.ara.web.rest.member.user.UserGroupResource.*;

@Configuration
public class SecurityConfiguration {

    private static final String PROJECT_DATA_FETCH_PERMISSION = "@projectData.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).FETCH)";
    private static final String PROJECT_DATA_ALTER_PERMISSION = "@projectData.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).ALTER)";
    private static final String PROJECT_SETTINGS_FETCH_PERMISSION = "@projectSettings.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).FETCH)";
    private static final String PROJECT_SETTINGS_ALTER_PERMISSION = "@projectSettings.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).ALTER)";
    private static final String PROJECT_INSTANCE_FETCH_PERMISSION = "@projectInstance.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).FETCH)";
    private static final String PROJECT_INSTANCE_ALTER_PERMISSION = "@projectInstance.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).ALTER)";
    private static final String PROJECT_SCOPE_FETCH_PERMISSION = "@projectScope.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).FETCH)";
    private static final String PROJECT_SCOPE_ALTER_PERMISSION = "@projectScope.isEnabled(#projectCode, T(com.decathlon.ara.security.dto.permission.ResourcePermission).ALTER)";

    private static final String MEMBER_USER_GROUP_MANAGEMENT_PERMISSION = "@userSessionService.canManageGroup(#groupId)";

    private static final String[] ALL_DEMO_RELATED_API_PATHS = {DEMO_ALL_API_PATHS, PROJECT_DEMO_ALL_API_PATHS};

    private static final String ACTUATOR_ALL_API_PATHS = "/actuator/**";

    public static final String BASE_API_PATH = "/api";

    public static final String MEMBER_BASE_API_PATH = BASE_API_PATH + "/member";
    public static final String MEMBER_USER_BASE_API_PATH = MEMBER_BASE_API_PATH + "/user";

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
                .antMatchers(OAUTH_ALL_API_PATHS, ACTUATOR_ALL_API_PATHS).permitAll()
                // member > user > accounts
                .antMatchers(HttpMethod.GET, MEMBER_USER_ACCOUNT_ALL_ACCOUNTS_API_PATH).hasAnyAuthority(AUTHORITY_USER_PROFILE_SUPER_ADMIN, AUTHORITY_USER_PROFILE_AUDITOR)
                .antMatchers(HttpMethod.GET, MEMBER_USER_ACCOUNT_SCOPED_ACCOUNTS_API_PATH).authenticated()
                .antMatchers(HttpMethod.GET, MEMBER_USER_ACCOUNT_SCOPED_ACCOUNTS_BY_PROJECT_API_PATH).access(PROJECT_SCOPE_FETCH_PERMISSION)
                // member > user > accounts > scopes
                .antMatchers(HttpMethod.PUT, MEMBER_USER_ACCOUNT_PROJECT_SCOPE_API_PATH).access(PROJECT_SCOPE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, MEMBER_USER_ACCOUNT_PROJECT_SCOPE_API_PATH).access(PROJECT_SCOPE_ALTER_PERMISSION)
                // member > user > accounts > profile
                .antMatchers(HttpMethod.PUT, MEMBER_USER_ACCOUNT_PROFILE_API_PATH).hasAuthority(AUTHORITY_USER_PROFILE_SUPER_ADMIN)
                // member > user > accounts > current > default project
                .antMatchers(HttpMethod.DELETE, MEMBER_USER_ACCOUNT_CLEAR_DEFAULT_PROJECT_API_PATH).authenticated()
                .antMatchers(HttpMethod.PUT, MEMBER_USER_ACCOUNT_UPDATE_DEFAULT_PROJECT_BY_CODE_API_PATH).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                // member > user > accounts > current
                .antMatchers(HttpMethod.GET, MEMBER_USER_ACCOUNT_CURRENT_ACCOUNT_API_PATH).authenticated()

                // member > group
                .antMatchers(HttpMethod.GET, MEMBER_USER_GROUP_ALL_GROUPS_API_PATH).authenticated()
                .antMatchers(HttpMethod.GET, MEMBER_USER_GROUPS_CONTAINING_USER_FROM_LOGIN_API_PATH).authenticated()
                .antMatchers(HttpMethod.GET, MEMBER_USER_GROUP_MANAGED_GROUPS_FROM_USER_LOGIN_API_PATH).authenticated()
                .antMatchers(HttpMethod.GET, MEMBER_USER_GROUPS_CONTAINING_CURRENT_USER_API_PATH).authenticated()
                .antMatchers(HttpMethod.GET, MEMBER_USER_GROUP_MANAGED_GROUPS_BY_CURRENT_USER_API_PATH).authenticated()
                .antMatchers(HttpMethod.POST, MEMBER_USER_GROUP_BASE_API_PATH).authenticated()
                .antMatchers(HttpMethod.PUT, MEMBER_USER_GROUP_BY_ID_API_PATH).access(MEMBER_USER_GROUP_MANAGEMENT_PERMISSION)
                .antMatchers(HttpMethod.DELETE, MEMBER_USER_GROUP_BY_ID_API_PATH).access(MEMBER_USER_GROUP_MANAGEMENT_PERMISSION)

                // projects > demo
                .antMatchers(ALL_DEMO_RELATED_API_PATHS).authenticated()

                // projects [data] > execution
                .antMatchers(HttpMethod.GET, EXECUTION_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, EXECUTION_FILTER_API_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, EXECUTION_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, EXECUTION_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > executed scenario
                .antMatchers(HttpMethod.GET, EXECUTED_SCENARIO_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, EXECUTED_SCENARIO_HISTORY_API_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                // projects [data] > error
                .antMatchers(HttpMethod.GET, ERROR_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ERROR_MATCHING_API_PATH).access(PROJECT_DATA_FETCH_PERMISSION)

                // projects [data] > scenario
                .antMatchers(HttpMethod.GET, SCENARIO_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                // projects [data] > scenario > cucumber
                .antMatchers(HttpMethod.POST, CUCUMBER_SCENARIO_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > scenario > postman
                .antMatchers(HttpMethod.POST, POSTMAN_SCENARIO_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > scenario > cypress
                .antMatchers(HttpMethod.POST, CYPRESS_SCENARIO_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > scenario > generic
                .antMatchers(HttpMethod.POST, GENERIC_SCENARIO_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)

                // projects [data] > functionality
                .antMatchers(HttpMethod.GET, FUNCTIONALITY_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.OPTIONS, FUNCTIONALITY_EXPORT_API_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, FUNCTIONALITY_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, FUNCTIONALITY_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, FUNCTIONALITY_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)

                // projects [data] > problem
                .antMatchers(HttpMethod.GET, PROBLEM_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, PROBLEM_FILTER_API_PATH).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, PROBLEM_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, PROBLEM_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, PROBLEM_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                // projects [data] > problem patterns
                .antMatchers(HttpMethod.GET, PROBLEM_PATTERN_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)
                .antMatchers(HttpMethod.PUT, PROBLEM_PATTERN_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, PROBLEM_PATTERN_ALL_API_PATHS).access(PROJECT_DATA_ALTER_PERMISSION)

                // projects [data] > template
                .antMatchers(HttpMethod.GET, TEMPLATE_ALL_API_PATHS).access(PROJECT_DATA_FETCH_PERMISSION)

                // projects [settings]
                .antMatchers(HttpMethod.GET, SETTING_ALL_API_PATHS).access(PROJECT_SETTINGS_FETCH_PERMISSION)
                .antMatchers(HttpMethod.PUT, SETTING_ALL_API_PATHS).access(PROJECT_SETTINGS_ALTER_PERMISSION)

                // projects [instance] > purge
                .antMatchers(HttpMethod.DELETE, PURGE_FORCE_API_PATH).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > communication
                .antMatchers(HttpMethod.GET, COMMUNICATION_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.PUT, COMMUNICATION_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > country
                .antMatchers(HttpMethod.GET, COUNTRY_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, COUNTRY_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, COUNTRY_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, COUNTRY_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > cycle definition
                .antMatchers(HttpMethod.GET, CYCLE_DEFINITION_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, CYCLE_DEFINITION_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, CYCLE_DEFINITION_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, CYCLE_DEFINITION_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > root cause
                .antMatchers(HttpMethod.GET, ROOT_CAUSE_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, ROOT_CAUSE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, ROOT_CAUSE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, ROOT_CAUSE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > severity
                .antMatchers(HttpMethod.GET, SEVERITY_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, SEVERITY_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, SEVERITY_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, SEVERITY_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > team
                .antMatchers(HttpMethod.GET, TEAM_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, TEAM_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, TEAM_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, TEAM_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > source
                .antMatchers(HttpMethod.GET, SOURCE_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, SOURCE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, SOURCE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, SOURCE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                // projects [instance] > type
                .antMatchers(HttpMethod.GET, TYPE_ALL_API_PATHS).access(PROJECT_INSTANCE_FETCH_PERMISSION)
                .antMatchers(HttpMethod.POST, TYPE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.PUT, TYPE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, TYPE_ALL_API_PATHS).access(PROJECT_INSTANCE_ALTER_PERMISSION)

                // projects [instance]
                .antMatchers(HttpMethod.GET, PROJECT_BASE_API_PATH).authenticated()
                .antMatchers(HttpMethod.POST, PROJECT_BASE_API_PATH).authenticated()
                .antMatchers(HttpMethod.PUT, PROJECT_CODE_BASE_API_PATH).access(PROJECT_INSTANCE_ALTER_PERMISSION)
                .antMatchers(HttpMethod.DELETE, PROJECT_CODE_BASE_API_PATH).access(PROJECT_INSTANCE_ALTER_PERMISSION)

                // feature flipping
                .antMatchers(HttpMethod.GET, FEATURE_ALL_API_PATHS).authenticated()
                .antMatchers(HttpMethod.PATCH, FEATURE_ALL_API_PATHS).hasAuthority(AUTHORITY_USER_PROFILE_SUPER_ADMIN)
                .antMatchers(HttpMethod.DELETE, FEATURE_ALL_API_PATHS).hasAuthority(AUTHORITY_USER_PROFILE_SUPER_ADMIN)

                .anyRequest().denyAll()
                .and()
                .logout()
                .logoutUrl(this.logoutProcessingUrl) // logout entrypoint
                .invalidateHttpSession(true)
                .clearAuthentication(true).logoutSuccessUrl(redirectUrl).deleteCookies("JSESSIONID").permitAll()
                .and()
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserLoginService::manageUserLoginRequest).userService(oauth2UserLoginService::manageUserLoginRequest))
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

    private boolean isResourceServerConfiguration() {
        return Strings.isNotBlank(resourceIssuerUri) || Strings.isNotBlank(resourceJwkSetUri);
    }

}
