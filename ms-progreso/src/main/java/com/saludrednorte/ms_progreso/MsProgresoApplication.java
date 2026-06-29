package com.saludrednorte.ms_progreso;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "Progress Service", version = "1.0", description = "Seguimiento de progreso de pacientes"))
@SpringBootApplication
public class MsProgresoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProgresoApplication.class, args);
    }
}

