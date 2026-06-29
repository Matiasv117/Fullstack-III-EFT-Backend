package com.saludrednorte.eureka;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor Eureka para descubrimiento de servicios.
 * Todos los microservicios se registran en este servidor.
 */
@OpenAPIDefinition(info = @Info(title = "Eureka Server", version = "1.0", description = "Servidor de descubrimiento de servicios"))
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}

