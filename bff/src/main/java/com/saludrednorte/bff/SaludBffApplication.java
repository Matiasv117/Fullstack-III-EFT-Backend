package com.saludrednorte.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Backend for Frontend (BFF): capa dedicada a armar respuestas pensadas para el frontend,
 * combinando llamadas a varios microservicios (vía API Gateway).
 */
@SpringBootApplication
public class SaludBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaludBffApplication.class, args);
    }
}
