package com.decathlon.ara.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.javadoc.configuration.JavadocPluginConfiguration;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

@Configuration
@EnableSwagger2
@Import(JavadocPluginConfiguration.class)
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("api")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant(API_PATH + "/**")) // Don't include actuator endpoints
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("ARA API")
                .description("API used by ARA (Agile Regression Analyzer)")
                .contact(new Contact("Decathlon", "https://github.com/decathlon", "developers@decathlon.com"))
                .version("1.0")
                .build();
    }

}
