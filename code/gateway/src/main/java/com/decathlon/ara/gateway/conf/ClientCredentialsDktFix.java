package com.decathlon.ara.gateway.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class ClientCredentialsDktFix {

    private static final Logger logger = LoggerFactory.getLogger(ClientCredentialsDktFix.class);

    // In the jwt token, the iss value is idpdecathlon, while it should be: idpdecathlon.oxylane.com
    // If this is fixed by fedid, this bean can be deactivated
    @Bean
    @ConditionalOnExpression("${feature-flipping.authent.oauth.enabled:true} && ${feature-flipping.authent.oauth.features.client-credentials:true} && !T(org.springframework.util.StringUtils).isEmpty('${feature-flipping.authent.oauth.features.client-credential-dktfix.override-issuer:}')")
    public ReactiveJwtDecoder getCustomRxJwtDecoder(
            OAuth2ResourceServerProperties properties,
            @Value("${feature-flipping.authent.oauth.features.client-credential-dktfix.override-issuer}") String customIssuerInToken
    ){

        logger.info("Using custom jwt decoder to set custom issuer uri AND custom token issuer");

        OAuth2ResourceServerProperties.Jwt jwtProps= properties.getJwt();
        String jwk_set_uri= jwtProps.getJwkSetUri();
        logger.debug("Using jwk-set-uri: {}", jwk_set_uri);
        // When we define our own jwt decoder, we need to override the jwks_uri param
        // OAuth2ResourceServerJwtConfiguration is able to do that, so I found it here: https://github.com/spring-projects/spring-boot/blob/2.4.x/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/security/oauth2/resource/servlet/OAuth2ResourceServerJwtConfiguration.java#L69
        // => This allows to use the jwk-set-uri property defined in application.yml to override the bad jwks_uri value
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwk_set_uri)
                .jwsAlgorithm(SignatureAlgorithm.from(jwtProps.getJwsAlgorithm())).build();

        // This allow to specify the (f*cking wrong) issuer
        logger.debug("Set custom jwt issuer in tokens: {}", customIssuerInToken);
        OAuth2TokenValidator<Jwt> defaultWithIssuer = JwtValidators.createDefaultWithIssuer(customIssuerInToken);
        DelegatingOAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<Jwt>(defaultWithIssuer);
        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

}
