package com.saludrednorte.ms_auditoria;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableRabbit
public class MsAuditoriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAuditoriaApplication.class, args);
    }
}

