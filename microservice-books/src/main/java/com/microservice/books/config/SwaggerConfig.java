package com.microservice.books.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservice de Libros")
                        .description("Documentación de la API de Microservice de Libros")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gary Ricardo Campaña Ramírez")
                                .email("gary.campana@epn.edu.ec"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
