package com.decathlon.ara.configuration.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriUtils;

import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.domain.enumeration.Permission;
import com.decathlon.ara.domain.enumeration.UserSecurityRole;
import com.decathlon.ara.service.security.SecurityService;
import com.decathlon.ara.web.rest.CommunicationResource;
import com.decathlon.ara.web.rest.CountryResource;
import com.decathlon.ara.web.rest.CycleDefinitionResource;
import com.decathlon.ara.web.rest.DemoResource;
import com.decathlon.ara.web.rest.ErrorResource;
import com.decathlon.ara.web.rest.ExecutedScenarioResource;
import com.decathlon.ara.web.rest.ExecutionResource;
import com.decathlon.ara.web.rest.FeatureResource;
import com.decathlon.ara.web.rest.FunctionalityResource;
import com.decathlon.ara.web.rest.GroupMemberResource;
import com.decathlon.ara.web.rest.GroupResource;
import com.decathlon.ara.web.rest.ProblemPatternResource;
import com.decathlon.ara.web.rest.ProblemResource;
import com.decathlon.ara.web.rest.ProjectAdministrationResource;
import com.decathlon.ara.web.rest.ProjectGroupMemberResource;
import com.decathlon.ara.web.rest.ProjectResource;
import com.decathlon.ara.web.rest.ProjectUserMemberResource;
import com.decathlon.ara.web.rest.PurgeResource;
import com.decathlon.ara.web.rest.RootCauseResource;
import com.decathlon.ara.web.rest.ScenarioResource;
import com.decathlon.ara.web.rest.SettingResource;
import com.decathlon.ara.web.rest.SourceResource;
import com.decathlon.ara.web.rest.TeamResource;
import com.decathlon.ara.web.rest.TemplateResource;
import com.decathlon.ara.web.rest.TypeResource;
import com.decathlon.ara.web.rest.audits.AuditingResource;
import com.decathlon.ara.web.rest.authentication.UserResource;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.nimbusds.jose.jwk.RSAKey;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;

/**
 * To test security, each api is called with different configuration of the Authentication, and then the status code is checked.
 * Because spring check security before checking uri existence and to avoid mocking each controller's used services, no controller are loaded for the test.
 * In this context, when security pass, the HTTP status code is 404. The problem of this way to test, is that if the uri doesn't exists and have no security configuration, we have a 404 too.
 * To solve this problem a check based on classes annotation is done to check that the currently tested uri exists in the given classes
 * @author z15lross
 */
@WebMvcTest
@ContextConfiguration(classes = { CustomSecurity.class }, initializers = CustomSecurityTest.Initializer.class)
class CustomSecurityTest {

    private static final Pattern STATE_EXTRACTOR_PATTERN = Pattern.compile("state=([^&]+)");
    private static final Pattern NONCE_EXTRACTOR_PATTERN = Pattern.compile("nonce=([^&]+)");

    @RegisterExtension
    private static WireMockExtension wireMockExtensionOidc = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig().dynamicPort().extensions(new ResponseTemplateTransformer(false)).usingFilesUnderClasspath("wiremock/oidc")).build();

    @RegisterExtension
    private static WireMockExtension wireMockExtensionOauth2 = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig().dynamicPort().extensions(new ResponseTemplateTransformer(false)).usingFilesUnderClasspath("wiremock/oauth2")).build();

    private static KeyPair keyPair = generateRsaKey();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @MockBean
    private SecurityService securityService;

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(Map.of("spring.security.oauth2.client.provider.spring.issuer-uri", wireMockExtensionOidc.baseUrl(),
                    "spring.security.oauth2.client.registration.ara-client-oauth2.provider", "ara-provider-oauth2",
                    "spring.security.oauth2.client.registration.ara-client-oauth2.client-name", "ara-client-oauth2",
                    "spring.security.oauth2.client.registration.ara-client-oauth2.client-id", "ara-client",
                    "spring.security.oauth2.client.registration.ara-client-oauth2.client-secret", "ara-client",
                    "spring.security.oauth2.client.registration.ara-client-oauth2.authorization-grant-type", "authorization_code",
                    "spring.security.oauth2.client.registration.ara-client-oauth2.redirect-uri", "${ara.clientBaseUrl}/${ara.loginProcessingUrl}/{registrationId}",
                    "spring.security.oauth2.client.registration.ara-client-oauth2.scope", "profile",
                    "spring.security.oauth2.client.provider.ara-provider-oauth2.issuer-uri", wireMockExtensionOauth2.baseUrl(),
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri", wireMockExtensionOauth2.baseUrl())).applyTo(applicationContext);

        }
    }

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilter(springSecurityFilterChain).build();
    }

    @Test
    void shouldInitUserAndReturnCustomPrincipalWhenLoginWithOauth2Provider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = login(session, false, "simple_user", "user1", null, Collections.emptyList());
        Authentication authentication = getAuthenticationfromSession(session);
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals("com.decathlon.ara.configuration.security.CustomSecurity$AraOauth2User", principal.getClass().getName());
        Assertions.assertEquals("user1@email.com", principal.getAttribute("email"));
        Assertions.assertTrue(authentication.getAuthorities().isEmpty());
        Mockito.verify(securityService).initUser(user.getName(), user.getIssuer());
        Mockito.verify(securityService).getUserRoles(user.getId());
    }

    @Test
    void shouldInitUserAndReturnCustomPrincipalWhenLoginWithOidcProvider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = login(session, true, "simple_user", "user1", null, Collections.emptyList());
        Authentication authentication = getAuthenticationfromSession(session);
        OidcUser principal = (OidcUser) authentication.getPrincipal();
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals("com.decathlon.ara.configuration.security.CustomSecurity$AraOidcUser", principal.getClass().getName());
        Assertions.assertEquals("user1@email.com", principal.getEmail());
        Assertions.assertTrue(authentication.getAuthorities().isEmpty());
        Mockito.verify(securityService).initUser(user.getName(), user.getIssuer());
        Mockito.verify(securityService).getUserRoles(user.getId());
    }

    @Test
    void shouldInitUserWhenUsingAppAsResourceServer() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = machineToMachineCall(session, Collections.emptyList());
        Authentication authentication = getAuthenticationfromSession(session);
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertTrue(authentication.getAuthorities().isEmpty());
        Mockito.verify(securityService).initUser(user.getName(), user.getIssuer());
        Mockito.verify(securityService).getUserRoles(user.getId());
    }

    @Test
    void authenticationShouldHaveAllUserInfoAttributeWhenLoginWithOauth2Provider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<String, String> attributes = Map.of("email_verified", "true", "birthdate", "01/01/1971", "customAttr", "value");
        User user = login(session, false, "user_with_attributes", "user1", attributes, Collections.emptyList());
        Authentication authentication = getAuthenticationfromSession(session);
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals("user1@email.com", principal.getAttribute("email"));
        attributes.forEach((key, value) -> Assertions.assertEquals(value, principal.getAttribute(key)));
    }

    @Test
    void principalShouldHaveAllIdTokenClaimsWhenLoginWithOidcProvider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<String, String> attributes = Map.of("email_verified", "true", "birthdate", "01/01/1971", "customAttr", "value");
        User user = login(session, true, "user_with_attributes", "user1", attributes, Collections.emptyList());
        Authentication authentication = getAuthenticationfromSession(session);
        OidcUser principal = (OidcUser) authentication.getPrincipal();
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals("user1@email.com", principal.getEmail());
        Assertions.assertEquals(true, principal.getEmailVerified());
        Assertions.assertEquals("01/01/1971", principal.getBirthdate());
        Assertions.assertEquals("value", principal.getAttribute("customAttr"));
    }

    @Test
    void authenticationShouldHaveAuthoritiesGetFromDatabaseWhenLoginWithOauth2Provider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<UserSecurityRole> userRoles = List.of(UserSecurityRole.ADMIN, UserSecurityRole.PROJECT_OR_GROUP_CREATOR);
        User user = login(session, false, "user_with_authorities", "user1", null, userRoles);
        Authentication authentication = getAuthenticationfromSession(session);
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals(userRoles.size(), authentication.getAuthorities().size());
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        userRoles.forEach(userRole -> Assertions.assertTrue(authorities.contains("ROLE_" + userRole.name())));
    }

    @Test
    void authenticationShouldHaveAuthoritiesGetFromDatabaseWhenLoginWithOidcProvider() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<UserSecurityRole> userRoles = List.of(UserSecurityRole.ADMIN, UserSecurityRole.PROJECT_OR_GROUP_CREATOR);
        User user = login(session, true, "user_with_authorities", "user1", null, userRoles);
        Authentication authentication = getAuthenticationfromSession(session);
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals(userRoles.size(), authentication.getAuthorities().size());
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        userRoles.forEach(userRole -> Assertions.assertTrue(authorities.contains("ROLE_" + userRole.name())));
    }

    @Test
    void authenticationShouldHaveAuthoritiesGetFromDatabaseWhenUsingAppAsResourceServer() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<UserSecurityRole> userRoles = List.of(UserSecurityRole.ADMIN, UserSecurityRole.PROJECT_OR_GROUP_CREATOR);
        User user = machineToMachineCall(session, userRoles);
        Authentication authentication = getAuthenticationfromSession(session);
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals(user.getId(), authentication.getName());
        Assertions.assertEquals(userRoles.size(), authentication.getAuthorities().size());
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        userRoles.forEach(userRole -> Assertions.assertTrue(authorities.contains("ROLE_" + userRole.name())));
    }

    private static Stream<Arguments> provideUnauthenticatedTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/admin/projects", ProjectAdministrationResource.class),
                Arguments.of(HttpMethod.GET, "/api/auditing/users-roles", AuditingResource.class),
                Arguments.of(HttpMethod.POST, "/api/demo", DemoResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/demo", DemoResource.class),
                Arguments.of(HttpMethod.GET, "/api/users", UserResource.class),
                Arguments.of(HttpMethod.GET, "/api/users/toto", UserResource.class),
                Arguments.of(HttpMethod.GET, "/api/users/current/details", UserResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups", GroupResource.class),
                Arguments.of(HttpMethod.POST, "/api/groups", GroupResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups/toto", GroupResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto", GroupResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects", ProjectResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects", ProjectResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto", ProjectResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications", CommunicationResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications/titi", CommunicationResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/communications/titi", CommunicationResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/countries", CountryResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/countries", CountryResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/countries/ok", CountryResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/countries/ok", CountryResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/42", ErrorResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/errors/matching", ErrorResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/distinct/titi", ErrorResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executed-scenarios/history", ExecutedScenarioResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executed-scenarios/42", ExecutedScenarioResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions", ExecutionResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest", ExecutionResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/request-completion", ExecutionResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/quality-status", ExecutionResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest-eligible-versions", ExecutionResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/upload", ExecutionResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42", ExecutionResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/with-successes", ExecutionResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/discard", ExecutionResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/un-discard", ExecutionResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/history", ExecutionResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/42/filtered", ExecutionResource.class),
                Arguments.of(HttpMethod.GET, "/api/features", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/features/titi", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/features/titi/state", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/features/titi/default", FeatureResource.class),
                Arguments.of(HttpMethod.PATCH, "/api/features", FeatureResource.class),
                Arguments.of(HttpMethod.PATCH, "/api/features/titi", FeatureResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/features", FeatureResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/features/titi", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities", FunctionalityResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/functionalities/42", FunctionalityResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities", FunctionalityResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities", FunctionalityResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities/42", FunctionalityResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move", FunctionalityResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move/list", FunctionalityResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/42/scenarios", FunctionalityResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/coverage", FunctionalityResource.class),
                Arguments.of(HttpMethod.OPTIONS, "/api/projects/toto/functionalities/export", FunctionalityResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/export", FunctionalityResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/import", FunctionalityResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups/toto/members", GroupMemberResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups/toto/members/titi", GroupMemberResource.class),
                Arguments.of(HttpMethod.POST, "/api/groups/toto/members", GroupMemberResource.class),
                Arguments.of(HttpMethod.PATCH, "/api/groups/toto/members/titi", GroupMemberResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto/members/titi", GroupMemberResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problem-patterns/42/errors", ProblemPatternResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems", ProblemResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42", ProblemResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42/errors", ProblemResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problems/42", ProblemResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/filter", ProblemResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/append-pattern", ProblemResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42", ProblemResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/pick-up-pattern/41", ProblemResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/close/41", ProblemResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/reopen", ProblemResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/refresh-defect-status", ProblemResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/recompute-first-and-last-seen-date-times", ProblemResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users", ProjectUserMemberResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/users", ProjectUserMemberResource.class),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/purge/force", PurgeResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/root-causes", RootCauseResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/root-causes/42", RootCauseResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/root-causes", RootCauseResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/root-causes/42", RootCauseResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload/toto", ScenarioResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload-postman/toto", ScenarioResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/without-functionalities", ScenarioResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/ignored", ScenarioResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/toto", SettingResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings", SettingResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings/technology", SettingResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/titi/technology/tutu", SettingResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/sources", SourceResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/sources", SourceResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/sources/titi", SourceResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/sources/titi", SourceResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/teams", TeamResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/teams/42", TeamResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/teams", TeamResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/teams/42", TeamResource.class),
                Arguments.of(HttpMethod.GET, "/api/templates/cycle-execution", TemplateResource.class),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/types", TypeResource.class),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/types/titi", TypeResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/types", TypeResource.class),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/types/titi", TypeResource.class));
    }

    @ParameterizedTest
    @MethodSource("provideUnauthenticatedTestArguments")
    void shouldHasUnauthorizedwhenUnAuthenticated(HttpMethod method, String uri, Class<?> controllerClass) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri)).andExpect(MockMvcResultMatchers.status().isUnauthorized());
        assertRequestExists(controllerClass, method, uri);
    }

    private static Stream<Arguments> provideAuthenticatedTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/users", UserResource.class),
                Arguments.of(HttpMethod.GET, "/api/users/current/details", UserResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups", GroupResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups/toto", GroupResource.class),
                Arguments.of(HttpMethod.GET, "/api/projects", ProjectResource.class),
                Arguments.of(HttpMethod.GET, "/api/features", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/features/titi", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/features/titi/state", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/features/titi/default", FeatureResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups/toto/members", GroupMemberResource.class),
                Arguments.of(HttpMethod.GET, "/api/groups/toto/members/titi", GroupMemberResource.class),
                Arguments.of(HttpMethod.GET, "/api/templates/cycle-execution", TemplateResource.class));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticatedTestArguments")
    void shouldHasNotFoundwhenAuthenticated(HttpMethod method, String uri, Class<?> controllerClass) throws Exception {
        TestAuthentication authentication = new TestAuthentication(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri).with(SecurityMockMvcRequestPostProcessors.authentication(authentication))).andExpect(MockMvcResultMatchers.status().isNotFound());
        assertRequestExists(controllerClass, method, uri);
    }

    private static Stream<Arguments> provideAuthenticatedWithUserWithOtherRoleThanTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/admin/projects", ProjectAdministrationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/auditing/users-roles", AuditingResource.class, UserSecurityRole.AUDITING),
                Arguments.of(HttpMethod.POST, "/api/demo", DemoResource.class, UserSecurityRole.PROJECT_OR_GROUP_CREATOR),
                Arguments.of(HttpMethod.DELETE, "/api/demo", DemoResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/users/toto", UserResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/groups", GroupResource.class, UserSecurityRole.PROJECT_OR_GROUP_CREATOR),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto", GroupResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects", ProjectResource.class, UserSecurityRole.PROJECT_OR_GROUP_CREATOR),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto", ProjectResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications", CommunicationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/42", ErrorResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/errors/matching", ErrorResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/distinct/titi", ErrorResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executed-scenarios/history", ExecutedScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executed-scenarios/42", ExecutedScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/request-completion", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/quality-status", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest-eligible-versions", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/upload", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/with-successes", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/discard", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/un-discard", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/history", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/42/filtered", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/features", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/features/titi", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/features", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/features/titi", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move/list", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/42/scenarios", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/coverage", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.OPTIONS, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/import", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/groups/toto/members", GroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problem-patterns/42/errors", ProblemPatternResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42/errors", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/filter", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/append-pattern", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/pick-up-pattern/41", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/close/41", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/reopen", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/refresh-defect-status", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/recompute-first-and-last-seen-date-times", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/purge/force", PurgeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload/toto", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload-postman/toto", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/without-functionalities", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/ignored", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/toto", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings/technology", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/titi/technology/tutu", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticatedWithUserWithOtherRoleThanTestArguments")
    void shouldHasForbiddenwhenAuthenticatedWithUserWithOtherRoleThan(HttpMethod method, String uri, Class<?> controllerClass, UserSecurityRole role) throws Exception {
        TestAuthentication authentication = new TestAuthentication(allRolesExcept(role));
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri).with(SecurityMockMvcRequestPostProcessors.authentication(authentication))).andExpect(MockMvcResultMatchers.status().isForbidden());
        assertRequestExists(controllerClass, method, uri);
    }

    private static Stream<Arguments> provideAuthenticatedWithUserWithRoleTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/admin/projects", ProjectAdministrationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/auditing/users-roles", AuditingResource.class, UserSecurityRole.AUDITING),
                Arguments.of(HttpMethod.POST, "/api/demo", DemoResource.class, UserSecurityRole.PROJECT_OR_GROUP_CREATOR),
                Arguments.of(HttpMethod.DELETE, "/api/demo", DemoResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/users/toto", UserResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/groups", GroupResource.class, UserSecurityRole.PROJECT_OR_GROUP_CREATOR),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto", GroupResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects", ProjectResource.class, UserSecurityRole.PROJECT_OR_GROUP_CREATOR),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto", ProjectResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications", CommunicationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/42", ErrorResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/errors/matching", ErrorResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/distinct/titi", ErrorResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executed-scenarios/history", ExecutedScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executed-scenarios/42", ExecutedScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/request-completion", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/quality-status", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest-eligible-versions", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/upload", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/with-successes", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/discard", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/un-discard", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/history", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/42/filtered", ExecutionResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/features", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/features/titi", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/features", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/features/titi", FeatureResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move/list", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/42/scenarios", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/coverage", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.OPTIONS, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/import", FunctionalityResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/groups/toto/members", GroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problem-patterns/42/errors", ProblemPatternResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42/errors", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/filter", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/append-pattern", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/pick-up-pattern/41", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/close/41", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/reopen", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/refresh-defect-status", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/recompute-first-and-last-seen-date-times", ProblemResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/purge/force", PurgeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload/toto", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload-postman/toto", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/without-functionalities", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/ignored", ScenarioResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/toto", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings/technology", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/titi/technology/tutu", SettingResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticatedWithUserWithRoleTestArguments")
    void shouldHasNotFoundwhenAuthenticatedWithUserWithRole(HttpMethod method, String uri, Class<?> controllerClass, UserSecurityRole role) throws Exception {
        TestAuthentication authentication = new TestAuthentication(roles(role));
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri).with(SecurityMockMvcRequestPostProcessors.authentication(authentication))).andExpect(MockMvcResultMatchers.status().isNotFound());
        assertRequestExists(controllerClass, method, uri);
    }

    private static Stream<Arguments> provideAuthenticatedWithUserWithOtherRoleThanAndProjecMemberWithPermissionOtherThanTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.PUT, "/api/projects/toto", ProjectResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications", CommunicationResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/42", ErrorResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/errors/matching", ErrorResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/distinct/titi", ErrorResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executed-scenarios/history", ExecutedScenarioResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executed-scenarios/42", ExecutedScenarioResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/request-completion", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/quality-status", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest-eligible-versions", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/upload", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/with-successes", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/discard", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/un-discard", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/history", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/42/filtered", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move/list", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/42/scenarios", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/coverage", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.OPTIONS, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/import", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problem-patterns/42/errors", ProblemPatternResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42/errors", ProblemResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/filter", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/append-pattern", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/pick-up-pattern/41", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/close/41", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/reopen", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/refresh-defect-status", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/recompute-first-and-last-seen-date-times", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/purge/force", PurgeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload/toto", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload-postman/toto", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/without-functionalities", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/ignored", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/toto", SettingResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings", SettingResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings/technology", SettingResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/titi/technology/tutu", SettingResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticatedWithUserWithOtherRoleThanAndProjecMemberWithPermissionOtherThanTestArguments")
    void shouldHasForbiddenwhenAuthenticatedWithUserWithOtherRoleThanAndProjectMemberWithPermissionOtherThan(HttpMethod method, String uri, Class<?> controllerClass, UserSecurityRole role, Permission permission) throws Exception {
        TestAuthentication authentication = new TestAuthentication(allRolesExcept(role));
        Mockito.when(securityService.getProjectMemberRoles("toto", authentication.getName())).thenReturn(allMemberRolesExceptPermission(permission));
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri).with(SecurityMockMvcRequestPostProcessors.authentication(authentication))).andExpect(MockMvcResultMatchers.status().isForbidden());
        assertRequestExists(controllerClass, method, uri);
    }

    private static Stream<Arguments> provideAuthenticatedWithUserWithOtherRoleThanAndProjectMemberWithPermissionTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.PUT, "/api/projects/toto", ProjectResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications", CommunicationResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/communications/titi", CommunicationResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/countries", CountryResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/countries/ok", CountryResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/cycle-definitions", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/cycle-definitions/42", CycleDefinitionResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/42", ErrorResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/errors/matching", ErrorResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/errors/distinct/titi", ErrorResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executed-scenarios/history", ExecutedScenarioResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executed-scenarios/42", ExecutedScenarioResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/request-completion", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/quality-status", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/latest-eligible-versions", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/upload", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/with-successes", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/discard", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/executions/42/un-discard", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/executions/42/history", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/executions/42/filtered", ExecutionResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/functionalities/42", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/move/list", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/42/scenarios", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/coverage", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.OPTIONS, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/functionalities/export", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/functionalities/import", FunctionalityResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problem-patterns/42/errors", ProblemPatternResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problem-patterns/42", ProblemPatternResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/problems/42/errors", ProblemResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/filter", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/append-pattern", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/42/pick-up-pattern/41", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/close/41", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/reopen", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/problems/42/refresh-defect-status", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/problems/recompute-first-and-last-seen-date-times", ProblemResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/groups", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/groups/titi", ProjectGroupMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/members/users", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PATCH, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/members/users/titi", ProjectUserMemberResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/purge/force", PurgeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/root-causes", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/root-causes/42", RootCauseResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload/toto", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/scenarios/upload-postman/toto", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/without-functionalities", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/scenarios/ignored", ScenarioResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/toto", SettingResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings", SettingResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/settings/technology", SettingResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/settings/titi/technology/tutu", SettingResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/sources", SourceResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/sources/titi", SourceResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/teams", TeamResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/teams/42", TeamResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.POST, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.PUT, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN),
                Arguments.of(HttpMethod.GET, "/api/projects/toto/types", TypeResource.class, UserSecurityRole.ADMIN, Permission.READ),
                Arguments.of(HttpMethod.DELETE, "/api/projects/toto/types/titi", TypeResource.class, UserSecurityRole.ADMIN, Permission.ADMIN));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticatedWithUserWithOtherRoleThanAndProjectMemberWithPermissionTestArguments")
    void shouldHasNodFoundwhenAuthenticatedWithUserWithOtherRoleThanAndProjectMemberWithPermission(HttpMethod method, String uri, Class<?> controllerClass, UserSecurityRole role, Permission permission) throws Exception {
        TestAuthentication authentication = new TestAuthentication(allRolesExcept(role));
        Mockito.when(securityService.getProjectMemberRoles("toto", authentication.getName())).thenReturn(memberRoleWithAtLeast(permission));
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri).with(SecurityMockMvcRequestPostProcessors.authentication(authentication))).andExpect(MockMvcResultMatchers.status().isNotFound());
        assertRequestExists(controllerClass, method, uri);
    }

    private static Stream<Arguments> provideAuthenticatedWithUserWithOtherRoleThanAndGroupMemberWithPermissionOtherThanTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto", GroupResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/groups/toto/members", GroupMemberResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PATCH, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN, Permission.WRITE));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticatedWithUserWithOtherRoleThanAndGroupMemberWithPermissionOtherThanTestArguments")
    void shouldHasForbiddenResponseWhenAuthenticatedWithUserWithOtherRoleThanAndGroupMemberWithPermissionOtherThan(HttpMethod method, String uri, Class<?> controllerClass, UserSecurityRole role, Permission permission) throws Exception {
        TestAuthentication authentication = new TestAuthentication(allRolesExcept(role));
        Mockito.when(securityService.getGroupMemberRoles("toto", authentication.getName())).thenReturn(allMemberRolesExceptPermission(permission));
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri).with(SecurityMockMvcRequestPostProcessors.authentication(authentication))).andExpect(MockMvcResultMatchers.status().isForbidden());
        assertRequestExists(controllerClass, method, uri);
    }

    private static Stream<Arguments> provideAuthenticatedWithUserWithOtherRoleThanAndGroupMemberWithPermissionTestArguments() {
        return Stream.of(
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto", GroupResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.POST, "/api/groups/toto/members", GroupMemberResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.PATCH, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN, Permission.WRITE),
                Arguments.of(HttpMethod.DELETE, "/api/groups/toto/members/titi", GroupMemberResource.class, UserSecurityRole.ADMIN, Permission.WRITE));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticatedWithUserWithOtherRoleThanAndGroupMemberWithPermissionTestArguments")
    void shouldHasNotFoundResponseWhenAuthenticatedWithUserWithOtherRoleThanAndGroupMemberWithPermission(HttpMethod method, String uri, Class<?> controllerClass, UserSecurityRole role, Permission permission) throws Exception {
        TestAuthentication authentication = new TestAuthentication(allRolesExcept(role));
        Mockito.when(securityService.getGroupMemberRoles("toto", authentication.getName())).thenReturn(memberRoleWithAtLeast(permission));
        mockMvc.perform(MockMvcRequestBuilders.request(method, uri).with(SecurityMockMvcRequestPostProcessors.authentication(authentication))).andExpect(MockMvcResultMatchers.status().isNotFound());
        assertRequestExists(controllerClass, method, uri);
    }

    private User machineToMachineCall(MockHttpSession session, List<UserSecurityRole> userRoles) throws Exception {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        mockJwks(wireMockExtensionOauth2, publicKey, privateKey);
        JwtBuilder jwtBuilder = new DefaultJwtBuilder().setSubject("user1").setIssuer(wireMockExtensionOauth2.baseUrl()).setAudience("ara-client").setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 3600000)).signWith(SignatureAlgorithm.RS256, privateKey);
        String accessToken = jwtBuilder.compact();
        User user = new User("user1", wireMockExtensionOauth2.baseUrl());
        Mockito.when(securityService.initUser("user1", wireMockExtensionOauth2.baseUrl())).thenReturn(user);
        Mockito.when(securityService.getUserRoles(user.getId())).thenReturn(userRoles);
        mockMvc.perform(MockMvcRequestBuilders.get("/").header("Authorization", "Bearer " + accessToken).session(session)).andReturn();
        return user;
    }

    private User login(MockHttpSession session, boolean oidc, String code, String userName, Map<String, String> attributes, List<UserSecurityRole> userRoles) throws Exception {
        Map<String, String> userAttributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
        userAttributes.put("email", userName + "@email.com");
        WireMockExtension wireMockExtension = null;
        String registrationId = null;
        if (oidc) {
            wireMockExtension = wireMockExtensionOidc;
            registrationId = "ara-client-oidc";
        } else {
            wireMockExtension = wireMockExtensionOauth2;
            registrationId = "ara-client-oauth2";
        }

        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.get("/oauth/login/" + registrationId).session(session)).andReturn();
        String location = loginResult.getResponse().getHeader("Location");
        Matcher stateMatcher = STATE_EXTRACTOR_PATTERN.matcher(location);
        String state = null;
        String nonce = null;
        if (stateMatcher.find()) {
            state = stateMatcher.group(1);
            state = UriUtils.decode(state, StandardCharsets.UTF_8);
            if (oidc) {
                Matcher nonceMatcher = NONCE_EXTRACTOR_PATTERN.matcher(location);
                if (nonceMatcher.find()) {
                    nonce = nonceMatcher.group(1);
                    nonce = UriUtils.decode(nonce, StandardCharsets.UTF_8);
                }
            }
        }
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        mockJwks(wireMockExtension, publicKey, privateKey);
        JwtBuilder jwtBuilder = new DefaultJwtBuilder().setSubject(userName).setIssuer(wireMockExtension.baseUrl()).setAudience("ara-client").setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 3600000)).signWith(SignatureAlgorithm.RS256, privateKey);
        String accessToken = jwtBuilder.compact();
        StringBuilder tokenEndpointBody = new StringBuilder("{\"access_token\": \"");
        tokenEndpointBody.append(accessToken).append("\",\"token_type\": \"Bearer\", \"expire_in\": 3600");
        if (oidc) {
            tokenEndpointBody.append(",\"id_token\":\"");
            jwtBuilder.claim("nonce", nonce);
            for (Map.Entry<String, String> entry : userAttributes.entrySet()) {
                jwtBuilder.claim(entry.getKey(), entry.getValue());
            }
            tokenEndpointBody.append(jwtBuilder.compact()).append('"');
        }
        tokenEndpointBody.append('}');
        wireMockExtension.stubFor(WireMock.post("/oauth2/token").withRequestBody(new ContainsPattern("code=" + code)).willReturn(WireMock.okForContentType(MediaType.APPLICATION_JSON_VALUE, tokenEndpointBody.toString())));
        if (!oidc) {
            StringBuilder userInfoEndpointBody = new StringBuilder("{\"sub\":\"");
            userInfoEndpointBody.append(userName).append('"');
            for (Map.Entry<String, String> entry : userAttributes.entrySet()) {
                userInfoEndpointBody.append(",\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append('"');
            }
            userInfoEndpointBody.append('}');
            wireMockExtension.stubFor(WireMock.get("/oauth2/userinfo").withHeader("Authorization", new ContainsPattern(accessToken)).willReturn(WireMock.okForContentType(MediaType.APPLICATION_JSON_VALUE, userInfoEndpointBody.toString())));
        }
        User user = new User(userName, wireMockExtension.baseUrl());
        Mockito.when(securityService.initUser(userName, wireMockExtension.baseUrl())).thenReturn(user);
        Mockito.when(securityService.getUserRoles(user.getId())).thenReturn(userRoles);
        mockMvc.perform(MockMvcRequestBuilders.get("/oauth/logincomplete/" + registrationId + "?code=" + code + "&state=" + state).session(session)).andReturn();
        return user;
    }

    private void mockJwks(WireMockExtension wireMockExtension, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        if (wireMockExtension.findAllStubsByMetadata(new EqualToPattern("/oauth2/jwks")).getMappings().isEmpty()) {
            wireMockExtension.stubFor(WireMock.get("/oauth2/jwks").willReturn(WireMock.okForContentType(MediaType.APPLICATION_JSON_VALUE, "{\"keys\":[" + new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build().toPublicJWK().toJSONString() + "]}")));
        }
    }

    private Authentication getAuthenticationfromSession(MockHttpSession session) {
        return ((SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication();
    }

    private List<SimpleGrantedAuthority> allRolesExcept(UserSecurityRole... roles) {
        Set<UserSecurityRole> except = new HashSet<>();
        for (UserSecurityRole role : roles) {
            except.add(role);
            UserSecurityRole parent = role;
            while ((parent = parent.getParent()) != null) {
                except.add(parent);
            }
        }
        return roles(Arrays.stream(UserSecurityRole.values()).filter(role -> !except.contains(role)).toList());
    }

    private List<SimpleGrantedAuthority> roles(UserSecurityRole... roles) {
        return roles(Arrays.asList(roles));
    }

    private List<SimpleGrantedAuthority> roles(List<UserSecurityRole> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
    }

    private Set<MemberRole> memberRoleWithAtLeast(Permission permission) {
        Set<MemberRole> memberRoles = new HashSet<>();
        Optional<MemberRole> roleWithPermission = Arrays.stream(MemberRole.values()).filter(role -> role.hasPermission(permission)).findFirst();
        if (roleWithPermission.isPresent()) {
            memberRoles.add(roleWithPermission.get());
        }
        return memberRoles;
    }

    private Set<MemberRole> allMemberRolesExceptPermission(Permission... permissions) {
        return Arrays.stream(MemberRole.values()).filter(role -> {
            for (Permission permission : permissions) {
                if (role.hasPermission(permission)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toSet());
    }

    private void assertRequestExists(Class<?> controllerClass, HttpMethod httpMethod, String uri) {
        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        if (requestMapping == null) {
            Assertions.fail(controllerClass.getName() + " is not a RequestMapping");
        }
        String[] baseUris = requestMapping.value();
        for (Method method : controllerClass.getMethods()) {
            requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping != null) {
                for (RequestMethod requestMethod : requestMapping.method()) {
                    if (requestMethod.name().equals(httpMethod.name())) {
                        for (String baseUri : baseUris) {
                            if (match(uri, baseUri, requestMapping.value())) {
                                return;
                            }
                        }
                    }
                }
            } else {
                for (Annotation annotation : method.getAnnotations()) {
                    requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
                    if (requestMapping != null) {
                        for (RequestMethod requestMethod : requestMapping.method()) {
                            if (requestMethod.name().equals(httpMethod.name())) {
                                for (String baseUri : baseUris) {
                                    try {
                                        String[] paths = (String[]) annotation.annotationType().getMethod("value").invoke(annotation);
                                        if (match(uri, baseUri, paths)) {
                                            return;
                                        }
                                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                                        Assertions.fail("should not happen", e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Assertions.fail(httpMethod + " " + uri + " doesn't match any method in " + controllerClass.getName());
    }

    private boolean match(String uri, String baseUri, String[] paths) {
        if (paths.length > 0) {
            for (String path : paths) {
                AntPathMatcher antPathMatcher = new AntPathMatcher();
                if (antPathMatcher.match(baseUri + path, uri)) {
                    return true;
                }
            }
        } else {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            if (antPathMatcher.match(baseUri, uri)) {
                return true;
            }
        }
        return false;
    }

}
