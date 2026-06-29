package com.saludrednorte.gateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway para enrutamiento de solicitudes a los microservicios.
 * Descubre dinámicamente los servicios a través de Eureka.
 */
@OpenAPIDefinition(info = @Info(title = "API Gateway", version = "1.0", description = "API Gateway para enrutamiento de solicitudes a los microservicios"))
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}

