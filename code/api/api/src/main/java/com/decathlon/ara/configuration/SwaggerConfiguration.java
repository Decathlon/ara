/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfiguration {

    @Value("${ara.clientBaseUrl}")
    private String baseUrl;
          
    final String JWT_SECURITY_SCHEME_NAME = "jwt";
    final String JWT_SECURITY_SCHEME_BEARER = "bearer";
    final String JWT_SECURITY_SCHEME_BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI araOpenAPI() {
      return new OpenAPI()
                .addServersItem(new Server().url(baseUrl))
                .info(
                        new Info()
                                .title("ARA API")
                                .description("API used by ARA (Agile Regression Analyzer)")
                                .version("1.0")
                                .contact(
                                        new Contact()
                                                .name("Decathlon")
                                                .url("https://github.com/decathlon")
                                                .email("developers@decathlon.com"))
                                .license(
                                        new License()
                                                .name("Apache 2.0")
                                                .url("http://www.apache.org/licenses/")))
                                .components(
                                         new Components()
                                                    .addSecuritySchemes(JWT_SECURITY_SCHEME_NAME, new SecurityScheme().scheme(JWT_SECURITY_SCHEME_BEARER).type(Type.HTTP).bearerFormat(JWT_SECURITY_SCHEME_BEARER_FORMAT)))
                                                    .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME_NAME));     

    }

}

   
