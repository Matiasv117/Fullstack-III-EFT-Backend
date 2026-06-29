package com.saludrednorte.ms_optimizacion;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "Optimization Service", version = "1.0", description = "Optimización de citas médicas"))
@SpringBootApplication
@EnableFeignClients
public class MsOptimizacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsOptimizacionApplication.class, args);
    }

}
