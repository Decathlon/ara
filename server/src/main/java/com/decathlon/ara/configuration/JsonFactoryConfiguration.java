package com.decathlon.ara.configuration;

import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonFactoryConfiguration {

    /**
     * From the JsonFactory documentation:
     * factory instances are thread-safe and reusable [...]
     * Factory reuse is important if efficiency matters;
     * most recycling of expensive construct is done on per-factory basis.
     *
     * @return a globally shared JsonFactory
     */
    @Bean
    public JsonFactory jsonFactory() {
        return new JsonFactory();
    }

}
