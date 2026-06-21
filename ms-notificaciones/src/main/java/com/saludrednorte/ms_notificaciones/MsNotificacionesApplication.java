package com.saludrednorte.ms_notificaciones;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
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
@SpringBootApplication
@EnableRabbit
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
public class MsNotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsNotificacionesApplication.class, args);
    }
}
