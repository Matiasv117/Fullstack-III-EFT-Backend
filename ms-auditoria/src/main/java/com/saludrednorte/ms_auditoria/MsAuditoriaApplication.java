package com.saludrednorte.ms_auditoria;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@OpenAPIDefinition(info = @Info(title = "Audit Service", version = "1.0", description = "Auditoría de eventos del sistema"))
@SpringBootApplication
@EnableDiscoveryClient
@EnableRabbit
public class MsAuditoriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAuditoriaApplication.class, args);
    }
}

