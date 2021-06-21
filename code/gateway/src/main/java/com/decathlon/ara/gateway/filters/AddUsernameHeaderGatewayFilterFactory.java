package com.decathlon.ara.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class AddUsernameHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<AddUsernameHeaderGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AddUsernameHeaderGatewayFilterFactory.class);

    public AddUsernameHeaderGatewayFilterFactory() {
        super(Config.class);
    }


    @Override
    public GatewayFilter apply(Config config) {

        // grab configuration from Config object
        return (exchange, chain) -> exchange.getPrincipal()
                .doOnEach( principal -> {
                    if (!principal.hasValue()){
                        exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                        logger.error("No principal associated to this request, this should never happen.");
                    }
                })
                .map(Principal::getName)
                .map(userName -> {
                    //adds header to proxied request
                    logger.debug("Username: {}", userName);
                    exchange.getRequest().mutate().header("GW-DOWNSTREAM-USERNAME", userName).build();
                    return exchange;
                })
                .flatMap(chain::filter);
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }
}
