package com.saludrednorte.bff.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BFF - Red Norte")
                        .version("1.0.0")
                        .description("Backend for Frontend: agrega datos de varios microservicios para la UI del sistema de salud Red Norte")
                        .contact(new Contact()
                                .name("Red Norte")
                                .email("contacto@saludrednorte.com")));
    }
}
