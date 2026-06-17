package com.saludrednorte.ms_progreso.config;

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
                        .title("API de Progreso del Paciente - Red Norte")
                        .version("1.0.0")
                        .description("Microservicio para la gestión del estado de progreso del paciente en el sistema de salud Red Norte")
                        .contact(new Contact()
                                .name("Red Norte")
                                .email("contacto@saludrednorte.com")));
    }
}
