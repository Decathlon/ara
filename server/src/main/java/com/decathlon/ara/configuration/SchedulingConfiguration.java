package com.decathlon.ara.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(value = "ara.scheduling.enable", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class SchedulingConfiguration {

}
