package com.decathlon.ara;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@Slf4j
public class AraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AraApplication.class, args);
    }

    @Bean
    @SuppressWarnings("static-method")
    public CommandLineRunner commandLineRunner(ApplicationContext applicationContext) {
        return args -> {
            if (log.isDebugEnabled()) {
                log.debug("Spring Beans:");
                String[] beanNames = applicationContext.getBeanDefinitionNames();
                Arrays.sort(beanNames);
                for (String beanName : beanNames) {
                    log.debug("* {} => {}", beanName, applicationContext.getBean(beanName).getClass().getName());
                }
            }
        };
    }

}
