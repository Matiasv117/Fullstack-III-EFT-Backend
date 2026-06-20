package com.saludrednorte.ms_auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger para documentar la API del servicio.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Autenticación y Usuarios - Red Norte")
                        .version("1.0.0")
                        .description("Microservicio para la autenticación, generación de tokens JWT y roles de usuario en el sistema de salud Red Norte")
                        .contact(new Contact()
                                .name("Red Norte")
                                .email("contacto@saludrednorte.com")));
    }
}
