package com.credenciales.tribunal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
@Configuration
public class OpenApiConfig{
    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
            .info( new Info()
                .title("Control Api")
                .version("1.0")
                .description("Api for ")
                .contact( new Contact().name("Alfred Lazar").email("micorreo@example.com").url(""))
            )
            .addSecurityItem(new SecurityRequirement()
                .addList("bearerAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}