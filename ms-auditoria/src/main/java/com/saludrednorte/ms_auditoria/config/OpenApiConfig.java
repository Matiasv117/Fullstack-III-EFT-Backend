package com.saludrednorte.ms_auditoria.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de documentación OpenAPI/Swagger para ms-auditoria.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Auditoría - Red Norte")
                        .version("1.0.0")
                        .description("Microservicio para registrar y consultar eventos de auditoría del sistema de salud RedNorte")
                        .contact(new Contact()
                                .name("Red Norte")
                                .email("contacto@saludrednorte.com")));
    }
}
