package com.saludrednorte.ms_notificaciones;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del Microservicio de Notificaciones
 * Responsable de gestionar el envío de notificaciones a pacientes
 * en el sistema de salud RedNorte
 */
@OpenAPIDefinition(info = @Info(title = "Notification Service", version = "1.0", description = "Gestión de notificaciones a pacientes"))
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
public class MsNotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsNotificacionesApplication.class, args);
    }
}
