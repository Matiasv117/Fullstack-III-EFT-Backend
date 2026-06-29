package com.saludrednorte.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Backend for Frontend (BFF): capa dedicada a armar respuestas pensadas para el frontend,
 * combinando llamadas a varios microservicios (vía API Gateway).
 */
@OpenAPIDefinition(info = @Info(title = "BFF", version = "1.0", description = "Backend for Frontend"))
@SpringBootApplication
public class SaludBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaludBffApplication.class, args);
    }
}
