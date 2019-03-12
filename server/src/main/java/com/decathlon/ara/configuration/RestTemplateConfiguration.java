package com.decathlon.ara.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestTemplateConfiguration {

    @NonNull
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * @return a globally available REST template to make HTTP REST API calls with the same configuration everywhere in the application.
     */
    @Bean
    public RestTemplate restTemplate() {
        return restTemplateBuilder.build();
    }

}
