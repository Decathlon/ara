package com.decathlon.ara.configuration.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.util.Assert;

import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.domain.enumeration.Permission;
import com.decathlon.ara.domain.enumeration.UserSecurityRole;
import com.decathlon.ara.loader.DemoLoaderConstants;
import com.decathlon.ara.service.security.SecurityService;
import com.decathlon.ara.web.rest.util.RestConstants;

@Configuration
@EnableCaching
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

    private SecurityService securityService;

    public CustomSecurity(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Whatever is the authentication method, we need to authorize /actguatorgw
        String redirectUrl = String.format("%s?spring_redirect=true", this.clientBaseUrl);
        configureProjectAccess(http,
                new String[] { "/problems", "/problem-patterns", "/errors", "/executions", "/functionalities" },
                new MethodAccess(UserSecurityRole.ADMIN, Permission.WRITE, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE));
        configureProjectAccess(http,
                new MethodAccess(UserSecurityRole.ADMIN, Permission.READ, HttpMethod.GET, HttpMethod.OPTIONS),
                new MethodAccess(UserSecurityRole.ADMIN, Permission.ADMIN, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE));
        configureGroupAccess(http, new MethodAccess(UserSecurityRole.ADMIN, Permission.WRITE, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.DELETE));
        configureAccess(http, new String[] { "/admin/projects" },
                new MethodAccess(UserSecurityRole.ADMIN, HttpMethod.GET));
        configureAccess(http, new String[] { "/demo" },
                methodAccess -> methodAccess.makePermissionAccess(builder -> builder.append("@customSecurity.hasProjectPermission(authentication, '").append(DemoLoaderConstants.PROJECT_CODE_DEMO).append("', '")),
                new MethodAccess(UserSecurityRole.ADMIN, Permission.ADMIN, HttpMethod.DELETE));
        configureAccess(http, new String[] { "/projects", "/groups", "/demo" },
                new MethodAccess(UserSecurityRole.PROJECT_OR_GROUP_CREATOR, HttpMethod.POST));
        configureAccess(http, new String[] { "/features" },
                new MethodAccess(UserSecurityRole.ADMIN, HttpMethod.PATCH, HttpMethod.DELETE));
        configureAccess(http, new String[] { "/users/current/details" },
                new MethodAccess(HttpMethod.GET));
        configureAccess(http, new String[] { "/users/{userName}" },
                new MethodAccess(UserSecurityRole.ADMIN, HttpMethod.GET));
        configureAccess(http, new String[] { "/auditing" },
                new MethodAccess(UserSecurityRole.AUDITING, HttpMethod.GET));
        http.csrf().disable() // NOSONAR
                .authorizeRequests(requests -> requests
                        .antMatchers("/oauth/**", "/actuator/**", "/test/**").permitAll().anyRequest().authenticated())
                .logout(logout -> logout.logoutUrl(this.logoutProcessingUrl) // logout entrypoint
                        .invalidateHttpSession(true).clearAuthentication(true).logoutSuccessUrl(redirectUrl)
                        .deleteCookies("JSESSIONID").permitAll())
                .oauth2Login(login -> login.loginProcessingUrl(this.loginProcessingUrl + "/*") // path to redirect to from auth server
                        .loginPage(String.format("%s/%s", this.clientBaseUrl, "login")) // standard spring redirection for protected resources
                        .defaultSuccessUrl(redirectUrl, true) // once logged in, redirect to
                        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.baseUri(this.loginStartingUrl)) // entrypoint to initialize oauth processing
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oauth2UserService(new DefaultOAuth2UserService(), this::constructOAuth2User))
                                .oidcUserService(oauth2UserService(new OidcUserService(), this::constructOidcUser))))
                .sessionManagement(sessionManagement -> sessionManagement.maximumSessions(-1).sessionRegistry(sessionRegistry()));

        if (isResourceServerConfiguration()) {
            http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(this::convert);
        }
        return http.build();
    }

    private boolean isResourceServerConfiguration() {
        return Strings.isNotBlank(resourceIssuerUri) || Strings.isNotBlank(resourceJwkSetUri);
    }

    private <R extends OAuth2UserRequest, U extends OAuth2User> OAuth2UserService<R, U> oauth2UserService(OAuth2UserService<R, U> delegate, OAuth2UserConstructor<R, U> userConstructor) {
        return request -> {
            U remoteUser = delegate.loadUser(request);
            User user = securityService.initUser(remoteUser.getName(), request.getClientRegistration().getProviderDetails().getIssuerUri());
            List<? extends GrantedAuthority> authorities = getUserAuthorities(user.getId());
            return userConstructor.constructUser(user.getId(), request, remoteUser, authorities);
        };
    }

    private OAuth2User constructOAuth2User(String userName, OAuth2UserRequest request, OAuth2User remoteUser, List<? extends GrantedAuthority> authorities) {
        return new AraOauth2User(userName, authorities, remoteUser.getAttributes());
    }

    private OidcUser constructOidcUser(String userName, OidcUserRequest request, OidcUser user, List<? extends GrantedAuthority> authorities) {
        return new AraOidcUser(userName, authorities, user.getIdToken(), user.getUserInfo());
    }

    private AbstractAuthenticationToken convert(Jwt jwt) {
        User user = securityService.initUser(jwt.getSubject(), jwt.getClaimAsString(JwtClaimNames.ISS));
        return new JwtAuthenticationToken(jwt, getUserAuthorities(user.getId()), user.getId());
    }

    private List<? extends GrantedAuthority> getUserAuthorities(String userId) {
        return securityService.getUserRoles(userId).stream().map(userRole -> new SimpleGrantedAuthority(getSecurityRole(userRole))).toList();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        StringBuilder roleHierarchyBuilder = new StringBuilder();
        Set<UserSecurityRole> processedRole = new HashSet<>();
        for (UserSecurityRole role : UserSecurityRole.values()) {
            appendHierarchy(roleHierarchyBuilder, processedRole, role);
        }
        roleHierarchy.setHierarchy(roleHierarchyBuilder.toString());
        return roleHierarchy;
    }

    private void appendHierarchy(StringBuilder roleHierarchyBuilder, Set<UserSecurityRole> processedRole, UserSecurityRole role) {
        UserSecurityRole parent = role.getParent();
        if (parent != null && processedRole.add(role)) {
            appendHierarchy(roleHierarchyBuilder, processedRole, parent);
            roleHierarchyBuilder.append(getSecurityRole(parent)).append(" > ").append(getSecurityRole(role)).append(" \n");
        }
    }

    private static String getSecurityRole(UserSecurityRole role) {
        return "ROLE_" + role.name();
    }

    public boolean hasProjectPermission(Authentication authentication, String projectCode, String permission) {
        return hasPermission(securityService.getProjectMemberRoles(projectCode, authentication.getName()), permission);
    }

    public boolean hasGroupPermission(Authentication authentication, String groupName, String permission) {
        return hasPermission(securityService.getGroupMemberRoles(groupName, authentication.getName()), permission);
    }

    private boolean hasPermission(Set<MemberRole> memberRoles, String permission) {
        return memberRoles.stream().anyMatch(projectMemberType -> projectMemberType.hasPermission(Permission.valueOf(permission)));
    }

    /**
     * Call {@link #configureAccess(HttpSecurity, String, String[], Function, MethodAccess...)} with "/api" as prefix and MethodAccess#makeRoleAccess as authorizedUrlConfigurer
     * @param http
     * @param urlPatterns
     * @param accesses
     * @throws Exception
     */
    private void configureAccess(HttpSecurity http, String[] urlPatterns, MethodAccess... accesses) throws Exception {
        configureAccess(http, RestConstants.API_PATH, urlPatterns, MethodAccess::makeRoleAccess, accesses);
    }

    /**
     * Call {@link #configureAccess(HttpSecurity, String, String[], Function, MethodAccess...)} with "/api"  as prefix
     * @param http
     * @param urlPatterns
     * @param accesses
     * @throws Exception
     */
    private void configureAccess(HttpSecurity http, String[] urlPatterns, Function<MethodAccess, Consumer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl>> authorizedUrlConfigurer, MethodAccess... accesses) throws Exception {
        configureAccess(http, RestConstants.API_PATH, urlPatterns, authorizedUrlConfigurer, accesses);
    }

    /**
     * Call {@link #configureAccess(HttpSecurity, String, String[], Function, MethodAccess...)} with "/api/groups/{groupName}" as prefix and MethodAccess#makeGroupAccess as authorizedUrlConfigurer
     * @param http
     * @param urlPatterns
     * @param accesses
     * @throws Exception
     */
    private void configureGroupAccess(HttpSecurity http, MethodAccess... accesses) throws Exception {
        configureAccess(http, RestConstants.API_PATH + "/groups/{groupName}", null, MethodAccess::makeGroupAccess, accesses);
    }

    /**
     * Call {@link #configureAccess(HttpSecurity, String, String[], Function, MethodAccess...)} with "/api/projects/{project}" as prefix, null as urlPattern and MethodAccess#makeProjectAccess as authorizedUrlConfigurer
     * @param http
     * @param urlPatterns
     * @param accesses
     * @throws Exception
     */
    private void configureProjectAccess(HttpSecurity http, MethodAccess... accesses) throws Exception {
        configureProjectAccess(http, null, accesses);
    }

    /**
     * Call {@link #configureAccess(HttpSecurity, String, String[], Function, MethodAccess...)} with "/api/projects/{project}" as prefix and MethodAccess#makeProjectAccess as authorizedUrlConfigurer
     * @param http
     * @param urlPatterns
     * @param accesses
     * @throws Exception
     */
    private void configureProjectAccess(HttpSecurity http, String[] urlPatterns, MethodAccess... accesses) throws Exception {
        configureAccess(http, RestConstants.PROJECT_API_PATH, urlPatterns, MethodAccess::makeProjectAccess, accesses);
    }

    /**
     * Configure access for all urlPattern and their matching child with the given prefix. <br />
     * Example with urlPatterns as new String[] { "/toto", "/titi" } and /api as prefix,  configured url will be /api/toto, /api/toto/**, /api/titi, /api/titi/** </br>
     * When configuring different api level, more precise level must be defined first to take effect </br>
     * Example to configure /api/toto and /api/toto/titi, the call of this method to configure /api/toto/titi must be done before the one for /api/toto
     * @param http
     * @param urlPatternPrefix : prefix to apply to all urlPatterns
     * @param urlPatterns : url patterns to configure, when null only urlPatternPrefix is configured
     * @param authorizedUrlConfigurer
     * @param accesses 
     * @throws Exception
     */
    private void configureAccess(HttpSecurity http, String urlPatternPrefix, String[] urlPatterns, Function<MethodAccess, Consumer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl>> authorizedUrlConfigurer, MethodAccess... accesses) throws Exception {
        http.authorizeRequests(requests -> {
            for (MethodAccess access : accesses) {
                for (HttpMethod method : access.methods()) {
                    Stream<String> patternStream;
                    if (urlPatterns == null) {
                        patternStream = Stream.of("");
                    } else {
                        patternStream = Arrays.stream(urlPatterns);
                    }
                    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl antMatchers = requests.antMatchers(method, patternStream.mapMulti((pattern, stream) -> {
                        Assert.state(!pattern.startsWith(urlPatternPrefix), () -> "when configuring access, pattern must not start with the prefix pattern. Got prefix " + urlPatternPrefix + " and pattern " + pattern);
                        String projectUriPattern = urlPatternPrefix + pattern;
                        stream.accept(projectUriPattern);
                        stream.accept(projectUriPattern + "/**");
                    }).toArray(String[]::new));
                    authorizedUrlConfigurer.apply(access).accept(antMatchers);
                }
            }
        });
    }

    /**
     * Class that store all information necessary to define which Role/Permission are mandatory to access given HttpMethod.<br />
     * This class provide some method that can generate SpringSecurity configuration ({@link #makeGroupAccess()}, {@link #makeProjectAccess()}, {@link #makeRoleAccess()}...)
     * @author z15lross
     */
    private static record MethodAccess(UserSecurityRole role, Permission permission, List<HttpMethod> methods) {

        /**
         * Access to httpMethods is not restricted
         * @param httpMethods
         */
        public MethodAccess(HttpMethod... httpMethods) {
            this(null, null, List.of(httpMethods));
        }

        /**
         * Access to httpMethods is available for user with the given role
         * @param role
         * @param httpMethods
         */
        public MethodAccess(UserSecurityRole role, HttpMethod... httpMethods) {
            this(role, null, List.of(httpMethods));
        }

        /**
         * Access to httpMethods is available for user with the given role or the given permission 
         * @see CustomSecurity#configureProjectAccess(HttpSecurity, MethodAccess...)
         * @see CustomSecurity#configureProjectAccess(HttpSecurity, String[], MethodAccess...)
         * @see CustomSecurity#configureGroupAccess(HttpSecurity, MethodAccess...)
         * @param role
         * @param permission
         * @param httpMethods
         */
        public MethodAccess(UserSecurityRole role, Permission permission, HttpMethod... httpMethods) {
            this(role, permission, List.of(httpMethods));
        }

        /**
         * This method can be used to get a consumer that can be used to configure spring security
         * @return a consumer of {@link ExpressionUrlAuthorizationConfigurer.AuthorizedUrl} that can be used to configure spring security.
         * User can access if he has the role configured in this MethodAccess
         */
        private Consumer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl> makeRoleAccess() {
            return authorizedUrl -> {
                if (role != null) {
                    authorizedUrl.hasRole(role.name());
                } else {
                    authorizedUrl.authenticated();
                }
            };
        }

        /**
         * This method can be used to get a consumer that can be used to configure spring security for url with projectCode as path variable
         * @return a consumer of {@link ExpressionUrlAuthorizationConfigurer.AuthorizedUrl} that can be used to configure spring security.
         * User can access if he has the role or the permission configured in this MethodAccess
         * User project permission will be checked by calling {@link CustomSecurity#hasProjectPermission(Authentication, String, String)}
         */
        private Consumer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl> makeProjectAccess() {
            return makePermissionAccess(builder -> builder.append("@customSecurity.hasProjectPermission(authentication, #projectCode, '"));
        }

        /**
         * This method can be used to get a consumer that can be used to configure spring security for url with groupName as path variable
         * @return a consumer of {@link ExpressionUrlAuthorizationConfigurer.AuthorizedUrl} that can be used to configure spring security.
         * User can access if he has the role or the permission configured in this MethodAccess
         * User group permission will be checked by calling {@link CustomSecurity#hasGroupPermission(Authentication, String, String)}
         */
        private Consumer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl> makeGroupAccess() {
            return makePermissionAccess(builder -> builder.append("@customSecurity.hasGroupPermission(authentication, #groupName, '"));
        }

        private Consumer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl> makePermissionAccess(Consumer<StringBuilder> builderConsumer) {
            StringBuilder builder = new StringBuilder();
            builder.append("hasRole('").append(getSecurityRole(role)).append("')").append(" or ");
            builderConsumer.accept(builder);
            builder.append(permission.name()).append("')");
            return authorizedUrl -> authorizedUrl.access(builder.toString());
        }

    }

    private static class AraOauth2User implements OAuth2User {

        private String name;
        private Collection<? extends GrantedAuthority> authorities;
        private Map<String, Object> attributes;

        public AraOauth2User(String name, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
            this.name = name;
            this.authorities = Collections.unmodifiableCollection(authorities);
            this.attributes = Collections.unmodifiableMap(attributes);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }
    }

    private static class AraOidcUser extends DefaultOidcUser {

        private static final long serialVersionUID = 1L;

        private String name;

        public AraOidcUser(String name, Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, OidcUserInfo userInfo) {
            super(authorities, idToken, userInfo);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(name);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof AraOidcUser)) {
                return false;
            }
            AraOidcUser other = (AraOidcUser) obj;
            return Objects.equals(name, other.name);
        }

    }

    @FunctionalInterface
    private interface OAuth2UserConstructor<R extends OAuth2UserRequest, U extends OAuth2User> {
        U constructUser(String userName, R request, U remoteUser, List<? extends GrantedAuthority> authorities);
    }

}
