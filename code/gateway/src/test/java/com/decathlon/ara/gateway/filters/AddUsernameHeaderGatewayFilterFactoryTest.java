package com.decathlon.ara.gateway.filters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;

@SpringBootTest
@ActiveProfiles(profiles = "dev")
public class AddUsernameHeaderGatewayFilterFactoryTest {

    @MockBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private AddUsernameHeaderGatewayFilterFactory customFilter;


    @Test
    public void testApply(){
        //TODO Complete TU
        /*
        GatewayFilterChain filterChain= filterExchange -> Mono.empty();
        Principal user = () -> "user";
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/fake"))
                .mutate().principal(Mono.just(user)).build();

        GatewayFilter myFilterImpl= this.customFilter.apply(new AddUsernameHeaderGatewayFilterFactory.Config() );
        myFilterImpl.filter(exchange, filterChain).doOnEach( v -> v. )
        */
        Assertions.assertTrue(true);

    }

}
